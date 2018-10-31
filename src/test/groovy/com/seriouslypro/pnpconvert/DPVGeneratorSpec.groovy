package com.seriouslypro.pnpconvert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class DPVGeneratorSpec extends Specification {

    DPVHeader dpvHeader = new DPVHeader(
            fileName: "TEST-FILE",
            pcbFileName: "TEST-PCB-FILE"
    )

    List<ComponentPlacement> componentPlacements
    Components components
    Feeders feeders

    OutputStream outputStream

    void setup() {
        componentPlacements = []
        components = new Components()
        feeders = new Feeders()

        outputStream = new ByteOutputStream()

        System.out.flush()
    }

    def 'generate empty dpv'() {
        given:
            DPVGenerator generator = buildGenerator()

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content.startsWith("separated")

        and:
            content.contains("Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed")

        and:
            content.contains("Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay")
    }

    def 'generate for components in feeders'() {
        given:
            Component component1 = new Component(
                name: "10K 0402 1%/RES_0402"
            )
            Component component2 = new Component(
                name: "100nF 6.3V 0402/CAP_0402"
            )
            components.add(component1)
            components.add(component2)

            PickSettings pickSettings = new PickSettings()

            FeederProperties leftHandSideReel = new FeederProperties(
                feederAngle: 90
            )
            FeederProperties rightHandSideReel = new FeederProperties(
                feederAngle: 270
            )

            feeders.loadReel(1, 8, component1, pickSettings, "Cheap " + component1.name, leftHandSideReel)
            feeders.loadReel(36, 8, component2, pickSettings, "Expensive " + component2.name, rightHandSideReel)

        and:
            componentPlacements = [
                new ComponentPlacement(
                    refdes: "R1",
                    pattern: "RES_0402_HIGH_DENSITY",
                    coordinate: new Coordinate(x: 14.44, y: 13.9),
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "10K 0402 1%",
                    name: "RES_0402"
                ),
                new ComponentPlacement(
                    refdes: "C1",
                    pattern: "CAP_0402_HIGH_DENSITY",
                    coordinate: new Coordinate(x: 24.89, y: 21.64),
                    side: PCBSide.TOP,
                    rotation: 225,
                    value: "100nF 6.3V 0402",
                    name: "CAP_0402"
                )
            ]

        and:
            DPVGenerator generator = buildGenerator()

        and:
            List<List<String>> expectedMaterials = [
                ["Station","0","1","0","0","4","Cheap 10K 0402 1%/RES_0402","0.5","0","6","0","0","0","0","0"],
                ["Station","1","36","0","0","4","Expensive 100nF 6.3V 0402/CAP_0402","0.5","0","6","0","0","0","0","0"]
            ]

        and:
            List<List<String>> expectedComponents = [
                ["EComponent","0","1","1","1","14.44","13.9","180","0.5","6","0","R1","10K 0402 1%/RES_0402","0"],
                ["EComponent","1","2","1","36","24.89","21.64","135","0.5","6","0","C1","100nF 6.3V 0402/CAP_0402","0"]
            ]

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            materialsPresent(content, expectedMaterials)
            componentsPresent(content, expectedComponents)
    }

    @Ignore
    def 'placement with unknown component'() {
        expect:
            false
    }

    @Ignore
    def 'unloaded component'() {
        expect:
            false
    }

    @Unroll
    def 'placement angle - #designAngle, #pickAngle, #feederAngle'(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle, BigDecimal expectedMachineAngle) {

        /*
            Design, Feeder, Pick angles are positive clockwise, 0 to 360.
            Machine angles are negative clockwise, -180 to +180.

            Design angle = angle in EDA file.
            Pick angle = angle relative to pick head and feeder e.g. angle of component in tape to tape feed direction
            Feeder angle = feeder angle relative to design

            Example and Notes:
            feeders on the left are 270 degrees out relative to the design (clockwise)
            feeders on the right are 90 degrees out relative to the design (clockwise)
            feeders on the left are 180 degrees out relative to the feeders on the right
            components in a tape, e.g. usb connectors, can be different relative to the design.

            IMPORTANT: Pick angles other than 0/90/180/270 will cause confusion for the vision system which will just assume
            that the part has been picked at a strange angle and will correct to the nearest 90 degrees on the nozzle
            during placement.

            When vision is disabled the pick angle on the feeder needs to be correct.
         */
        expect:
            expectedMachineAngle == buildGenerator().calculateMachineAngle(designAngle, pickAngle, feederAngle)

        where:
            designAngle | pickAngle | feederAngle  | expectedMachineAngle
            180         | 0         | 270          | 90
            270         | 0         | 270          | 180
            0           | 0         | 270          | -90
            90          | 0         | 270          | 0
            180         | 0         | 90           | -90
            270         | 0         | 90           | 0
            0           | 0         | 90           | 90
            90          | 0         | 90           | 180
            45          | 0         | 270          | -45
            315         | 0         | 270          | -135
            0           | 90        | 270          | 0
            90          | 90        | 270          | 90
    }

    private DPVGenerator buildGenerator() {
        DPVGenerator generator = new DPVGenerator(
                dpvHeader: dpvHeader,
                placements: componentPlacements,
                components: components,
                feeders: feeders
        )
        generator
    }

    static final int MATERIAL_COLUMN_COUNT = 15
    static final int COMPONENT_COLUMN_COUNT = 14

    void materialsPresent(String content, List<List<String>> materialRows) {
        materialRows.each { List<String> materialRow ->
            assert(materialRow.size() == MATERIAL_COLUMN_COUNT)
            String row = materialRow.join(",")
            assert content.contains(row)
        }
    }

    void componentsPresent(String content, List<List<String>> componentRows) {
        componentRows.each { componentRow ->
            assert(componentRow.size() == COMPONENT_COLUMN_COUNT)
            String row = componentRow.join(",")
            assert content.contains(row)
        }
    }

}
