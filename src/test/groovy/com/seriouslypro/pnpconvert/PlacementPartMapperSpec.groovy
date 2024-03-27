package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import org.junit.Ignore
import spock.lang.Specification

class PlacementPartMapperSpec extends Specification {

    def 'no mappings'() {
        given:
            List<ComponentPlacement> placements = []
            List<Component> components = []
            List<PartMapping> partMappings = []

        expect:
            new PlacementPartMapper().apply(placements, components, partMappings) == []
    }

    def 'mapped component'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '')
            ]

            List<PartMapping> partMappings = [
                new PartMapping(namePattern: "/.*/", valuePattern: "/.*/", partCode: 'PC2', manufacturer: 'MFR2')
            ]

            List<Component> components = [
                new Component(partCode: 'PC2', manufacturer: 'MFR2')
            ]

        and:
            MappedPlacement expectedMappedPlacement = new MappedPlacement(
                // input
                placement: placements[0],
                // results
                component: Optional.of(components[0]),
                partMapping: Optional.of(partMappings[0]),
            )

        when:
            List<MappedPlacement> result = new PlacementPartMapper().apply(placements, components, partMappings)


        then:
            result.first() == expectedMappedPlacement
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
            MappedPlacement expectedMappedPlacement = new MappedPlacement(
                // input
                placement: placements[0],
                // results
                component: Optional.of(components[0]),
                partMapping: Optional<PartMapping>.empty(),
            )

        when:
            List<MappedPlacement> result = new PlacementPartMapper().apply(placements, components, partMappings)


        then:
            result.first() == expectedMappedPlacement
    }

    def 'unmatched component, no mapping'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '', partCode: 'PC1', manufacturer: 'MFR1'),
            ]

            List<PartMapping> partMappings = []
            List<Component> components = []

        and:
            MappedPlacement expectedMappedPlacement = new MappedPlacement(
                // input
                placement: placements[0],
                // results
                component: Optional<Component>.empty(),
                partMapping: Optional<PartMapping>.empty(),
                errors: ['no matching components, check part code and manufacturer is correct, check or add components, use refdes replacements or part mappings']
            )
        when:
            List<MappedPlacement> result = new PlacementPartMapper().apply(placements, components, partMappings)

        then:
            result.first() == expectedMappedPlacement
    }

    @Ignore
    def 'component with multiple applicable mappings generates error'() {
    }
}
