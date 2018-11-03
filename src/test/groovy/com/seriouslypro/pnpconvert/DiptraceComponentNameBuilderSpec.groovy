package com.seriouslypro.pnpconvert

import spock.lang.Specification

class DiptraceComponentNameBuilderSpec extends Specification {

    def 'build name'() {
        given:
            ComponentPlacement componentPlacement = new ComponentPlacement(name: "TEST_NAME", value: "TEST_VALUE")

        expect:
            "TEST_VALUE/TEST_NAME" == new DiptraceComponentNameBuilder().buildDipTraceComponentName(componentPlacement)
    }
}
