package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import java.text.DecimalFormat

import org.springframework.boot.test.OutputCapture

class DPVGeneratorSpec extends Specification {

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    private static final CRLF = "\r\n"
    private static final TEST_TABLE_LINE_ENDING = CRLF * 2

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
            defaultPanelPresent(content)
    }

    def 'generate for components in feeders'() {
        given:
            Component component1 = new Component(
                name: "10K 0402 1%/RES_0402",
                width: 0.01,
                length: 30
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
            Component component5 = new Component(
                name: "Micro USB Socket With Very Long Name",
                width: 8,
                height: 3.5,
                length: 5,

                // use zero second fraction digit to aid matching rotation calculations (note: easy to see 0.1 + 0.02 = 0.12)
                // use offsets that will result it the first digit of the placement being different for X/Y
                placementOffsetX: 1.10,
                placementOffsetY: 0.80,
            )
            components.add(component5)
            components.add(component4)
            components.add(component3)
            components.add(component2)
            components.add(component1)

        and:
            PickSettings slowPickSettings = new PickSettings(
                placeDelay: 0.5G,
                takeDelay: 0.25G
            )
            PickSettings fastPickSettings = new PickSettings()

        and:
            FeederProperties leftHandSideReel = new FeederProperties(
                feederAngle: 90
            )
            FeederProperties rightHandSideReel = new FeederProperties(
                feederAngle: 270
            )

            feeders.loadFeeder(feeders.createReelFeeder(1, 8, component1.name, slowPickSettings, "Cheap"))
            feeders.loadFeeder(feeders.createReelFeeder(36, 8, component2.name, fastPickSettings, "Expensive"))

            feeders.loadFeeder(feeders.createReelFeeder(33, 12, component5.name, new PickSettings(separateMount: true, takeHeight: 2), "Special"))

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

            feeders.loadFeeder(feeders.createTrayFeeder(91, tray1, component3.name, slowPickSettings, "Back 1-4 Top-Left, Pin 1 Top-Left"))
            feeders.loadFeeder(feeders.createTrayFeeder(92, tray2, component4.name, fastPickSettings,"Back 6-7 Top-Left, Pin 1 Bottom-Right"))

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
                    rotation: 225, // arbitrary, without fraction, but not 0/90/180/270
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
                    coordinate: new Coordinate(x: 21.50, y: 19.50), // one digit fraction
                    side: PCBSide.TOP,
                    rotation: 22.5,  // arbitrary, with fraction, but not 0/90/180/270
                    value: "",
                    name: "MAX14851"
                ),
                new ComponentPlacement(
                    refdes: "U3",
                    pattern: "SOIC-8/150mil",
                    coordinate: new Coordinate(x: 16.00, y: 45.00), // no fraction
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "",
                    name: "CAT24C32WI-GT3"
                ),
                new ComponentPlacement(
                    enabled: false,
                    refdes: "J1",
                    pattern: "USB/MICRO1",
                    coordinate: new Coordinate(x: 50.03, y: 25.07), // use non-zero second fraction digit to aid matching rotation calculations
                    side: PCBSide.TOP,
                    rotation: 90, // rotation also used for placement offset
                    value: "",
                    name: "Micro USB Socket With Very Long Name"
                )
            ]

        and: // test data expectations
            boolean haveTwoIdenticalComponents = (
                componentPlacements[0].value == componentPlacements[1].value &&
                componentPlacements[0].name == componentPlacements[1].name
            )
            assert haveTwoIdenticalComponents // two identical components should result in a single material.

            boolean haveXYCoordinateWithoutFraction = componentPlacements.find {
                it.coordinate.x.remainder(BigDecimal.ONE) == BigDecimal.ZERO &&
                it.coordinate.y.remainder(BigDecimal.ONE) == BigDecimal.ZERO
            }
            assert haveXYCoordinateWithoutFraction // trailing zeros should be removed in output

            boolean haveXYCoordinateWithTrailingZeros = componentPlacements.find {
                it.coordinate.x.remainder(BigDecimal.ONE).precision() == 1 &&
                it.coordinate.y.remainder(BigDecimal.ONE).precision() == 1
            }
            assert haveXYCoordinateWithTrailingZeros // trailing zeros should be removed in output

            boolean haveAngleOf90 = componentPlacements.find { it.rotation == 90.0G }
            assert haveAngleOf90 // for checking rotation

            boolean haveAngleWithFraction = componentPlacements.find { it.rotation.remainder(BigDecimal.ONE) != BigDecimal.ZERO }
            assert haveAngleWithFraction // for checking rotation

            String nameOfComponentWithPlacementOffset = "Micro USB Socket With Very Long Name"
            boolean haveComponentWithPlacementOffset = componentPlacements.find { it.name == nameOfComponentWithPlacementOffset && it.rotation != 0 && it.coordinate.x != it.coordinate.y}
            boolean havePlacementThatUsesComponentWithPlacementOffset = components.components.find { it.name == nameOfComponentWithPlacementOffset && it.placementOffsetX != 0 && it.placementOffsetY != 0 && it.placementOffsetX != it.placementOffsetY}
            assert haveComponentWithPlacementOffset && havePlacementThatUsesComponentWithPlacementOffset

        and:
            DPVGenerator generator = buildGenerator()

        and:
            List<List<String>> expectedMaterials = [
                ["Station","0","1","0","0","4","10K 0402 1%/RES_0402 - Cheap","0.5","100","6","1","3000","0","25","0"],
                ["Station","1","33","0","0","4","Micro USB Socket With Very Long Name - Special","3.5","100","14","800","500","200","0","0"],
                ["Station","2","36","0","0","4","100nF 6.3V 0402/CAP_0402 - Expensive","0.5","100","6","0","0","0","0","0"],
                ["Station","3","1001","0","0","4","MAX14851 - Back 1-4 Top-Left; Pin 1 Top-Left","0.5","100","6","0","0","0","25","0"],
                ["Station","4","1002","0","0","4","CAT24C32WI-GT3 - Back 6-7 Top-Left; Pin 1 Bottom-Right","0.5","100","6","0","0","0","0","0"],
            ]

        and:
            List<List<String>> expectedComponents = [
                ["EComponent","0","1","1","36","24.89","21.64","45","0.5","6","100","C1","100nF 6.3V 0402/CAP_0402","0"],
                ["EComponent","1","2","1","1001","21.3","35.07","90","0.5","6","100","U1","/MAX14851","50"],
                ["EComponent","2","3","1","1001","21.5","19.5","157.5","0.5","6","100","U2","/MAX14851","50"],
                ["EComponent","3","4","1","1002","16","45","90","0.5","6","100","U3","/CAT24C32WI-GT3","0"],
                ["EComponent","4","5","1","1","14.44","13.9","0","0.5","6","100","R1","10K 0402 1%/RES_0402","50"],
                ["EComponent","5","6","1","1","15.72","25.2","-90","0.5","6","100","R2","10K 0402 1%/RES_0402","50"],
                ["EComponent","6","7","1","33","50.83","23.97","0","3.5","15","100","J1","/Micro USB Socket With Very Lon","0"],
            ]

        and:
            List<List<String>> expectedTrays = [
                ["ICTray","0","1001","205.07","61.05","277.1","61.11","4","1","0"],
                ["ICTray","1","1002","327.5","58.57","351.51","58.57","2","1","0"],
            ]

        and:
            List<List<String>> expectedFeederSummary = [
                ['1','2','2','[R1, R2]','[id:1, note:Cheap]','[name:10K 0402 1%/RES_0402, aliases:[]]'],
                ['33','0','0','[]','[id:33, note:Special]','[name:Micro USB Socket With Very Long Name, aliases:[]]'],
                ['36','1','1','[C1]','[id:36, note:Expensive]','[name:100nF 6.3V 0402/CAP_0402, aliases:[]]'],
                ['1001','2','2','[U1, U2]','[tray:B-1-4-TL, note:Back 1-4 Top-Left, Pin 1 Top-Left]','[name:MAX14851, aliases:[]]'],
                ['1002','1','1','[U3]','[tray:B-6-7-TL, note:Back 6-7 Top-Left, Pin 1 Bottom-Right]','[name:CAT24C32WI-GT3, aliases:[]]']
            ]

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            materialsPresent(content, expectedMaterials)
            componentsPresent(content, expectedComponents)
            traysPresent(content, expectedTrays)
            defaultPanelPresent(content)

        and:
            String capturedOutput = capture.toString()
            !capturedOutput.empty
            feederSummaryPresent(capturedOutput, expectedFeederSummary)
    }

    @Ignore
    def 'placement with unknown component'() {
        expect:
            false
    }

    @Ignore
    def 'materials should be sorted by feederId before assigning ids'() {
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

    @Ignore
    def 'trays should be sorted by id'() {
        expect:
            false
    }

    @Ignore
    def 'error should be generated if no more tray ids are available when assigning IDs to trays'() {
        expect:
            false
    }

    @Unroll
    def 'placement angle - #designAngle, #pickAngle, #feederAngle'(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle, BigDecimal expectedMachineAngle) {

        /*
            Design, Feeder, Pick angles are positive clockwise, 0 to 360.
            DipTrace EDA angles are negative clockwise, 0 to 360.  Also, Diptrace UI allows you to setting a component angle of -90, but when component is re-insepected in Diptrace UI the angle is converted to 270.
            Machine angles are negative clockwise, -180 to +180.

            Design angle = angle after conversion from EDA angle, this happens when component placements are loaded.
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
            270         | 0         | 270          | 0
            0           | 0         | 270          | -90
            90          | 0         | 270          | 180
            180         | 0         | 90           | -90
            270         | 0         | 90           | 180
            0           | 0         | 90           | 90
            90          | 0         | 90           | 0
            45          | 0         | 270          | -135
            315         | 0         | 270          | -45
            0           | 90        | 270          | 180
            90          | 90        | 270          | 90
    }

    private DPVGenerator buildGenerator() {
        DPVGenerator generator = new DPVGenerator(
                machine: new TestMachine(),
                dpvHeader: dpvHeader,
                placements: componentPlacements,
                components: components,
                feeders: feeders,
                optionalPanel: Optional.empty(),
                optionalFiducials: Optional.empty(),
                offsetZ: 0,
        )
        generator
    }

    static final int MATERIAL_COLUMN_COUNT = 15
    static final int COMPONENT_COLUMN_COUNT = 14
    static final int TRAY_COLUMN_COUNT = 10
    static final int FEEDER_SUMMARY_COLUMN_COUNT = 6

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

    void traysPresent(String content, List<List<String>> trayRows) {
        assert content.contains("Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start")
        trayRows.each { List<String> trayRow ->
            assert (trayRow.size() == TRAY_COLUMN_COUNT)
            String row = trayRow.join(",")
            assert content.contains(row)
        }
    }

    void defaultPanelPresent(String content) {
        assert content.contains(
            "Table,No.,ID,DeltX,DeltY" + TEST_TABLE_LINE_ENDING +
            "Panel_Coord,0,1,0,0" + TEST_TABLE_LINE_ENDING
        )
    }

    void panelArrayPresent(String content, Panel panel) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        assert content.contains(
            "Table,No.,ID,IntervalX,IntervalY,NumX,NumY" + TEST_TABLE_LINE_ENDING +
            "Panel_Array,0,1,${twoDigitDecimalFormat.format(panel.intervalX)},${twoDigitDecimalFormat.format(panel.intervalY)},${panel.numberX},${panel.numberY}" + TEST_TABLE_LINE_ENDING
        )
    }


    void feederSummaryPresent(String content, List<List<String>> feederSummaryRows) {
        assert content.contains('feederSummary:')
        assert content.contains('feederId,componentsPerUnit,componentsPerPanel,refdes,feeder,component')

        feederSummaryRows.each { List<String> feederSummaryRow ->
            assert (feederSummaryRow.size() == FEEDER_SUMMARY_COLUMN_COUNT)
            String row = feederSummaryRow.join(",")
            assert content.contains(row)
        }
    }


    def 'generate default panel'() {
        given:
            DPVGenerator generator = buildGenerator()

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content.contains("PANELYPE,0")

        and:
            defaultPanelPresent(content)
    }

    def 'generate array panel'() {
        given:
            // reference: https://github.com/sparkfunX/Desktop-PickAndPlace-CHMT36VA/blob/master/Eagle-Conversion/ConvertToCharm.ulp#L469-L498

            DPVGenerator generator = buildGenerator()
            Panel panel = new Panel(intervalX: 1.501, intervalY: 2.759, numberX: 3, numberY: 4)
            generator.optionalPanel = Optional.of(panel)

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content.contains("PANELYPE,1")

        and:
            panelArrayPresent(content, panel)
    }

    def 'generate fiducial markers'() {
        given:
            DPVGenerator generator = buildGenerator()
            List<Fiducial> fiducialList = [
                new Fiducial(note: "Mark1", coordinate: new Coordinate(x: 10, y: 3)),
                new Fiducial(note: "Mark2", coordinate: new Coordinate(x: 90, y: 97)),
            ]

            generator.optionalFiducials = Optional.of(fiducialList)

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content.contains(
                "Table,No.,nType,nAlg,nFinished" + TEST_TABLE_LINE_ENDING +
                "PcbCalib,0,1,0,0" + TEST_TABLE_LINE_ENDING
            )

        and:
            content.contains(
                "Table,No.,ID,offsetX,offsetY,Note" + TEST_TABLE_LINE_ENDING +
                "CalibPoint,0,1,10,3,Mark1" + CRLF +
                "CalibPoint,1,2,90,97,Mark2" + CRLF
            )

    }

    private class TestMachine extends Machine {

        Range trayIds = 1001..1009

        FeederProperties leftFeederProperties = new FeederProperties(
            feederAngle: 90
        )

        FeederProperties rightFeederProperties = new FeederProperties(
            feederAngle: 270
        )

        FeederProperties trayFeederProperties = new FeederProperties(
            feederAngle: 0
        )

        @Override
        FeederProperties feederProperties(Integer id) {
            if (id >= 1 && id <= 35) {
                return leftFeederProperties
            }
            if (id >= 36 && id <= 70) {
                return rightFeederProperties
            }
            if (trayIds.contains(id)) {
                return trayFeederProperties
            }

            return defaultFeederProperties
        }
    }
}

