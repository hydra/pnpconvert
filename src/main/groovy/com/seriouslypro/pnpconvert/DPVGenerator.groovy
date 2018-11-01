package com.seriouslypro.pnpconvert

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

    private PrintStream stream

    void generate(OutputStream outputStream) {

        placementsWithUnknownComponents = []
        unloadedComponents = []

        materialNumberSequence = new NumberSequence(0)
        Map<ComponentPlacement, MaterialSelection> materialSelections = selectMaterials()

        System.out.println("placementsWithUnknownComponents:\n" + placementsWithUnknownComponents.join('\n'))
        System.out.println("unloadedComponents:\n" + unloadedComponents.join('\n'))

        List<String[]> placements = buildPlacements(materialSelections)

        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader)
        writeMaterials(materialSelections)
        writePlacements(placements)
    }

    class MaterialSelection {
        Component component
        Integer feederId
        Feeder feeder
        String[] material
    }

    Map<ComponentPlacement, MaterialSelection> selectMaterials() {
        Map<ComponentPlacement, MaterialSelection> materialSelections = [:]

        placements.each { ComponentPlacement placement ->
            Component component = components.findByPlacement(placement)
            if (!component) {
                placementsWithUnknownComponents << placement
                return
            }

            FeederMapping findResult = feeders.findByComponent(component)
            if (!findResult) {
                unloadedComponents << component
                return
            }
            def (Integer feederId, Feeder feeder) = [findResult.id, findResult.feeder]

            MaterialSelection materialSelection = new MaterialSelection(
                component: component,
                feederId: feederId,
                feeder: feeder,
                material:  buildMaterial(feederId, feeder, component)
            )

            materialSelections[placement] = materialSelection
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

    void writeHeader(DPVHeader dpvHeader) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('hh:mm:ss').format(now)

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

        materials.values().each { materialSelection ->
            stream.println(materialSelection.material.join(","))
        }
        stream.println()
    }

    String[] buildMaterial(Integer feederId, Feeder feeder, Component component) {

        /*
        Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed
        Station,0,29,4.17,0,12,??,3.75,0,6,0,0,0,0,0
         */

        PickSettings pickSettings = feeder.pickSettings

        int statusFlags = buildStatus(feeder.enabled, pickSettings)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")
        String[] material = [
                "Station",
                materialNumberSequence.next(),
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

        statusFlags |= (1 << 7)

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
