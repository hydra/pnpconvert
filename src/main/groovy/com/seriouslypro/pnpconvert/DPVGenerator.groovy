package com.seriouslypro.pnpconvert

import groovy.transform.ToString

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class DPVGenerator {
    DPVHeader dpvHeader
    List<ComponentPlacement> placements
    Components components
    Feeders feeders

    NumberSequence materialNumberSequence

    List<ComponentPlacement> placementsWithUnknownComponents
    Set<Component> unloadedComponents
    Map<Feeder, Component> feedersMatchedByAlias
    Map<ComponentPlacement, ComponentFindResult> inexactComponentMatches

    private PrintStream stream

    void generate(OutputStream outputStream) {

        placementsWithUnknownComponents = []
        unloadedComponents = []
        feedersMatchedByAlias = [:]
        inexactComponentMatches = [:]

        materialNumberSequence = new NumberSequence(0)
        Map<ComponentPlacement, MaterialSelection> materialSelections = selectMaterials()

        System.out.println()
        System.out.println("placementsWithUnknownComponents:\n" + placementsWithUnknownComponents.join('\n'))
        System.out.println()
        System.out.println("unloadedComponents:\n" + unloadedComponents.join('\n'))
        System.out.println()
        System.out.println("inexactComponentsMatches:\n" + inexactComponentMatches.collect { ComponentPlacement placement, ComponentFindResult componentFindResult ->
            "placement: $placement.name, component: $componentFindResult.component, strategies: $componentFindResult.matchingStrategies"
        }.join('\n'))
        System.out.println()
        System.out.println("feedersMatchedByAlias:\n" + feedersMatchedByAlias.collect { Feeder feeder, Component component ->
            "feederComponent: $feeder.componentName, component: $component"
        }.join('\n'))

        System.out.println()
        System.out.println("materialSelections:\n" + materialSelections.collect { ComponentPlacement placement, MaterialSelection materialSelection ->
            "placement: $placement, materialSelection: $materialSelection"
        }.join('\n'))


        List<String[]> placements = buildPlacements(materialSelections)

        List<String[]> trays = buildTrays(materialSelections)

        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader)
        writeMaterials(materialSelections)
        writePlacements(placements)
        writeTrays(trays)
        writePanel()
    }

    Map<ComponentPlacement, MaterialSelection> selectMaterials() {
        Map<ComponentPlacement, MaterialSelection> materialSelections = [:]

        placements.each { ComponentPlacement placement ->
            ComponentFindResult componentFindResult = components.findByPlacement(placement)

            if (!componentFindResult) {
                placementsWithUnknownComponents << placement
                return
            }

            if (!componentFindResult.isExactMatch()) {
                inexactComponentMatches[placement] = componentFindResult
            }

            Component component = componentFindResult.component

            //
            // feeder with component?
            //
            FeederMapping findResult = feeders.findByComponent(component.name)
            if (!findResult) {

                //
                // feeder with alias?
                //
                findResult = component.aliases.findResult { alias ->
                    feeders.findByComponent(alias)
                }

                if (!findResult) {
                    //
                    // alias component in feeder?
                    //
                    Component aliasComponent = components.components.find { otherComponent -> otherComponent.aliases.contains(component.name) }

                    if (aliasComponent) {
                        findResult = component.aliases.findResult { alias ->
                            feeders.findByComponent(aliasComponent.name)
                        }
                    }

                    if (!findResult) {
                        unloadedComponents << component
                        return
                    }
                }

                feedersMatchedByAlias[findResult.feeder] = component

            }
            def (Integer feederId, Feeder feeder) = [findResult.id, findResult.feeder]

            MaterialSelection existingMaterialSelection = materialSelections.values().find { MaterialSelection candidate ->
                candidate.feederId == feederId
            }

            if (existingMaterialSelection) {
                materialSelections[placement] = existingMaterialSelection
            } else {

                MaterialSelection materialSelection = new MaterialSelection(
                        component: component,
                        feederId: feederId,
                        feeder: feeder,
                        material: buildMaterial(feederId, feeder, component)
                )

                materialSelections[placement] = materialSelection
            }
        }

        return materialSelections
    }

    List<String[]> buildPlacements(Map<ComponentPlacement, MaterialSelection> materialSelections) {

        /*
        Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay
        EComponent,0,1,1,16,24.89,21.64,90,0.5,5,0,C1,100nF 6.3V 0402/CAP_0402,0
         */

        List<String[]> placements =[]

        NumberSequence placementNumberSequence = new NumberSequence(0)
        NumberSequence placementIDSequence = new NumberSequence(1)

        materialSelections.each { ComponentPlacement componentPlacement, MaterialSelection materialSelection ->

            PickSettings pickSettings = materialSelection.feeder.pickSettings

            BigDecimal counterClockwiseMachineAngle = calculateMachineAngle(
                componentPlacement.rotation,
                pickSettings.packageAngle,
                materialSelection.feeder.properties.feederAngle
            )

            String[] placement = [
                "EComponent",
                placementNumberSequence.next(),
                placementIDSequence.next(),
                materialSelection.feeder.pickSettings.head,
                materialSelection.feederId,
                componentPlacement.coordinate.x,
                componentPlacement.coordinate.y,
                counterClockwiseMachineAngle,
                materialSelection.component.height,
                buildStatus(materialSelection.feeder.enabled, pickSettings),
                buildPlaceSpeed(pickSettings.placeSpeedPercentage),
                componentPlacement.refdes,
                componentPlacement.value + "/" + componentPlacement.name,
                pickSettings.placeDelay
            ]

            placements << placement
        }

        return placements
    }

    private int buildPlaceSpeed(int placeSpeedPercentage) {
        100 - placeSpeedPercentage
    }

    BigDecimal calculateMachineAngle(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle) {

        BigDecimal machineAngle = (designAngle + feederAngle + pickAngle).remainder(360)

        if (machineAngle > 180) machineAngle -= 360

        return machineAngle
    }

    List<String[]> buildTrays(Map<ComponentPlacement, MaterialSelection> materialSelections) {
        Map<Integer, String[]> trays = [:]

        NumberSequence trayNumberSequence = new NumberSequence(0)

        materialSelections.each { ComponentPlacement placement, MaterialSelection materialSelection ->
            Feeder candidate = materialSelection.feeder

            boolean feederUsesTray = candidate instanceof TrayFeeder
            boolean alreadyProcessed = trays.containsKey(materialSelection.feederId)
            if (!feederUsesTray || alreadyProcessed) {
                return
            }

            Tray tray = ((TrayFeeder)candidate).tray

            String[] trayRow = [
                "ICTray",
                trayNumberSequence.next(),
                materialSelection.feederId,
                tray.firstComponentX,
                tray.firstComponentY,
                tray.lastComponentX,
                tray.lastComponentY,
                tray.columns,
                tray.rows,
                tray.firstComponentIndex,
            ]

            trays[materialSelection.feederId] = trayRow
        }

        trays.values() as List<String[]>
    }

    void writeHeader(DPVHeader dpvHeader) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('HH:mm:ss').format(now)

        String header = "separated\n" +
                DPVFileHeaders.FILE + ",$dpvHeader.fileName\n" +
                DPVFileHeaders.PCBFILE + ",$dpvHeader.pcbFileName\n" +
                DPVFileHeaders.DATE + ",$formattedDate\n" +
                DPVFileHeaders.TIME + ",$formattedTime\n" +
                DPVFileHeaders.PANELTYPE + ",0" // Type 0 = batch of PCBs. Type 1 = panel of PCBs.

        stream.println(header)
        stream.println()
    }

    def writeMaterials(Map<ComponentPlacement, MaterialSelection> materials) {
        String sectionHeader =
                "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed"
        stream.println(sectionHeader)


        materials.values().toUnique { a ->
            a.feederId
        }.toSorted { a, b ->
            a.feederId <=> b.feederId
        }.each { materialSelection ->
            String[] managedColumns = [
                "Station",
                materialNumberSequence.next()
            ]
            stream.println((managedColumns + materialSelection.material).join(","))
        }
        stream.println()
    }

    String[] buildMaterial(Integer feederId, Feeder feeder, Component component) {

        /*
        Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed
        Station,0,29,4.17,0,12,??,3.75,0,6,0,0,0,0,0
         */

        //
        // Note: Table and No. are assigned later
        // Note: this method may generate a duplicate material, duplicates are filtered before being written.

        PickSettings pickSettings = feeder.pickSettings

        int statusFlags = buildStatus(feeder.enabled, pickSettings)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")
        String[] material = [
                feederId,
                twoDigitDecimalFormat.format(pickSettings.xOffset),
                twoDigitDecimalFormat.format(pickSettings.yOffset),
                pickSettings.tapeSpacing,
                buildMaterialNote(component, feeder),
                twoDigitDecimalFormat.format(component.height),
                buildPlaceSpeed(pickSettings.placeSpeedPercentage),
                statusFlags & 0xFF,
                twoDigitDecimalFormat.format(component.width),
                twoDigitDecimalFormat.format(component.length),
                pickSettings.takeHeight,
                pickSettings.placeDelay,
                pickSettings.pullSpeed
        ]

        return material
    }

    private String buildMaterialNote(Component component, Feeder feeder) {
        String materialNote = component.name
        if (feeder.note) {
            materialNote += " - " + feeder.note
        }
        materialNote
    }

    private int buildStatus(boolean enabled, PickSettings pickSettings) {
        int statusFlags = 0

        //76543210 bitmask
        //2: 1 = Use Vision
        //2: 0 = No Vision
        //1: 1 = Use Vacuum Detection
        //1: 0 = No Vacuum Detection
        //0: 1 = Skip placement
        //0: 0 = Place this component
        //Example: 3 (decimal) (0b00000011) = Skip placement, Use vacuum detection, No vision

        if (!enabled) {
            statusFlags |= (1 << 0)
        }

        if (pickSettings.checkVacuum) {
            statusFlags |= (1 << 1)
        }
        if (pickSettings.useVision) {
            statusFlags |= (1 << 2)
        }
        statusFlags
    }

    void writePlacements(List<String[]> placements) {
        String sectionHeader =
            "Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay"

        stream.println(sectionHeader)

        placements.each { placement ->
            stream.println(placement.join(","))
        }
        stream.println()
    }

    void writeTrays(List<String[]> trays) {
        String sectionHeader = "Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start"

        stream.println(sectionHeader)

        trays.each { tray ->
            stream.println(tray.join(","))
        }

        stream.println()
    }

    void writePanel() {
        stream.println("Table,No.,ID,DeltX,DeltY")
        stream.println("Panel_Coord,0,1,0,0")
        stream.println()
    }

    class NumberSequence {
        int index = 0

        NumberSequence(int first) {
            index = first
        }

        int next() {
            int id = index
            index++
            return id
        }
    }
}

@ToString(includeNames = true, includePackage = false)
class MaterialSelection {
    Component component
    Integer feederId
    Feeder feeder
    String[] material
}
