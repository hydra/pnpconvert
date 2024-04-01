package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping

class MaterialsSelectionsResult {
    Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]
    Set<PlacementMapping> unloadedPlacements = []
    Set<PlacementMapping> unmappedPlacements = []
}

class MaterialsSelector {
    MaterialsSelectionsResult selectMaterials(List<ComponentPlacement> placements, List<Component> components, List<PartMapping> partMappings, List<Feeder> feeders, MaterialsReporter reporter) {
        MaterialsSelectionsResult result = new MaterialsSelectionsResult()

        reporter.buildMappedPlacementsRoot()

        List<PlacementMapping> placementMappings = new PlacementMapper().map(placements, components, partMappings)
        placementMappings.each { PlacementMapping mappedPlacement ->
            reporter.addPlacementMapping(mappedPlacement)


            int unmappedCount = 0
            Optional<Feeder> selectedFeeder = Optional.empty()
            Optional<MappingResult> selectedResult = Optional.ofNullable(mappedPlacement.mappingResults.findResult { mr ->

                MappingResult selectedResult = null
                if (mr.component.isPresent()) {
                    Component c = mr.component.get()

                    Optional<Feeder> of = findFeederByComponent(feeders, mr.criteria)

                    of.ifPresent {f ->
                        MaterialSelectionEntry materialSelection = new MaterialSelectionEntry(
                            component: c,
                            feeder: f,
                        )
                        result.materialSelections[mappedPlacement.placement] = materialSelection
                        selectedFeeder = Optional.of(f)

                        reporter.addPlacementComponent(c)

                        selectedResult = mr
                    }
                } else {
                    if (!mr.partMapping.isPresent()) {
                        unmappedCount += 1
                    }

                }

                selectedResult
            })

            if (unmappedCount > 0 && !selectedResult.isPresent()) {
                result.unmappedPlacements << mappedPlacement
                return
            }

            if (!selectedFeeder.isPresent()) {
                result.unloadedPlacements << mappedPlacement
                return
            }

            if (selectedResult.isPresent()) {
                reporter.addCurrentPlacement()
            }
        }

        result
    }

    Optional<Feeder> findFeederByComponent(List<Feeder> feeders, ComponentCriteria criteria) {
        Feeder feeder = feeders.findResult { Feeder feeder ->
            criteria.matches(feeder.partCode, feeder.manufacturer) ? feeder : null
        }
        Optional.ofNullable(feeder)
    }
}
