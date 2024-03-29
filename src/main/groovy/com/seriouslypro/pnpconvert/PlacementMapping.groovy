package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PlacementMapping {
    ComponentPlacement placement

    Optional<Component> component = Optional.empty()
    Optional<PartMapping> partMapping = Optional.empty()

    ComponentCriteria componentCriteria
    List<String> errors = []
}