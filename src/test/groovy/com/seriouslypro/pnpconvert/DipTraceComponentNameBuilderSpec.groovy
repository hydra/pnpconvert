package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class DipTraceComponentNameBuilderSpec extends Specification {

    def 'build name'() {
        given:
            ComponentPlacement componentPlacement = new ComponentPlacement(name: "TEST_NAME", value: "TEST_VALUE")

        expect:
            "TEST_VALUE/TEST_NAME" == new DipTraceComponentNameBuilder().buildDipTraceComponentName(componentPlacement)
    }

    @Unroll
    def 'build component name - value: "#value"'() {
        given:
            ComponentPlacement componentPlacement = new ComponentPlacement(name: name, value: value)

        expect:
            "TEST_NAME" == new DipTraceComponentNameBuilder().buildDipTraceComponentName(componentPlacement)

        where:
            name        | value
            "TEST_NAME" | null
            "TEST_NAME" | ""
    }
}
