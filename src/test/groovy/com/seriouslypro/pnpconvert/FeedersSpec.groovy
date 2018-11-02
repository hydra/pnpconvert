package com.seriouslypro.pnpconvert

import spock.lang.Specification

class FeedersSpec extends Specification {

    private static final String TEST_COMPONENT_NAME = "TEST-COMPONENT"

    Feeders feeders

    void setup() {
        feeders = new Feeders()
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
            InputStream inputStream = this.getClass().getResourceAsStream("/feeders1.csv")
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            Integer feeder1Id = 36

            PickSettings feeder1PickSettings = new PickSettings(
                tapeSpacing: 2,
                xOffset: -0.07G,
                yOffset: 0.35G,
                packageAngle: 0,
                head: 1,
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

            Map<Integer, Feeder> expectedFeederMap = [
                (feeder1Id): feeder1,
                (feeder2Id): feeder2,
            ]

        when:
            feeders.loadFromCSV(inputStreamReader)

        then:
            feeders.feederMap.sort() == expectedFeederMap.sort()
    }
}
