package com.seriouslypro.pnpconvert

import spock.lang.Specification

class RefdesExclusionFilterSpec extends Specification {

    def 'should filter'() {
        given:
            Set<String> exclusions = ['R1', 'R3']

        and:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1'),
                new ComponentPlacement(refdes: 'R2'),
                new ComponentPlacement(refdes: 'R3'),
            ]

        and:
            RefdesExclusionFilter filter = new RefdesExclusionFilter(refdesExclusions: exclusions)

        and:
            List<ComponentPlacement> expectedPlacements = [
                placements[1],
            ]

        when:
            List<ComponentPlacement> filteredPlacements = placements.findAll { filter.shouldFilter(it) }

        then:
            filteredPlacements == expectedPlacements
    }
}
