package com.seriouslypro.pnpconvert

import spock.lang.Specification

class FeedersSpec extends Specification {

    private static final String TEST_COMPONENT_NAME = "TEST-COMPONENT"
    public static final String TEST_FEEDERS_RESOURCE = "/feeders1.csv"

    Feeders feeders


    private static Tray testTray = new Tray(
        name: "B-1-4-TL",
        firstComponentX: 205.07G, firstComponentY: 61.05G,
        lastComponentX: 277.1G, lastComponentY: 61.11G,
        columns: 4,
        rows: 1,
        firstComponentIndex: 0
    )

    Trays mockTrays

    void setup() {
        mockTrays = Mock()
        feeders = new Feeders(trays: mockTrays)
    }

    def 'find by component - no components'() {
        expect:
            feeders.findByComponent(Mock(Component)) == null
    }

    def 'find by component - matching component'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME)
            PickSettings mockPickSettings = Mock()
            FeederProperties mockFeederProperties = Mock()

            feeders.loadReel(1, 8, TEST_COMPONENT_NAME, mockPickSettings, "TEST-NOTE", mockFeederProperties)

        when:
            FeederMapping result = feeders.findByComponent(component)

        then:
            result.id == 1
            result.feeder
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_FEEDERS_RESOURCE)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            Integer feeder1Id = 36

            PickSettings feeder1PickSettings = new PickSettings(
                tapeSpacing: 2,
                xOffset: -0.07G,
                yOffset: 0.35G,
                packageAngle: 0,
                head: 1,
                useVision: true,
                checkVacuum: false,
                placeSpeedPercentage: 100,
                placeDelay: 50,
                takeHeight: 1,
                pullSpeed: 50,
            )

            FeederProperties feeder1Properties = feeders.machine.feederProperties(feeder1Id)

            Feeder feeder1 = new ReelFeeder(
                enabled: false,
                componentName: "10K 0402 1%/RES_0402",
                note: "RH",
                pickSettings: feeder1PickSettings,
                tapeWidth: 8,
                properties: feeder1Properties,
            )
        and:
            Integer feeder2Id = 27

            PickSettings feeder2PickSettings = new PickSettings(
                tapeSpacing: 12,
                xOffset: 0G,
                yOffset: 0G,
                packageAngle: 270,
                head: 2,
                useVision: false,
                checkVacuum: true,
                placeSpeedPercentage: 50,
                placeDelay: 25,
                takeHeight: 2.5G,
                pullSpeed: 25,
            )

            FeederProperties feeder2Properties = feeders.machine.feederProperties(feeder2Id)

            Feeder feeder2 = new ReelFeeder(
                enabled: true,
                componentName: "MicroUSB/001-01-0x06x",
                note: "LH",
                pickSettings: feeder2PickSettings,
                tapeWidth: 16,
                properties: feeder2Properties,
            )

        and:
            Integer feeder3Id = 91

            PickSettings feeder3PickSettings = new PickSettings(
                xOffset: 0G,
                yOffset: 0G,
                packageAngle: 0,
                head: 2,
                useVision: true,
                checkVacuum: true,
                placeSpeedPercentage: 33,
                placeDelay: 66,
                takeHeight: 3,
            )

            FeederProperties feeder3Properties = feeders.machine.feederProperties(feeder3Id)

            Feeder feeder3 = new TrayFeeder(
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
                componentName: "MAX14851",
                note: "Back 1-4 Top-Left",
                pickSettings: feeder3PickSettings,
                properties: feeder3Properties
            )

            Map<Integer, Feeder> expectedFeederMap = [
                (feeder1Id): feeder1,
                (feeder2Id): feeder2,
                (feeder3Id): feeder3,
            ]

        and:
            allPropertiesDifferent(PickSettings, feeder1PickSettings, feeder2PickSettings)
            allPropertiesDifferent(ReelFeeder, feeder1, feeder2)

        when:
            feeders.loadFromCSV(TEST_FEEDERS_RESOURCE, inputStreamReader)

        then:
            1 * mockTrays.findByName("B-1-4-TL") >> testTray
            0 * _

        and:
            feeders.feederMap.sort() == expectedFeederMap.sort()
    }

    void allPropertiesDifferent(Class aClass, Object a, Object b) {
        aClass.getDeclaredFields().findAll{ field ->
            !field.name.contains('$') && field.name != "metaClass" // filter out all the groovy magic
        }.each { field ->
            assert a.getProperty(field.name) != b.getProperty(field.name)
        }
    }
}
