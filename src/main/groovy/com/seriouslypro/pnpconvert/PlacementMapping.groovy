package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class MappingResult {
    ComponentCriteria criteria
    Optional<PartMapping> partMapping
    Optional<Component> component
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PlacementMapping {
    ComponentPlacement placement
    List<MappingResult> mappingResults = []
    List<String> errors = []
}