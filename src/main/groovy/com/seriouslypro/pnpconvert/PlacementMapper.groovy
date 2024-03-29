package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.diptrace.placement.DipTracePartMapper
import com.seriouslypro.eda.part.PartMapping
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class PlacementMapper {
    List<PlacementMapping> map(List<ComponentPlacement> placements, List<Component> components, List<PartMapping> partMappings) {

        DipTracePartMapper mapper = new DipTracePartMapper()

        List<PlacementMapping> placementMappings = placements.findResults { placement ->


            PlacementMapping mappedPlacement = new PlacementMapping(placement: placement)

            List<PartMapping> applicableMappings = mapper.buildOptions(partMappings, placement.name, placement.value)

            List<Optional<PartMapping>> workList = applicableMappings.collect { Optional.of(it) }

            if (!applicableMappings) {
                // only search for placement's original part code and manufacturer
                workList.add(Optional.empty())
            }

            workList.each {optionalMapping ->
                ComponentCriteria componentCriteria = null // keep clover happy

                if (optionalMapping.isPresent()) {
                    optionalMapping.ifPresent {selectedMapping->
                        componentCriteria = new ComponentCriteria(
                            partCode: selectedMapping.partCode,
                            manufacturer: selectedMapping.manufacturer,
                        )
                    }
                } else {
                    componentCriteria = new ComponentCriteria(
                        partCode: placement.partCode,
                        manufacturer: placement.manufacturer,
                    )
                }

                List<Optional<Component>> applicableComponents = findComponents(components, componentCriteria).collect { Optional.of(it) }
                if (!applicableComponents) {
                    applicableComponents.add(Optional.empty())
                    if (!optionalMapping.isPresent()) {
                        // processing the placement itself
                        mappedPlacement.errors << "no matching components, check part code and manufacturer is correct, check or add components, use refdes replacements or part mappings"
                    }
                }
                applicableComponents.each {optionalComponent ->

                    MappingResult mappingResult = new MappingResult(
                        criteria: componentCriteria,
                        partMapping: optionalMapping,
                        component: optionalComponent
                    )

                    mappedPlacement.mappingResults << mappingResult
                }
                if (applicableComponents.size() > 1) {
                    mappedPlacement.errors << "multiple matching components found, check for component duplicates."
                }
            }

            mappedPlacement
        }

        placementMappings
    }

    // TODO add tests for case insensitive manufacturer comparison, also this method doesn't belong here now
    List<Component> findComponents(List<Component> components, ComponentCriteria criteria) {
        components.findAll { candidate ->
            candidate.partCode && criteria.partCode &&
                candidate.manufacturer && criteria.manufacturer &&
                candidate.partCode == criteria.partCode &&
                candidate.manufacturer.toLowerCase() == criteria.manufacturer.toLowerCase()
        }
    }
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class ComponentCriteria {
    String partCode
    String manufacturer

    boolean matches(String partCode, String manufacturer) {
        this.manufacturer && manufacturer && this.manufacturer.toLowerCase() == manufacturer.toLowerCase() &&
            this.partCode && partCode && this.partCode == partCode
    }

    String toSummary() {
        "[part code:${partCode}, manufacturer:${manufacturer}]"
    }
}