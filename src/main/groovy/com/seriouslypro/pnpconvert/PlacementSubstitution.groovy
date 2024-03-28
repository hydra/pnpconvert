package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartSubstitution
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PlacementSubstitution {
    ComponentPlacement placement

    Optional<PartSubstitution> appliedSubstitution = Optional.empty()
    Optional<ComponentPlacement> originalPlacement = Optional.empty()

    List<String> errors = []
}
