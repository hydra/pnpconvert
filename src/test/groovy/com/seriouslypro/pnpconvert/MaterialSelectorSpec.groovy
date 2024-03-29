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
            materialSelector.findFeederByComponent([], new ComponentCriteria()) == Optional.empty()
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
            Optional<Feeder> result = materialSelector.findFeederByComponent(feedersLoader.feeders, componentCriteria)

        then:
            result == Optional.of(feeder)
    }

    def 'unmapped placement'() {
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
                    mappingResults: [
                        new MappingResult(
                            criteria: new ComponentCriteria(partCode: null, manufacturer: null),
                            component: Optional.empty(),
                            partMapping: Optional.empty()
                        ),
                    ],
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
            ComponentPlacement componentPlacement = new ComponentPlacement(name: 'name', value: 'value', partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER)
            List<ComponentPlacement> componentPlacements = [componentPlacement]

        and:
            Set<PlacementMapping> expectedUnloadedPlacements = [
                new PlacementMapping(
                    placement: componentPlacements[0],
                    mappingResults: [
                        new MappingResult(
                            criteria: new ComponentCriteria(partCode: TEST_COMPONENT_PART_CODE, manufacturer: TEST_COMPONENT_MANUFACTURER),
                            component: Optional.of(components[0]),
                            partMapping: Optional.empty()
                        ),
                    ],
                )
            ]

        when:
            materialSelector.selectMaterials(componentPlacements, components, partMappings, feeders)

        then:
            materialSelector.unloadedPlacements == expectedUnloadedPlacements
    }

    /**
     * It's entirely plausible that someone would stock two different 10K 0402 reels from different manufacturers
     * and that either can be used without having to disable mappings and that any given time either or both reels could
     * be in a feeder.
     */
    // TODO other test scenarios: only one feeder loaded, no feeder loaded
    def 'placement with two matching part mappings'() {
        given:
            List<Feeder> feeders = [
                new Feeder(fixedId: Optional.of(1), partCode: 'PC1', manufacturer: 'MFR1', description: '10K 0402 1%'),
                new Feeder(fixedId: Optional.of(2), partCode: 'PC2', manufacturer: 'MFR2', description: '10K 0402 1%'),
            ]

        and:
            List<PartMapping> partMappings = [
                new PartMapping(namePattern: 'RES_0402', valuePattern: '10K 0402 1%', partCode: 'PC1', manufacturer: 'MFR1'),
                new PartMapping(namePattern: 'RES_0402', valuePattern: '10K 0402 1%', partCode: 'PC2', manufacturer: 'MFR2')
            ]

        and:
            List<Component> components = [
                new Component(partCode: 'PC1', manufacturer: 'MFR1'),
                new Component(partCode: 'PC2', manufacturer: 'MFR2'),
            ]

        and:
            ComponentPlacement componentPlacement = new ComponentPlacement(name: 'RES_0402', value: '10K 0402 1%')
            List<ComponentPlacement> componentPlacements = [componentPlacement]

        and:
            Map<ComponentPlacement, MaterialSelectionEntry> expectedMaterials = [
                (componentPlacement): new MaterialSelectionEntry(
                    component:  components[0], // first matching component used
                    feeder: feeders[0], // first feeder used
                )
            ]
        when:
            Map<ComponentPlacement, MaterialSelectionEntry> materials = materialSelector.selectMaterials(componentPlacements, components, partMappings, feeders)

        then:
            materials == expectedMaterials
    }
}
