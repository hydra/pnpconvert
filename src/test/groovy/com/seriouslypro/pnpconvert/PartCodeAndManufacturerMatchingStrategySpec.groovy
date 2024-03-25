package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class PartCodeAndManufacturerMatchingStrategySpec extends Specification {

    @Unroll
    def 'match - #scenario'() {
        given:
            MatchingStrategy strategy = new PartCodeAndManufacturerMatchingStrategy()

        and:
            Component candidate = new Component(partCode: componentPartCode, manufacturer: componentManufacturer)
            ComponentPlacement componentPlacement = new ComponentPlacement(partCode: placementPartCode, manufacturer: placementManufacturer)

        when:
            boolean result = strategy.matches(candidate, componentPlacement)

        then:
            result == expectedResult

        where:
            scenario                            | placementPartCode | placementManufacturer | componentPartCode | componentManufacturer | expectedResult
            "exact match"                       | "PC1"             | "M1"                  | "PC1"             | "M1"                  | true
            "part code match only"              | "PC1"             | "M1"                  | "PC1"             | "M2"                  | false
            "manufacturer match only"           | "PC1"             | "M1"                  | "PC2"             | "M1"                  | false
            "missing component details 1"       | "PC1"             | "M1"                  | null              | null                  | false
            "missing component details 2"       | "PC1"             | "M1"                  | "PC1"             | null                  | false
            "missing component details 3"       | "PC1"             | "M1"                  | null              | "M1"                  | false
            "missing placement details 1"       | null              | null                  | "PC1"             | "M1"                  | false
            "missing placement details 2"       | "PC1"             | null                  | "PC1"             | "M1"                  | false
            "missing placement details 3"       | null              | "M1"                  | "PC1"             | "M1"                  | false
            "null everywhere"                   | null              | null                  | null              | null                  | false // for robustness only, components and aliases should never be null or empty string
    }
}
