package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import org.junit.Ignore
import spock.lang.Specification

class PlacementMapperSpec extends Specification {

    def 'no mappings'() {
        given:
            List<ComponentPlacement> placements = []
            List<Component> components = []
            List<PartMapping> partMappings = []

        expect:
            new PlacementMapper().map(placements, components, partMappings) == []
    }

    // TODO test more combinations of name patterns and value patterns
    def 'mapped component'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '')
            ]

            List<PartMapping> partMappings = [
                new PartMapping(namePattern: "/.*/", valuePattern: "/.*/", partCode: 'PC2', manufacturer: 'MFR2'),
            ]

            List<Component> components = [
                new Component(partCode: 'PC2', manufacturer: 'MFR2')
            ]

        and:
            PlacementMapping expectedMappedPlacement = new PlacementMapping(
                placement: placements[0],
                mappingResults: [
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC2', manufacturer: 'MFR2'),
                        component: Optional.of(components[0]),
                        partMapping: Optional.of(partMappings[0])
                    ),
                ],
            )

        when:
            List<PlacementMapping> result = new PlacementMapper().map(placements, components, partMappings)


        then:
            result == [expectedMappedPlacement]
    }

    def 'matching component, no mapping'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '', partCode: 'PC1', manufacturer: 'MFR1'),
            ]

            List<PartMapping> partMappings = []

            List<Component> components = [
                new Component(partCode: 'PC1', manufacturer: 'MFR1')
            ]

        and:
            PlacementMapping expectedMappedPlacement = new PlacementMapping(
                placement: placements[0],
                mappingResults: [
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                        component: Optional.of(components[0]),
                        partMapping: Optional.empty(),
                    ),
                ],
            )

        when:
            List<PlacementMapping> result = new PlacementMapper().map(placements, components, partMappings)


        then:
            result == [expectedMappedPlacement]
    }

    def 'unmatched component, no mappings'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '', partCode: 'PC1', manufacturer: 'MFR1'),
            ]

            List<PartMapping> partMappings = []
            List<Component> components = []

        and:
            PlacementMapping expectedMappedPlacement = new PlacementMapping(
                // input
                placement: placements[0],
                mappingResults: [
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                        component: Optional.empty(),
                        partMapping: Optional.empty(),
                    ),
                ],
                errors: ['no matching components, check part code and manufacturer is correct, check or add components, use refdes replacements or part mappings'],
            )
        when:
            List<PlacementMapping> result = new PlacementMapper().map(placements, components, partMappings)

        then:
            result == [expectedMappedPlacement]
    }

    def 'placement with multiple matching components, no mappings'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '10K', partCode: 'PC1', manufacturer: 'MFR1'),
            ]

            List<PartMapping> partMappings = []

            List<Component> components = [
                new Component(partCode: 'PC1', manufacturer: 'MFR1', description: "A"),
                new Component(partCode: 'PC2', manufacturer: 'MFR2'),
                new Component(partCode: 'PC1', manufacturer: 'MFR1', description: "B"),
            ]

        and:
            PlacementMapping expectedMappedPlacement = new PlacementMapping(
                placement: placements[0],
                mappingResults: [
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                        component: Optional.of(components[0]),
                        partMapping: Optional.empty(),
                    ),
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                        component: Optional.of(components[2]),
                        partMapping: Optional.empty(),
                    ),
                ],
                errors: ['multiple matching components found, check for component duplicates.'],
            )
        when:
            List<PlacementMapping> result = new PlacementMapper().map(placements, components, partMappings)

        then:
            result == [expectedMappedPlacement]
    }

    def 'placement with multiple applicable mappings'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '10K'),
            ]

            List<PartMapping> partMappings = [
                new PartMapping(namePattern: "/.*/", valuePattern: "/10K.*/", partCode: 'PC2', manufacturer: 'MFR2'),
                new PartMapping(namePattern: "/.*/", valuePattern: "/20K.*/", partCode: 'PC3', manufacturer: 'MFR3'),
                new PartMapping(namePattern: "/.*/", valuePattern: "/10K.*/", partCode: 'PC4', manufacturer: 'MFR4'),
            ]

            List<Component> components = [
                new Component(partCode: 'PC2', manufacturer: 'MFR2'),
                new Component(partCode: 'PC3', manufacturer: 'MFR3'),
                new Component(partCode: 'PC4', manufacturer: 'MFR4'),
            ]
        and:

            Map<Component, Optional<PartMapping>> expectedApplicablePartMappings = [
                (components[0]): Optional.of(partMappings[0]),
                (components[2]): Optional.of(partMappings[2]),
            ]

            PlacementMapping expectedMappedPlacement = new PlacementMapping(
                // input
                placement: placements[0],
                mappingResults: [
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC2', manufacturer: 'MFR2'),
                        component: Optional.of(components[0]),
                        partMapping: Optional.of(partMappings[0]),
                    ),
                    new MappingResult(
                        criteria: new ComponentCriteria(partCode: 'PC4', manufacturer: 'MFR4'),
                        component: Optional.of(components[2]),
                        partMapping: Optional.of(partMappings[2]),
                    ),
                ],
            )
        when:
            List<PlacementMapping> result = new PlacementMapper().map(placements, components, partMappings)

        then:
            result.first() == expectedMappedPlacement
    }
}
