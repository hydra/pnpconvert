package com.seriouslypro.pnpconvert

import spock.lang.Specification

class ComponentCriteriaSpec extends Specification {
    def 'summary'() {
        expect:
            new ComponentCriteria(partCode: "A", manufacturer: "B").toSummary() == '[part code:A, manufacturer:B]'
    }
}
