package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.diptrace.placement.DipTracePartMapper
import com.seriouslypro.eda.part.PartMapping

class PlacementMapper {
    List<PlacementMapping> map(List<ComponentPlacement> placements, List<Component> components, List<PartMapping> partMappings) {

        DipTracePartMapper mapper = new DipTracePartMapper()

        List<PlacementMapping> placementMappings = placements.findResults { placement ->

            String partCode = placement.partCode
            String manufacturer = placement.manufacturer

            PlacementMapping mappedPlacement = new PlacementMapping(placement: placement)

            List<PartMapping> applicableMappings = mapper.buildOptions(partMappings, placement.name, placement.value)
            if (applicableMappings.size() > 1) {
                mappedPlacement.errors << "multiple matching mappings found, be more specific with mappings or use refdes replacements. applicableMappings: '${applicableMappings}'".toString()
            } else if (applicableMappings.size() == 1) {
                PartMapping selectedMapping = applicableMappings.first()

                partCode = selectedMapping.partCode
                manufacturer = selectedMapping.manufacturer

                mappedPlacement.partMapping = Optional.of(selectedMapping)
            }

            List<Component> applicableComponents = findComponents(components, partCode, manufacturer)

            if (applicableComponents.size() == 0) {
                mappedPlacement.errors << "no matching components, check part code and manufacturer is correct, check or add components, use refdes replacements or part mappings"
            } else if (applicableComponents.size() > 1) {
                mappedPlacement.errors << "multiple matching components found, check for component duplicates. applicableComponents: '${applicableComponents}'".toString()
            } else {
                Component selectedComponent = applicableComponents.first()
                mappedPlacement.component = Optional.of(selectedComponent)
            }

            mappedPlacement
        }

        placementMappings
    }

    List<Component> findComponents(List<Component> components, partCode, manufacturer) {
        components.findAll { candidate ->
            candidate.partCode && partCode &&
                candidate.manufacturer && manufacturer &&
                candidate.partCode == partCode &&
                candidate.manufacturer == manufacturer
        }
    }
}