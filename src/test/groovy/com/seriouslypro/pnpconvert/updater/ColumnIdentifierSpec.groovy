package com.seriouslypro.pnpconvert.updater

import spock.lang.Specification
import spock.lang.Unroll

class ColumnIdentifierSpec extends Specification {

    @Unroll
    def "valid value - #value = #expectedResult"(int value, String expectedResult) {
        expect:
            ColumnIdentifier.fromInt(value) == expectedResult

        where:
            value | expectedResult
            1 | 'A'
            26 | 'Z'
            27 | 'AA'
            52 | 'AZ'
            53 | 'BA'
    }

    @Unroll
    def "invalid value - #scenario"(String scenario, int value) {
        when:
            ColumnIdentifier.fromInt(value)

        then:
            Exception thrown = thrown()
            thrown instanceof IllegalArgumentException
            thrown.message == "Unable to convert value to Column identifier, value: ${value}"

        where:
            scenario   | value
            "zero"     | 0
            "negative" | -1
    }
}
