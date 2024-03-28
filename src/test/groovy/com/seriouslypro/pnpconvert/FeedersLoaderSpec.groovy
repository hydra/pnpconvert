package com.seriouslypro.pnpconvert

import spock.lang.Specification

class FeedersLoaderSpec extends Specification {

    private static final String TEST_COMPONENT_DESCRIPTION = "TEST-COMPONENT"
    private static final String TEST_COMPONENT_PART_CODE = "TEST-PART-CODE"
    private static final String TEST_COMPONENT_MANUFACTURER = "TEST-MANUFACTURER"
    public static final String TEST_FEEDERS_RESOURCE_1 = "/feeders1.csv"
    public static final String TEST_FEEDERS_RESOURCE_2 = "/feeders2.csv"
    public static final String TEST_FEEDERS_RESOURCE_3 = "/feeders3.csv"
    public static final String TEST_FEEDERS_RESOURCE_4 = "/feeders4.csv"

    FeedersLoader feedersLoader

    private static Tray testTray = new Tray(
        name: "B-1-4-TL",
        firstComponentX: 205.07G, firstComponentY: 61.05G,
        lastComponentX: 277.1G, lastComponentY: 61.11G,
        columns: 4,
        rows: 1,
        firstComponentIndex: 0
    )

    TraysLoader mockTrays

    void setup() {
        mockTrays = Mock()
        feedersLoader = new FeedersLoader(traysLoader: mockTrays)
    }

    def 'find by component - no components'() {
        expect:
            feedersLoader.findByComponent(new Component(description: TEST_COMPONENT_DESCRIPTION)) == null
    }

    def 'find by component - matching part & manufacturer'() {
        given:
            PickSettings mockPickSettings = Mock()

        and:
            Feeder feeder = feedersLoader.createReelFeeder(1, 8, TEST_COMPONENT_PART_CODE, TEST_COMPONENT_MANUFACTURER, "UNMATCHED_DESCRIPTION", mockPickSettings, "TEST-NOTE")
            feedersLoader.loadFeeder(feeder)

        when:
            Feeder result = feedersLoader.findByComponent(new Component(partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER, description: TEST_COMPONENT_DESCRIPTION))

        then:
            result
            result.fixedId.get() == 1
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_FEEDERS_RESOURCE_1)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            Integer feeder1Id = 36

            PickSettings feeder1PickSettings = new PickSettings(
                tapeSpacing: 2,
                xOffset: -0.07G,
                yOffset: 0.35G,
                packageAngle: 0,
                head: 1,
                separateMount: true,
                useVision: true,
                checkVacuum: false,
                placeSpeedPercentage: 100,
                placeDelay: 50,
                takeHeight: 1,
                takeDelay: 0.25G,
                pullSpeed: 50,
            )

            Feeder feeder1 = new ReelFeeder(
                fixedId: Optional.of(feeder1Id),
                enabled: false,
                description: "10K 0402 1%/RES_0402",
                partCode: "R10K0402",
                manufacturer: "RMFR1",
                note: "RH",
                pickSettings: feeder1PickSettings,
                tapeWidth: 8,
            )
        and:
            Integer feeder2Id = 27

            PickSettings feeder2PickSettings = new PickSettings(
                tapeSpacing: 12,
                xOffset: 0G,
                yOffset: 0G,
                packageAngle: 270,
                head: 2,
                separateMount: false,
                useVision: false,
                checkVacuum: true,
                placeSpeedPercentage: 50,
                placeDelay: 25,
                takeHeight: 2.5G,
                takeDelay: 0G,
                pullSpeed: 25,
            )

            Feeder feeder2 = new ReelFeeder(
                fixedId: Optional.of(feeder2Id),
                enabled: true,
                description: "MicroUSB/001-01-0x06x",
                partCode: "001-01-0x060",
                manufacturer: "USBCNMFR",
                note: "LH",
                pickSettings: feeder2PickSettings,
                tapeWidth: 16,
            )

        and:
            Integer feeder3Id = 91

            PickSettings feeder3PickSettings = new PickSettings(
                xOffset: 0G,
                yOffset: 0G,
                packageAngle: 0,
                head: 2,
                separateMount: true,
                useVision: true,
                checkVacuum: true,
                placeSpeedPercentage: 33,
                placeDelay: 66,
                takeDelay: 3G,
                takeHeight: 3,
            )

            Feeder feeder3 = new TrayFeeder(
                fixedId: Optional.of(feeder3Id),
                enabled: true,
                tray: new Tray(
                    name: "B-1-4-TL",
                    firstComponentX: 205.07G,
                    firstComponentY: 61.05G,
                    lastComponentX: 277.1G,
                    lastComponentY: 61.11G,
                    rows:1,
                    columns:4,
                    firstComponentIndex:0
                ),
                description: "MAX14851",
                note: "Back 1-4 Top-Left",
                pickSettings: feeder3PickSettings
            )

            ArrayList<Feeder> expectedFeederList = [
                feeder1,
                feeder2,
                feeder3,
            ]

        and:
            allPropertiesDifferent(PickSettings, feeder1PickSettings, feeder2PickSettings)
            allPropertiesDifferent(ReelFeeder, feeder1, feeder2)

        when:
            feedersLoader.loadFromCSV(TEST_FEEDERS_RESOURCE_1, inputStreamReader)

        then:
            1 * mockTrays.findByName("B-1-4-TL") >> testTray
            0 * _

        and:
            feedersLoader.csvParseExceptions.empty
            feedersLoader.feederList.sort() == expectedFeederList.sort()
    }

    def 'ignore rows that have ignore flag'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_FEEDERS_RESOURCE_2)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

            List<Feeder> expectedFeederList = []

        when:
            feedersLoader.loadFromCSV(TEST_FEEDERS_RESOURCE_2, inputStreamReader)

        then:
            1 * mockTrays.findByName('B-1-4-TL') >> testTray
            0 * _

        and:
            feedersLoader.csvParseExceptions.empty
            feedersLoader.feederList.empty
    }

    def 'allow rows with no fixed ID'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_FEEDERS_RESOURCE_3)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        when:
            feedersLoader.loadFromCSV(TEST_FEEDERS_RESOURCE_3, inputStreamReader)

        then:
            mockTrays.findByName('B-1-4-TL') >> testTray
            0 * _

        and:
            feedersLoader.csvParseExceptions.empty
            !feedersLoader.feederList.empty
    }

    def 'allow header aliases'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_FEEDERS_RESOURCE_4)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        when:
            feedersLoader.loadFromCSV(TEST_FEEDERS_RESOURCE_4, inputStreamReader)

        then:
            mockTrays.findByName('B-1-4-TL') >> testTray
            0 * _

        and:
            feedersLoader.csvParseExceptions.empty
            !feedersLoader.feederList.empty
    }

    void allPropertiesDifferent(Class aClass, Object a, Object b) {
        aClass.getDeclaredFields().findAll{ field ->
            !field.name.contains('$') && field.name != "metaClass" // filter out all the groovy magic
        }.each { field ->
            assert a.getProperty(field.name) != b.getProperty(field.name)
        }
    }
}
