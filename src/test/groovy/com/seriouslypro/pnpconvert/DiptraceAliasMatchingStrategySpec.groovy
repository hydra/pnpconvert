package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class DiptraceAliasMatchingStrategySpec extends Specification {

    @Unroll
    def 'match - #scenario - placementName: #placementName, placementValue: #placementValue'(String scenario, String placementName, String placementValue, String componentName, List<String> componentAliases, boolean expectedResult) {
        given:
            MatchingStrategy strategy = new DiptraceAliasMatchingStrategy()

        and:
            Component candidate = new Component(name: componentName, aliases: componentAliases)
            ComponentPlacement componentPlacement = new ComponentPlacement(name: placementName, value: placementValue)

        when:
            boolean result = strategy.matches(candidate, componentPlacement)

        then:
            result == expectedResult

        where:
            scenario                            | placementName | placementValue | componentName | componentAliases | expectedResult
            "null alias"                        | null          | null           | null          | [""]             | false // for robustness only, components and aliases should never be null or empty string
            "empty alias"                       | ""            | ""             | ""            | [""]             | false // for robustness only, components and aliases should never be null or empty string
            "no aliases"                        | "A"           | ""             | "A"           | []               | false
            "matching alias"                    | "A"           | ""             | "A1"          | ["A"]            | true
            "matching alias - multiple aliases" | "B"           | ""             | "B1"          | ["A", "B"]       | true
    }
}
