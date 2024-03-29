package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import spock.lang.Specification

class MaterialSelectorSpec extends Specification {

    MaterialSelector materialSelector

    private static final String TEST_COMPONENT_PART_CODE = "TEST-PART-CODE"
    private static final String TEST_COMPONENT_MANUFACTURER = "TEST-MANUFACTURER"

    def setup() {
        materialSelector = new MaterialSelector()
    }

    def 'find by component - no feeders, no components'() {
        expect:
            materialSelector.findFeederByComponent([], new ComponentCriteria()) == null
    }

    def 'find by component - matching part & manufacturer'() {
        given:
            PickSettings mockPickSettings = Mock()
            FeedersLoader feedersLoader = new FeedersLoader()

        and:
            Feeder feeder = FeedersHelper.createReelFeeder(1, 8, TEST_COMPONENT_PART_CODE, TEST_COMPONENT_MANUFACTURER, "UNMATCHED_DESCRIPTION", mockPickSettings, "TEST-NOTE")
            feedersLoader.feeders << feeder

        and:
            ComponentCriteria componentCriteria = new ComponentCriteria(partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER)
        when:
            Feeder result = materialSelector.findFeederByComponent(feedersLoader.feeders, componentCriteria)

        then:
            result
            result.fixedId.get() == 1
    }

    def 'placement with unmapped component'() {
        given:
            List<Feeder> feeders = []
            List<Component> components = []
            List<PartMapping> partMappings = []

        and:
            List<ComponentPlacement> componentPlacements = [
                new ComponentPlacement(
                    refdes: "R1",
                    pattern: "RES_0402_HIGH_DENSITY",
                    coordinate: new Coordinate(x: 14.44, y: 13.9),
                    side: PCBSide.TOP,
                    rotation: 90,
                    value: "10K 0402 1%",
                    name: "RES_0402"
                )
            ]

        and:
            List<PlacementMapping> expectedPlacementMappings = [
                new PlacementMapping(
                    placement: componentPlacements[0],
                    componentCriteria: new ComponentCriteria(partCode:null, manufacturer:null),
                    errors:[ "no matching components, check part code and manufacturer is correct, check or add components, use refdes replacements or part mappings" ],
                )
            ]

        when:
            materialSelector.selectMaterials(componentPlacements, components, partMappings, feeders)

        then:
            materialSelector.unmappedPlacements.containsAll(expectedPlacementMappings)
    }


    def 'placement with unloaded component'() {
        given:
            List<Feeder> feeders = []
            List<PartMapping> partMappings = []

        and:
            List<Component> components = [
                new Component(partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER),
            ]

        and:
            ComponentPlacement componentPlacement = new ComponentPlacement(partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER)
            List<ComponentPlacement> componentPlacements = [componentPlacement]

        when:
            materialSelector.selectMaterials(componentPlacements, components, partMappings, feeders)

        then:
            materialSelector.unloadedComponents.contains(components[0])
    }
}
