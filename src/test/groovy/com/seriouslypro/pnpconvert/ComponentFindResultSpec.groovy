package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class ComponentFindResultSpec extends Specification {

    @Unroll
    def 'exact match - #scenario'() {
        given:
            ComponentFindResult componentFindResult = new ComponentFindResult(
                component: null,
                matchingStrategies: matchingStrategies
            )

        when:
            boolean isExactMatch = componentFindResult.isExactMatch()

        then:
            isExactMatch == expectedResult

        where:
            scenario | matchingStrategies | expectedResult
            "1" | [new AlwaysExactMatchingStrategy()] | true
            "2" | [new AlwaysAliasMatchingStrategy()] | false
            "3" | [new AlwaysExactMatchingStrategy(), new AlwaysAliasMatchingStrategy()] | false
    }
}
