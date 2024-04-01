package com.seriouslypro.pnpconvert

import com.seriouslypro.util.ascii.TreeNode
import com.seriouslypro.util.ascii.TreePrinter

class MaterialsReporter {

    TreeNode<String> mappedPlacementsRoot
    TreeNode<String> placementMappingNode

    TreeNode<String> buildMappedPlacementsRoot() {
        mappedPlacementsRoot = new TreeNode("mappedPlacements:")
    }

    void addPlacementComponent(Component c) {
        TreeNode<String> componentNode = new TreeNode(buildComponentSummary(c))
        placementMappingNode.addChild(componentNode)

    }

    void addPlacementMapping(PlacementMapping pm) {
        placementMappingNode = new TreeNode(buildPlacementSummary(pm))
    }

    void addCurrentPlacement() {
        mappedPlacementsRoot.addChild(placementMappingNode)
    }

    void report(MaterialsSelectionsResult result) {
        System.out.println()
        List<String> lines = new TreePrinter().print(mappedPlacementsRoot)
        lines.each { line -> System.out.println(line) }

        System.out.println()
        boolean showIssues = !result.unmappedPlacements.empty || !result.unloadedPlacements.empty
        if (showIssues) {
            System.out.println()
            System.out.println('*** ISSUES ***')
            System.out.println('')
        }

        if (!result.unmappedPlacements.empty) {
            System.out.println()
            dumpPlacementMappings("unmappedPlacements:", result.unmappedPlacements)
        }

        if (!result.unloadedPlacements.empty) {
            System.out.println()
            dumpPlacementMappings("unloadedComponents:", result.unloadedPlacements)
        }
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
}
