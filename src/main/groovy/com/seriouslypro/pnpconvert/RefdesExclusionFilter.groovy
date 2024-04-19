package com.seriouslypro.pnpconvert

class RefdesExclusionFilter implements ComponentPlacementFilter {
    Set<String> refdesExclusions

    final boolean INCLUDE = true
    final boolean EXCLUDE = false

    @Override
    boolean shouldFilter(ComponentPlacement componentPlacement) {
        if (refdesExclusions.contains(componentPlacement.refdes)) {
            return EXCLUDE
        }
        return INCLUDE
    }
}
