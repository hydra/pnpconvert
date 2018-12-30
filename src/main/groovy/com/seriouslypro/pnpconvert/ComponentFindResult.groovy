package com.seriouslypro.pnpconvert

class ComponentFindResult {
    Component component
    List<MatchingStrategy> matchingStrategies

    boolean isExactMatch() {
        matchingStrategies.every { it.isExactMatch() }
    }
}
