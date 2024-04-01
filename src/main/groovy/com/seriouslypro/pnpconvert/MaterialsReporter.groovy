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
        dumpTree(mappedPlacementsRoot)

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

            Map<Component, List<String>> componentsToRefdesMap = buildComponentsToRefdesMap(result.unloadedPlacements)

            System.out.println()
            dumpComponentsToRefdesMap("unloadedComponentsToRefDes:", componentsToRefdesMap)
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

        dumpTree(root)
    }

    private void dumpTree(TreeNode root) {
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

    Map<Component, List<String>> buildComponentsToRefdesMap(Set<PlacementMapping> placementMappings) {
        Map<Component, List<String>> map = [:]
        placementMappings.each {pm ->
            List<Component> components = pm.mappingResults.findResults {mr -> mr.component.orElse( null )}
            components.each { c ->
                if (!map.containsKey(c)) {
                    map[c] = []
                }
                map[c] << pm.placement.refdes
            }
        }
        map
    }

    void dumpComponentsToRefdesMap(String title, Map<Component, List<String>> map) {

        TreeNode<String> root = new TreeNode(title)
        map.each { c, refdesList ->
            TreeNode<String> componentNode = new TreeNode(buildComponentSummary(c))
            root.addChild(componentNode)
            TreeNode<String> refDesListNode = new TreeNode('refdesList: ' + refdesList.toString())
            componentNode.addChild(refDesListNode)
        }
        dumpTree(root)
    }
}
