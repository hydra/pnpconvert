package com.seriouslypro.pnpconvert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class DPVGeneratorSpec extends Specification {

    private static final LF = System.getProperty("line.separator")

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
            materialsPresent(content, [])

        and:
            componentsPresent(content, [])

        and:
            traysPresent(content, [])

        and:
            content.contains(
                "Table,No.,ID,DeltX,DeltY" + LF +
                "Panel_Coord,0,1,0,0"
            )
    }

    def 'generate for components in feeders'() {
        given:
            Component component1 = new Component(
                name: "10K 0402 1%/RES_0402"
            )
            Component component2 = new Component(
                name: "100nF 6.3V 0402/CAP_0402"
            )
            Component component3 = new Component(
                name: "MAX14851"
            )
            Component component4 = new Component(
                name: "CAT24C32WI-GT3"
            )
            components.add(component1)
            components.add(component2)
            components.add(component3)
            components.add(component4)

        and:
            PickSettings pickSettings = new PickSettings()

        and:
            FeederProperties leftHandSideReel = new FeederProperties(
                feederAngle: 90
            )
            FeederProperties rightHandSideReel = new FeederProperties(
                feederAngle: 270
            )

            feeders.loadReel(1, 8, component1.name, pickSettings, "Cheap", leftHandSideReel)
            feeders.loadReel(36, 8, component2.name, pickSettings, "Expensive", rightHandSideReel)

        and:
            Tray tray1 = new Tray(
                name: "B-1-4-TL",
                firstComponentX: 205.07G, firstComponentY: 61.05G,
                lastComponentX: 277.1G, lastComponentY: 61.11G,
                columns: 4,
                rows: 1,
                firstComponentIndex: 0
            )

            Tray tray2 = new Tray(
                name: "B-6-7-TL",
                firstComponentX: 327.5G, firstComponentY: 58.57G,
                lastComponentX: 351.51G, lastComponentY: 58.57G,
                columns: 2,
                rows: 1,
                firstComponentIndex: 0
            )

            FeederProperties trayFeederProperties = new FeederProperties(
                feederAngle: 0
            )

            feeders.loadTray(91, tray1, component3.name, pickSettings, "Back 1-4 Top-Left", trayFeederProperties)
            feeders.loadTray(92, tray2, component4.name, pickSettings,"Back 6-7 Top-Left", trayFeederProperties)

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
                    refdes: "R2",
                    pattern: "RES_0402_HIGH_DENSITY",
                    coordinate: new Coordinate(x: 15.72, y: 25.2),
                    side: PCBSide.TOP,
                    rotation: 180,
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
                ),
                new ComponentPlacement(
                    refdes: "U1",
                    pattern: "QSOP-16",
                    coordinate: new Coordinate(x: 21.3, y: 35.07),
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "",
                    name: "MAX14851"
                ),
                new ComponentPlacement(
                    refdes: "U2",
                    pattern: "QSOP-16",
                    coordinate: new Coordinate(x: 21.3, y: 19.92),
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "",
                    name: "MAX14851"
                ),
                new ComponentPlacement(
                    refdes: "U3",
                    pattern: "SOIC-8/150mil",
                    coordinate: new Coordinate(x: 16.8, y: 45.45),
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "",
                    name: "CAT24C32WI-GT3"
                )
            ]

            boolean haveTwoIdenticalComponents = (
                componentPlacements[0].value == componentPlacements[1].value &&
                componentPlacements[0].name == componentPlacements[1].name
            )
            assert haveTwoIdenticalComponents // two identical components should result in a single material.

        and:
            DPVGenerator generator = buildGenerator()

        and:
            List<List<String>> expectedMaterials = [
                ["Station","0","1","0","0","4","10K 0402 1%/RES_0402 - Cheap","0.5","0","6","0","0","0","0","0"],
                ["Station","1","36","0","0","4","100nF 6.3V 0402/CAP_0402 - Expensive","0.5","0","6","0","0","0","0","0"],
                ["Station","2","91","0","0","4","MAX14851 - Back 1-4 Top-Left","0.5","0","6","0","0","0","0","0"],
                ["Station","3","92","0","0","4","CAT24C32WI-GT3 - Back 6-7 Top-Left","0.5","0","6","0","0","0","0","0"],
            ]

        and:
            List<List<String>> expectedComponents = [
                ["EComponent","0","1","1","1","14.44","13.9","180","0.5","6","0","R1","10K 0402 1%/RES_0402","0"],
                ["EComponent","1","2","1","1","15.72","25.2","-90","0.5","6","0","R2","10K 0402 1%/RES_0402","0"],
                ["EComponent","2","3","1","36","24.89","21.64","135","0.5","6","0","C1","100nF 6.3V 0402/CAP_0402","0"],
                ["EComponent","3","4","1","91","21.3","35.07","90","0.5","6","0","U1","/MAX14851","0"],
                ["EComponent","4","5","1","91","21.3","19.92","90","0.5","6","0","U2","/MAX14851","0"],
                ["EComponent","5","6","1","92","16.8","45.45","90","0.5","6","0","U3","/CAT24C32WI-GT3","0"],
            ]

        and:
            List<List<String>> expectedTrays = [
                ["ICTray","0","91","205.07","61.05","277.1","61.11","4","1","0"],
                ["ICTray","1","92","327.5","58.57","351.51","58.57","2","1","0"],
            ]

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            materialsPresent(content, expectedMaterials)
            componentsPresent(content, expectedComponents)
            traysPresent(content, expectedTrays)
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

    @Ignore
    def 'feeder match using a component alias'() {
        expect:
            false
    }

    @Ignore
    def 'feeder match using component that is an alias of component'() {
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
    static final int TRAY_COLUMN_COUNT = 10

    void materialsPresent(String content, List<List<String>> materialRows) {
        assert content.contains("Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed")

        materialRows.each { List<String> materialRow ->
            assert(materialRow.size() == MATERIAL_COLUMN_COUNT)
            String row = materialRow.join(",")
            assert content.contains(row)
        }
    }

    void componentsPresent(String content, List<List<String>> componentRows) {
        assert content.contains("Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay")

        componentRows.each { List<String> componentRow ->
            assert(componentRow.size() == COMPONENT_COLUMN_COUNT)
            String row = componentRow.join(",")
            assert content.contains(row)
        }
    }

    void traysPresent(String content, ArrayList<List<String>> trayRows) {
        assert content.contains("Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start")
        trayRows.each { List<String> trayRow ->
            assert (trayRow.size() == TRAY_COLUMN_COUNT)
            String row = trayRow.join(",")
            assert content.contains(row)
        }
    }

    @Ignore
    def 'generate panel'() {
        //https://github.com/sparkfunX/Desktop-PickAndPlace-CHMT36VA/blob/master/Eagle-Conversion/ConvertToCharm.ulp#L469-L498
        expect:
            false
    }

    @Ignore
    def 'generate array'() {
        //https://github.com/sparkfunX/Desktop-PickAndPlace-CHMT36VA/blob/master/Eagle-Conversion/ConvertToCharm.ulp#L469-L498
        expect:
            false
    }

}
