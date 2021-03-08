package com.seriouslypro.pnpconvert

class AlwaysAliasMatchingStrategy implements MatchingStrategy {
    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        return true
    }

    @Override
    boolean matches(Component candidate, String name) {
        return true
    }

    @Override
    boolean isExactMatch() {
        return false
    }
}
