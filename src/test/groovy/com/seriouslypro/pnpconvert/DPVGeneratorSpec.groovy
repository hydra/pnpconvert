package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine
import spock.lang.Ignore
import spock.lang.Specification
import org.springframework.boot.test.OutputCapture

// reference: https://github.com/sparkfunX/Desktop-PickAndPlace-CHMT36VA/blob/master/Eagle-Conversion/ConvertToCharm.ulp#L469-L498

class DPVGeneratorSpec extends Specification implements DPVFileAssertions {

    @org.junit.Rule
    OutputCapture capture = new OutputCapture()

    DPVHeader dpvHeader = new DPVHeader(
            fileName: "TEST-FILE",
            pcbFileName: "TEST-PCB-FILE"
    )

    List<PlacementMapping> placementMappings
    Feeders feeders

    OutputStream outputStream

    void setup() {
        placementMappings = []
        feeders = new Feeders()

        outputStream = new ByteArrayOutputStream()

        System.out.flush()
    }

    def 'generate empty dpv'() {
        given:
            DPVGenerator generator = buildGenerator()

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content
    }

    def 'generate'() {
        // This test is testing too much now.  Create separate tests for the following.
        // * Feeder/Placement/Component Content to DPV file content.
        // * Material selection
        // * Material assignment

        given:
            List<Component> components = [
                new Component(
                    description: "10K 0402 1%/RES_0402",
                    width: 0.01,
                    length: 30,
                    partCode: "R10K00402",
                    manufacturer: "RM1",
                ),
                new Component(
                    description: "100nF 6.3V 0402/CAP_0402",
                    partCode: "C10404026V3",
                    manufacturer: "CM1",
                ),
                new Component(
                    description: "MAX14851",
                    partCode: "MAX14851",
                    manufacturer: "UM1",
                ),
                new Component(
                    description: "RJ45CN",
                    partCode: "CAT24C32WI-GT3",
                    manufacturer: "JM1",
                ),
                new Component(
                    description: "Micro USB Socket With Very Long Name",
                    partCode: "MUSBSWVLN",
                    manufacturer: "JM2",
                    width: 8,
                    height: 3.5,
                    length: 5,

                    // use zero second fraction digit to aid matching rotation calculations (note: easy to see 0.1 + 0.02 = 0.12)
                    // use offsets that will result it the first digit of the placement being different for X/Y
                    placementOffsetX: 1.10,
                    placementOffsetY: 0.80,
                )
            ]

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

            feeders.loadFeeder(feeders.createReelFeeder(1, 8, components[0].partCode, components[0].manufacturer, components[0].description, slowPickSettings, "Cheap"))
            feeders.loadFeeder(feeders.createReelFeeder(33, 12, components[4].partCode, components[4].manufacturer, components[4].description, new PickSettings(separateMount: true, takeHeight: 2), "Special"))
            feeders.loadFeeder(feeders.createReelFeeder(36, 8, components[1].partCode, components[1].manufacturer, components[1].description, fastPickSettings, "Expensive"))

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

            feeders.loadFeeder(feeders.createTrayFeeder(tray1, components[2].partCode, components[2].manufacturer, components[2].description, slowPickSettings, "Back 1-4 Top-Left, Pin 1 Top-Left"))
            feeders.loadFeeder(feeders.createTrayFeeder(tray2, components[3].partCode, components[3].manufacturer, components[3].description, fastPickSettings,"Back 6-7 Top-Left, Pin 1 Bottom-Right"))

        and:
            List<ComponentPlacement> componentPlacements = [
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
                    name: "RJ45CN"
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
        and:
            placementMappings = [
                new PlacementMapping(
                    placement: componentPlacements[0],
                    component: Optional.of(components[0]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[1],
                    component: Optional.of(components[0]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[2],
                    component: Optional.of(components[1]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[3],
                    component: Optional.of(components[2]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[4],
                    component: Optional.of(components[2]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[5],
                    component: Optional.of(components[3]),
                ),
                new PlacementMapping(
                    placement: componentPlacements[6],
                    component: Optional.of(components[4]),
                ),
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
            boolean havePlacementThatUsesComponentWithPlacementOffset = components.find { it.description == nameOfComponentWithPlacementOffset && it.placementOffsetX != 0 && it.placementOffsetY != 0 && it.placementOffsetX != it.placementOffsetY}
            assert haveComponentWithPlacementOffset && havePlacementThatUsesComponentWithPlacementOffset
        and:
            DPVGenerator generator = buildGenerator()

        and:
            List<List<String>> expectedMaterials = [
                ["Station","0","1","0","0","4","R10K00402;RM1;10K 0402 1%/RES_0402;Cheap","0.5","100","6","1","3000","0","25","0"],
                ["Station","1","33","0","0","4","MUSBSWVLN;JM2;Micro USB Socket With Very Long Name;Special","3.5","100","14","800","500","200","0","0"],
                ["Station","2","36","0","0","4","C10404026V3;CM1;100nF 6.3V 0402/CAP_0402;Expensive","0.5","100","6","0","0","0","0","0"],
                ["Station","3","1001","0","0","4","MAX14851;UM1;MAX14851;Back 1-4 Top-Left; Pin 1 Top-Left","0.5","100","6","0","0","0","25","0"],
                ["Station","4","1002","0","0","4","CAT24C32WI-GT3;JM1;RJ45CN;Back 6-7 Top-Left; Pin 1 Bottom-Right","0.5","100","6","0","0","0","0","0"],
            ]

        and:
            List<List<String>> expectedComponents = [
                ["EComponent","0","1","1","36","24.89","21.64","45","0.5","6","100","C1","100nF 6.3V 0402/CAP_0402;100nF ","0"],
                ["EComponent","1","2","1","1001","21.3","35.07","90","0.5","6","100","U1","MAX14851;","50"],
                ["EComponent","2","3","1","1001","21.5","19.5","157.5","0.5","6","100","U2","MAX14851;","50"],
                ["EComponent","3","4","1","1002","16","45","90","0.5","6","100","U3","RJ45CN;","0"],
                ["EComponent","4","5","1","1","14.44","13.9","0","0.5","6","100","R1","10K 0402 1%/RES_0402;10K 0402 1","50"],
                ["EComponent","5","6","1","1","15.72","25.2","-90","0.5","6","100","R2","10K 0402 1%/RES_0402;10K 0402 1","50"],
                ["EComponent","6","7","1","33","50.83","23.97","0","3.5","15","100","J1","Micro USB Socket With Very Long","0"],
            ]

        and:
            List<List<String>> expectedTrays = [
                ["ICTray","0","1001","205.07","61.05","277.1","61.11","4","1","0"],
                ["ICTray","1","1002","327.5","58.57","351.51","58.57","2","1","0"],
            ]

        and:
            List<List<String>> expectedFeederSummary = [
                ['1','2','2','[R1, R2]','[id:1, note:Cheap]','[partCode:R10K00402, manufacturer:RM1, name:10K 0402 1%/RES_0402]'],
                ['33','0','0','[]','[id:33, note:Special]','[partCode:MUSBSWVLN, manufacturer:JM2, name:Micro USB Socket With Very Long Name]'],
                ['36','1','1','[C1]','[id:36, note:Expensive]','[partCode:C10404026V3, manufacturer:CM1, name:100nF 6.3V 0402/CAP_0402]'],
                ['1001','2','2','[U1, U2]','[tray:B-1-4-TL, note:Back 1-4 Top-Left, Pin 1 Top-Left]','[partCode:MAX14851, manufacturer:UM1, name:MAX14851]'],
                ['1002','1','1','[U3]','[tray:B-6-7-TL, note:Back 6-7 Top-Left, Pin 1 Bottom-Right]','[partCode:CAT24C32WI-GT3, manufacturer:JM1, name:RJ45CN]']
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

    def 'placement with unmapped component'() {
        given:
            DPVGenerator generator = buildGenerator()

        and:
            ComponentPlacement componentPlacement = new ComponentPlacement(
                refdes: "R1",
                pattern: "RES_0402_HIGH_DENSITY",
                coordinate: new Coordinate(x: 14.44, y: 13.9),
                side: PCBSide.TOP,
                rotation: 90,
                value: "10K 0402 1%",
                name: "RES_0402"
            )

        and:
            PlacementMapping placementMapping = new PlacementMapping(
                placement: componentPlacement,
                component: Optional.empty(),
            )

        and:
            generator.placementMappings = [ placementMapping ]

        when:
            generator.generate(outputStream)

        then:
            generator.unmappedPlacements.contains(placementMapping)
    }

    @Ignore
    def 'materials should be sorted by feederId before assigning ids'() {
        expect:
            false
    }

    def 'placement with unloaded component'() {
        given:
            DPVGenerator generator = buildGenerator()

        and:
            ComponentPlacement componentPlacement = new ComponentPlacement()
            Component component = new Component(description: "Component 1")

            generator.placementMappings = [
                new PlacementMapping(
                    placement: componentPlacement,
                    component: Optional.of(component),
                )
            ]

        and:
            Feeders mockFeeders = GroovyMock(Feeders)
            generator.feeders = mockFeeders

        when:
            generator.generate(outputStream)

        then:
            1 * mockFeeders.findByComponent(component) >> null
            0 * _

        and:
            generator.unloadedComponents.contains(component)
    }

    @Ignore
    def 'error should be generated if no more tray ids are available when assigning IDs to trays'() {
        expect:
            false
    }

    private DPVGenerator buildGenerator() {
        DPVGenerator generator = new DPVGenerator(
                machine: new TestMachine(),
                dpvHeader: dpvHeader,
                placementMappings: placementMappings,
                feeders: feeders,
                optionalPanel: Optional.empty(),
                optionalFiducials: Optional.empty(),
                offsetZ: 0,
        )
        generator
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

