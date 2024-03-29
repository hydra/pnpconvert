package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping

class MaterialSelector {
    Set<Component> unloadedComponents = []
    Set<PlacementMapping> unmappedPlacements = []

    Map<ComponentPlacement, MaterialSelectionEntry> selectMaterials(List<ComponentPlacement> placements, List<Component> components, List<PartMapping> partMappings, List<Feeder> feeders) {

        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        List<PlacementMapping> placementMappings = new PlacementMapper().map(placements, components, partMappings)
        System.out.println()
        System.out.println("placement component mappings:")
        placementMappings.each { PlacementMapping mappedPlacement ->
            def placementSummary = [
                refdes: mappedPlacement.placement.refdes,
                name: mappedPlacement.placement.name,
                value: mappedPlacement.placement.value,
            ]
            System.out.print("${placementSummary} -> ")

            ComponentCriteria componentCriteria = new ComponentCriteria(
                partCode: mappedPlacement.componentCriteria.partCode,
                manufacturer: mappedPlacement.componentCriteria.manufacturer,
            )

            System.out.print("${componentCriteria.toSummary()}")

            mappedPlacement.partMapping.ifPresent { partMapping ->
                def partMappingSummary = [
                    'name pattern': partMapping.namePattern,
                    'value pattern': partMapping.valuePattern,
                ]
                System.out.print(" <- ${partMappingSummary}'")
            }

            System.out.println()
            if (mappedPlacement.errors) {
                mappedPlacement.errors.eachWithIndex { error, i ->
                    String indentation = '' // add unused assignment to prevent 'EmptyExpression.INSTANCE is immutable' error with clover
                    if (i == mappedPlacement.errors.size() - 1) {
                        indentation = '└── '
                    } else {
                        indentation = '├── '
                    }
                    System.out.println(indentation + "error: ${error}")
                }
            }

            if (!mappedPlacement.component.isPresent()) {
                unmappedPlacements << mappedPlacement
                return
            }

            Component component = mappedPlacement.component.get()
            Feeder feeder = findFeederByComponent(feeders, componentCriteria)
            if (!feeder) {
                unloadedComponents << component
                return
            }

            MaterialSelectionEntry materialSelection = new MaterialSelectionEntry(
                component: component,
                feeder: feeder,
            )

            materialSelections[mappedPlacement.placement] = materialSelection
        }

        System.out.println()
        boolean showIssues = !unmappedPlacements.empty || !unloadedComponents.empty
        if (showIssues) {
            System.out.println()
            System.out.println('*** ISSUES ***')
            System.out.println('')
        }

        if (!unmappedPlacements.empty) {
            System.out.println()
            System.out.println("unmappedPlacements:\n" + unmappedPlacements.collect { PlacementMapping p -> [
                refdes: p.placement.refdes,
                name: p.placement.name,
                value: p.placement.value,
            ]}.join('\n'))
        }

        if (!unloadedComponents.empty) {
            System.out.println()
            System.out.println("unloadedComponents:\n" + unloadedComponents.collect {Component c -> [
                partCode: c.partCode,
                manufacturer: c.manufacturer,
                description: c.description,
            ]}.join('\n'))
        }

        return materialSelections
    }

    Feeder findFeederByComponent(List<Feeder> feeders, ComponentCriteria criteria) {
        return feeders.findResult { Feeder feeder ->
            criteria.matches(feeder.partCode, feeder.manufacturer) ? feeder : null
        }
    }
}
