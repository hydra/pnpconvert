package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.util.ascii.TreeNode
import com.seriouslypro.util.ascii.TreePrinter

class MaterialSelector {
    Set<PlacementMapping> unloadedPlacements = []
    Set<PlacementMapping> unmappedPlacements = []
    TreeNode<String> mappedPlacementsRoot = new TreeNode("mappedPlacements:")

    Map<ComponentPlacement, MaterialSelectionEntry> selectMaterials(List<ComponentPlacement> placements, List<Component> components, List<PartMapping> partMappings, List<Feeder> feeders) {

        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        List<PlacementMapping> placementMappings = new PlacementMapper().map(placements, components, partMappings)
        placementMappings.each { PlacementMapping mappedPlacement ->
            TreeNode<String> placementMappingNode = new TreeNode(buildPlacementSummary(mappedPlacement))

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
                        materialSelections[mappedPlacement.placement] = materialSelection
                        selectedFeeder = Optional.of(f)

                        TreeNode<String> componentNode = new TreeNode(buildComponentSummary(c))
                        placementMappingNode.addChild(componentNode)

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
                unmappedPlacements << mappedPlacement
                return
            }

            if (!selectedFeeder.isPresent()) {
                unloadedPlacements << mappedPlacement
                return
            }

            if (selectedResult.isPresent()) {
                mappedPlacementsRoot.addChild(placementMappingNode)
            }

        }

        System.out.println()
        List<String> lines = new TreePrinter().print(mappedPlacementsRoot)
        lines.each { line -> System.out.println(line) }

        System.out.println()
        boolean showIssues = !unmappedPlacements.empty || !unloadedPlacements.empty
        if (showIssues) {
            System.out.println()
            System.out.println('*** ISSUES ***')
            System.out.println('')
        }

        if (!unmappedPlacements.empty) {
            System.out.println()
            dumpPlacementMappings("unmappedPlacements:", unmappedPlacements)
        }

        if (!unloadedPlacements.empty) {
            System.out.println()
            dumpPlacementMappings("unloadedComponents:", unloadedPlacements)
        }

        return materialSelections
    }

    private dumpPlacementMappings(String title, Set<PlacementMapping> mappings) {

        TreeNode<String> root = new TreeNode(title)

        mappings.each { pm ->
            TreeNode<String> pmNode = new TreeNode(buildPlacementSummary(pm))
            root.addChild(pmNode)

            pm.mappingResults.each { mr ->
                TreeNode<String> criteriaNode = new TreeNode("criteria " + mr.criteria.toSummary())
                pmNode.addChild(criteriaNode)

                mr.component.ifPresent { c ->
                    TreeNode<String> componentNode = new TreeNode(buildComponentSummary(c))
                    criteriaNode.addChild(componentNode)
                }
                int totalErrors = pm.errors.size()
                if (totalErrors > 0) {
                    TreeNode<String> errorTitleNode = new TreeNode("errors")
                    criteriaNode.addChild(errorTitleNode)
                    pm.errors.each { error ->
                        TreeNode<String> errorNode = new TreeNode(error)
                        errorTitleNode.addChild(errorNode)
                    }
                }
            }
        }

        List<String> lines = new TreePrinter().print(root)
        lines.each { line -> System.out.println(line) }
    }

    private String buildComponentSummary(Component c) {
        def componentSummary = [
            partCode    : c.partCode,
            manufacturer: c.manufacturer,
            description : c.description,
        ]
        "component ${componentSummary}"
    }

    private String buildPlacementSummary(PlacementMapping pm) {
        def placementSummary = [
            refdes: pm.placement.refdes,
            name  : pm.placement.name,
            value : pm.placement.value,
        ]
        "placement ${placementSummary}"
    }

    Optional<Feeder> findFeederByComponent(List<Feeder> feeders, ComponentCriteria criteria) {
        Feeder feeder = feeders.findResult { Feeder feeder ->
            criteria.matches(feeder.partCode, feeder.manufacturer) ? feeder : null
        }
        Optional.ofNullable(feeder)
    }
}
