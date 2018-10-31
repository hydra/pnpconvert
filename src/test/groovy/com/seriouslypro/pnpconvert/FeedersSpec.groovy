package com.seriouslypro.pnpconvert

import spock.lang.Specification

class FeedersSpec extends Specification {

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
            Component mockComponent = Mock()
            PickSettings mockPickSettings = Mock()
            FeederProperties mockFeederProperties = Mock()

            feeders.loadReel(1, 8, mockComponent, mockPickSettings, "TEST-NOTE", mockFeederProperties)

        when:
            FeederMapping result = feeders.findByComponent(mockComponent)

        then:
            result.id == 1
            result.feeder
    }
}
