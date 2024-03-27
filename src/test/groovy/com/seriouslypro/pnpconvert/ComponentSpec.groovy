package com.seriouslypro.pnpconvert

import spock.lang.Specification

class ComponentSpec extends Specification {

    def 'hasPartCodeAndManufacturer'() {

        expect:
            expectedResult == new Component(partCode: partCode, manufacturer: manufacturer).hasPartCodeAndManufacturer()

        where:
            partCode | manufacturer | expectedResult
            null     | null         | false
            null     | ""           | false
            ""       | null         | false
            ""       | ""           | false
            "A"      | ""           | false
            ""       | "B"          | false
            "A"      | null         | false
            null     | "B"          | false
            "A"      | "B"          | true
    }
}
