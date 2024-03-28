package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.diptrace.placement.DipTracePlacementMapper
import com.seriouslypro.eda.part.PartSubstitution

class ComponentPlacementSubstitutor {

    DipTracePlacementMapper mapper = new DipTracePlacementMapper()

    List<PlacementSubstitution> process(List<ComponentPlacement> componentPlacements, List<PartSubstitution> partSubstitutions) {
        List<PlacementSubstitution> results = componentPlacements.findResults { placement ->
            PlacementSubstitution placementSubstitution = new PlacementSubstitution(
                placement: placement,
            )

            List<PartSubstitution> applicableSubstitutions = mapper.buildOptions(partSubstitutions, placement.name, placement.value)
            if (applicableSubstitutions.size() > 1) {
                placementSubstitution.errors << "multiple matching substitutions found, be more specific with substitutions or use refdes replacements. applicableSubstitutions: '${applicableSubstitutions}'".toString()
                return placementSubstitution
            } else if (applicableSubstitutions.size() == 1) {
                PartSubstitution selectedSubstitution = applicableSubstitutions.first()

                placementSubstitution.originalPlacement = Optional.of(placement.clone()) as Optional<ComponentPlacement>

                placement.name = selectedSubstitution.name
                placement.value = selectedSubstitution.value

                placementSubstitution.appliedSubstitution = Optional.of(selectedSubstitution)
            }

            placementSubstitution
        }

        results
    }
}
