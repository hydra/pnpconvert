package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine
import groovy.transform.ToString

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class DPVGenerator {
    DPVHeader dpvHeader
    List<ComponentPlacement> placements
    Components components
    Feeders feeders
    BigDecimal offsetZ

    Machine machine

    NumberSequence materialNumberSequence

    List<ComponentPlacement> placementsWithUnknownComponents
    Set<Component> unloadedComponents
    Map<Feeder, Component> feedersMatchedByAlias
    Map<ComponentPlacement, ComponentFindResult> inexactComponentMatches

    private String lineEnding
    private String tableLineEnding

    private PrintStream stream
    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials

    void generate(OutputStream outputStream) {

        lineEnding = "\r\n"

        tableLineEnding = lineEnding * 2

        placementsWithUnknownComponents = []
        unloadedComponents = []
        feedersMatchedByAlias = [:]
        inexactComponentMatches = [:]

        materialNumberSequence = new NumberSequence(0)
        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = selectMaterials()
        Map<ComponentPlacement, MaterialAssignment> materialAssignments = assignMaterials(materialSelections)

        MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()
        materialAssignments = materialAssignmentSorter.sort(materialAssignments)

        relocatePlacements(materialAssignments)

        System.out.println('')
        System.out.println('*** MATERIAL ASSIGNMENTS *** - Components from the design that matched the components and feeders/trays')
        System.out.println('')

        System.out.println()
        System.out.println("materialAssignments:\n" + materialAssignments.collect { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            "placement: $placement, materialAssignment: $materialAssignment"
        }.join('\n'))

        List<Integer> usedFeederIds = materialAssignments.collect { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            materialAssignment.feederId
        }.unique().sort()

        System.out.println()
        System.out.println("usedFeeders:\n" + usedFeederIds.join(','))

        ArrayList<FeederPrinter> feederPrinters = [
            new TrayFeederPrinter(),
            new ReelFeederPrinter()
        ]

        int countOfUnitsInPanel = optionalPanel.map { panel -> panel.numberX * panel.numberY }.orElseGet{ 1 }

        List<Map<String, String>> summaryItems = usedFeederIds.findResults { Integer feederId ->
            Map.Entry<ComponentPlacement, MaterialAssignment> materialAssigment = materialAssignments.find { ComponentPlacement placement, MaterialAssignment materialAssignment ->
                materialAssignment.feederId == feederId
            }
            Feeder feeder = materialAssigment.value.feeder
            FeederPrinter feederPrinter = feederPrinters.find { it.canPrint(feeder) }

            Map<ComponentPlacement, MaterialAssignment> materialAssigmentsUsingSameFeeder = materialAssignments.findAll { ComponentPlacement placement, MaterialAssignment materialAssignment ->
                materialAssignment.feederId == feederId
            }

            List<String> refdesList = materialAssigmentsUsingSameFeeder.keySet().findResults { ComponentPlacement placement -> placement.enabled ? placement.refdes : null }
            int countOfComponentsUsed  = refdesList.size()
            int totalComponentsUserPerPanel = countOfComponentsUsed * countOfUnitsInPanel
            [
                feederId            : materialAssigment.value.feederId.toString(),
                componentsPerUnit   : countOfComponentsUsed,
                componentsPerPanel  : totalComponentsUserPerPanel,
                refdes              : refdesList,
                feeder              : feederPrinter.print(feeder),
                component           : [
                    name   : materialAssigment.value.component.name,
                    aliases: materialAssigment.value.component.aliases,
                ].toString(),
            ]
        }
        if (!summaryItems.isEmpty()) {
            System.out.println()
            System.out.println("feederSummary:")
            System.out.println(summaryItems.first().keySet().join(','))
            System.out.println(summaryItems.collect { it.values().join(',') }.join('\n'))
        }

        System.out.println()
        System.out.println('*** ISSUES *** - Components that did not match, need verification or loading')
        System.out.println('')

        System.out.println()
        System.out.println("placementsWithUnknownComponents:\n" + placementsWithUnknownComponents.join('\n'))

        System.out.println()
        System.out.println("inexactComponentsMatches:\n" + inexactComponentMatches.collect { ComponentPlacement placement, ComponentFindResult componentFindResult ->
            "placement: $placement, component: $componentFindResult.component, strategies: $componentFindResult.matchingStrategies"
        }.join('\n'))

        System.out.println()
        System.out.println("feedersMatchedByAlias:\n" + feedersMatchedByAlias.collect { Feeder feeder, Component component ->
            "feederComponent: $feeder.componentName, component: $component"
        }.join('\n'))

        System.out.println()
        System.out.println("unloadedComponents:\n" + unloadedComponents.join('\n'))

        List<String[]> placements = buildPlacements(materialAssignments)

        Map<Integer, String[]> materials = buildMaterials(materialAssignments)
        List<String[]> trays = buildTrays(materialAssignments)

        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader)
        writeMaterials(materials)
        writePanel()
        writePlacements(placements)
        writeTrays(trays)
        writeFiducials()
    }

    Map<Integer, String[]> buildMaterials(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        Map<Integer, String[]> materials = materialAssignments.collectEntries { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            [materialAssignment.feederId, buildMaterial(materialAssignment.feederId, materialAssignment.feeder, materialAssignment.component)]
        }

        materials
    }

    void relocatePlacements(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        materialAssignments.each { ComponentPlacement cp, MaterialAssignment ma ->
            cp.coordinate = cp.coordinate.relocate(cp.rotation, ma.component.placementOffsetX, ma.component.placementOffsetY)
        }
    }

    Map<ComponentPlacement, MaterialSelectionEntry> selectMaterials() {

        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

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
            Feeder findResult = feeders.findByComponent(component.name)
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

                feedersMatchedByAlias[findResult] = component

            }
            Feeder feeder = findResult

            MaterialSelectionEntry materialSelection = new MaterialSelectionEntry(
                component: component,
                feeder: feeder,
            )

            materialSelections[placement] = materialSelection
        }

        return materialSelections
    }

    Integer assignFeederID(NumberSequence trayIdSequence, Set<Integer> usedIDs, Feeder feeder) {

        // Assign Feeder ID if required, code assumes feeder is a tray.
        // Trays may have a fixed id.  ID re-use needs to be avoided.

        Integer feederId = feeder.fixedId.orElseGet( {
            boolean found = false
            while (!found) {
                Integer candidateId = trayIdSequence.next()
                if (!usedIDs.contains(candidateId)) {
                    return candidateId
                }
            }
        })

        usedIDs.add(feederId)

        return feederId
    }

    Map<ComponentPlacement, MaterialAssignment> assignMaterials(Map<ComponentPlacement, MaterialSelectionEntry> materialSelections) {

        Set<Integer> usedIDs = materialSelections.findAll { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.present
        }.findResults { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.get()
        }.sort()

        Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]

        NumberSequence trayIdSequence = new NumberSequence(machine.trayIds.getFrom())

        materialSelections.each { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->

            Feeder feeder = materialSelectionEntry.feeder

            Component component = materialSelectionEntry.component


            MaterialAssignment existingMaterialAssignment = materialAssignments.values().find { MaterialAssignment candidate ->
                candidate.feeder.is(feeder)
            }

            if (existingMaterialAssignment) {
                materialAssignments[placement] = existingMaterialAssignment
            } else {

                Integer feederId = assignFeederID(trayIdSequence, usedIDs, feeder)

                MaterialAssignment materialAssignment = new MaterialAssignment(
                    component: component,
                    feederId: feederId,
                    feeder: feeder
                )
                materialAssignments[placement] = materialAssignment
            }

        }

        return materialAssignments
    }

    List<String[]> buildPlacements(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        /*
        Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay
        EComponent,0,1,1,16,24.89,21.64,90,0.5,5,0,C1,100nF 6.3V 0402/CAP_0402,0
         */

        // "Note" field is limited to 31 characters, e.g. "47uF 6.3V 1206 10% TANTALUM/CAP_1206" will be re-saved by the machine as "47uF 6.3V 1206 10% TANTALUM/CAP".


        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        List<String[]> placements =[]

        NumberSequence placementNumberSequence = new NumberSequence(0)
        NumberSequence placementIDSequence = new NumberSequence(1)

        materialAssignments.each { ComponentPlacement componentPlacement, MaterialAssignment materialAssignment ->

            PickSettings pickSettings = materialAssignment.feeder.pickSettings
            FeederProperties feederProperties = machine.feederProperties(materialAssignment.feederId)

            BigDecimal counterClockwiseMachineAngle = calculateMachineAngle(
                componentPlacement.rotation,
                pickSettings.packageAngle,
                feederProperties.feederAngle
            )

            String[] placement = [
                "EComponent",
                placementNumberSequence.next(),
                placementIDSequence.next(),
                materialAssignment.feeder.pickSettings.head,
                materialAssignment.feederId,
                twoDigitDecimalFormat.format(componentPlacement.coordinate.x),
                twoDigitDecimalFormat.format(componentPlacement.coordinate.y),
                twoDigitDecimalFormat.format(counterClockwiseMachineAngle),
                twoDigitDecimalFormat.format(materialAssignment.component.height + offsetZ),
                buildStatus(componentPlacement.enabled && materialAssignment.feeder.enabled, pickSettings),
                buildPlaceSpeed(pickSettings.placeSpeedPercentage),
                componentPlacement.refdes,
                (componentPlacement.value + "/" + componentPlacement.name).take(31),
                (pickSettings.placeDelay * 100) as Integer
            ]

            placements << placement
        }

        return placements
    }

    private int buildPlaceSpeed(int placeSpeedPercentage) {
        if (placeSpeedPercentage >= 100 || placeSpeedPercentage == 0) {
            return 100
        }
        return placeSpeedPercentage
    }

    BigDecimal calculateMachineAngle(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle) {

        BigDecimal machineAngle = (designAngle + feederAngle + pickAngle).remainder(360)

        machineAngle = 180 - machineAngle

        if (machineAngle > 180) machineAngle -= 360

        return machineAngle
    }

    List<String[]> buildTrays(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        Map<Integer, MaterialAssignment> trays = [:]

        NumberSequence trayNumberSequence = new NumberSequence(0)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        materialAssignments.each { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            Feeder candidate = materialAssignment.feeder

            boolean feederUsesTray = candidate instanceof TrayFeeder
            boolean alreadyProcessed = trays.containsKey(materialAssignment.feederId)
            if (!feederUsesTray || alreadyProcessed) {
                return
            }

            trays[materialAssignment.feederId] = materialAssignment
        }

        trays = trays.sort { a, b ->
            a.key <=> b.key
        }

        List<String[]> trayRows = trays.collect { Integer feederId, MaterialAssignment materialAssignment ->
            Tray tray = ((TrayFeeder) materialAssignment.feeder).tray

            String[] trayRow = [
                "ICTray",
                trayNumberSequence.next(),
                materialAssignment.feederId,
                twoDigitDecimalFormat.format(tray.firstComponentX),
                twoDigitDecimalFormat.format(tray.firstComponentY),
                twoDigitDecimalFormat.format(tray.lastComponentX),
                twoDigitDecimalFormat.format(tray.lastComponentY),
                tray.columns,
                tray.rows,
                tray.firstComponentIndex,
            ]

            return trayRow
        }

        trayRows
    }

    void writeHeader(DPVHeader dpvHeader) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('HH:mm:ss').format(now)

        String panelTypeValue = optionalPanel.present ? "1" : "0" // Type 0 = batch of PCBs. Type 1 = panel of PCBs.

        String content = "separated" + lineEnding +
                DPVFileHeaders.FILE + ",$dpvHeader.fileName" + lineEnding +
                DPVFileHeaders.PCBFILE + ",$dpvHeader.pcbFileName" + lineEnding +
                DPVFileHeaders.DATE + ",$formattedDate" + lineEnding +
                DPVFileHeaders.TIME + ",$formattedTime" + lineEnding +
                DPVFileHeaders.PANELTYPE + "," + panelTypeValue + lineEnding

        stream.print(content)
        stream.print(lineEnding)
    }

    def writeMaterials(Map<Integer, String[]> materials) {
        String sectionHeader =
                "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed"
        stream.print(sectionHeader + tableLineEnding)

        materials.toSorted { a, b ->
            a.key <=> b.key
        }.each { Integer feederId, String[] material ->
            String[] managedColumns = [
                "Station",
                materialNumberSequence.next()
            ]
            stream.print((managedColumns + material).collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }

    String[] buildMaterial(Integer feederId, Feeder feeder, Component component) {

        /*
        Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed
        Station,0,29,4.17,0,12,??,3.75,0,6,0,0,0,0,0
         */

        // SizeX/SizeY are INTEGER, machine accepts a range of 0.00 to 30.00 in the UI.
        // The when the BigDecimal (mm) values are stored in the DPV file they need to be multiplied by 100.  e.g. "0.01" -> "1" and "30.00" -> "3000"

        // DelayTake is INTEGER, machine accepts a range of 0.00 to 3.00 in the UI.
        // The when the BigDecimal (second) values are stored in the DPV file they need to be multiplied by 100.  1 second = 100, 0.01 second = 1.

        //
        // Note: Table and No. are assigned later
        // Note: this method may generate a duplicate material, duplicates are filtered before being written.

        PickSettings pickSettings = feeder.pickSettings

        int statusFlags = buildStatus(feeder.enabled, pickSettings)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")
        DecimalFormat zeroDigitDecimalFormat = new DecimalFormat("#0")
        String[] material = [
                feederId,
                twoDigitDecimalFormat.format(pickSettings.xOffset),
                twoDigitDecimalFormat.format(pickSettings.yOffset),
                twoDigitDecimalFormat.format(pickSettings.tapeSpacing),
                buildMaterialNote(component, feeder),
                twoDigitDecimalFormat.format(component.height),
                buildPlaceSpeed(pickSettings.placeSpeedPercentage),
                statusFlags & 0xFF,
                zeroDigitDecimalFormat.format(component.width * 100),
                zeroDigitDecimalFormat.format(component.length * 100),
                zeroDigitDecimalFormat.format(pickSettings.takeHeight * 100),
                zeroDigitDecimalFormat.format(pickSettings.takeDelay * 100),
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
        //3: 1 = Separate Mount (don't use other heads)
        //3: 0 = Combine Mount (use other heads)
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
        if (pickSettings.separateMount) {
            statusFlags |= (1 << 3)
        }
        statusFlags
    }

    void writePlacements(List<String[]> placements) {
        String sectionHeader =
            "Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay"

        stream.print(sectionHeader + tableLineEnding)

        placements.each { placement ->
            stream.print(placement.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }

    void writeTrays(List<String[]> trays) {
        String sectionHeader = "Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start"

        stream.print(sectionHeader + tableLineEnding)

        trays.each { tray ->
            stream.print(tray.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }

        stream.print(tableLineEnding)
    }

    void writePanel() {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        if (optionalPanel.present) {
            Panel panel = optionalPanel.get()
            stream.print("Table,No.,ID,IntervalX,IntervalY,NumX,NumY" + tableLineEnding)
            stream.print("Panel_Array,0,1,${twoDigitDecimalFormat.format(panel.intervalX)},${twoDigitDecimalFormat.format(panel.intervalY)},${panel.numberX},${panel.numberY}" + tableLineEnding)
            stream.print(tableLineEnding)
        } else {
            stream.print("Table,No.,ID,DeltX,DeltY" + tableLineEnding)
            stream.print("Panel_Coord,0,1,0,0" + tableLineEnding)
            stream.print(tableLineEnding)
        }
    }


    void writeFiducials() {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        NumberSequence fiducialNumberSequence = new NumberSequence(0)
        NumberSequence fiducialIDSequence = new NumberSequence(1)

        if (optionalFiducials.present) {

            //nType: 0 = use components, 1 = use marks
            //nFinished: ? 0 = calibration pending, 1 = calibration completed

            String calibationModeSectionHeader =
                "Table,No.,nType,nAlg,nFinished"

            stream.print(calibationModeSectionHeader + tableLineEnding)
            stream.print("PcbCalib,0,1,0,0" + lineEnding) // Note: NOT tableLineEnding
            stream.print(lineEnding) // Note: NOT tableLineEnding


            String calibationMarksSectionHeader =
                "Table,No.,ID,offsetX,offsetY,Note"

            stream.print(calibationMarksSectionHeader + tableLineEnding)

            List<Fiducial> fiducialList = optionalFiducials.get()

            fiducialList.each { fiducial ->
                String[] fiducialValues = [
                    "CalibPoint",
                    fiducialNumberSequence.next(),
                    fiducialIDSequence.next(),
                    twoDigitDecimalFormat.format(fiducial.coordinate.x),
                    twoDigitDecimalFormat.format(fiducial.coordinate.y),
                    fiducial.note
                ]
                stream.print(fiducialValues.collect { it.replace(',', ';') }.join(",") + lineEnding) // Note: NOT tableLineEnding
            }
            stream.print(lineEnding) // Note: NOT tableLineEnding
        }
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
class MaterialSelectionEntry {
    Component component
    Feeder feeder
}

@ToString(includeNames = true, includePackage = false)
class MaterialAssignment {
    Component component
    Integer feederId
    Feeder feeder
}
