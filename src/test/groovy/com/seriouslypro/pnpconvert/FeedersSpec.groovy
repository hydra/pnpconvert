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
}
