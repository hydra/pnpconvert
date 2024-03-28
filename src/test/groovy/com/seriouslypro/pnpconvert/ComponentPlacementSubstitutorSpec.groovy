package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartSubstitution
import spock.lang.Specification

class ComponentPlacementSubstitutorSpec extends Specification {

    def 'no substitutions'() {
        given:
            ComponentPlacementSubstitutor substitutor = new ComponentPlacementSubstitutor()

        and:
            List<ComponentPlacement> placements = []
            List<PartSubstitution> partSubstitutions = []

        when:
            List<PlacementSubstitution> result = substitutor.process(placements, partSubstitutions)

        then:
            result == []
    }

    def 'substituted'() {
        given:
            ComponentPlacementSubstitutor substitutor = new ComponentPlacementSubstitutor()

        and:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: 'ORIGINAL_NAME', value: 'ORIGINAL_VALUE')
            ]

            List<PartSubstitution> partSubstitutions = [
                new PartSubstitution(namePattern: 'ORIGINAL_NAME', valuePattern: 'ORIGINAL_VALUE', name: 'NEW_NAME', value: 'NEW_VALUE'),
            ]

        and:
            ComponentPlacement expectedPlacement = new ComponentPlacement(refdes: 'R1', name: 'NEW_NAME', value: 'NEW_VALUE')
            ComponentPlacement expectedOriginalPlacement = placements[0].clone()

        and:
            PlacementSubstitution expectedPlacementSubstitution = new PlacementSubstitution(
                // input
                placement: expectedPlacement,
                // results
                appliedSubstitution: Optional.of(partSubstitutions[0]),
                originalPlacement: Optional.of(expectedOriginalPlacement),
            )

        when:
            List<PlacementSubstitution> result = substitutor.process(placements, partSubstitutions)

        then:
            result == [expectedPlacementSubstitution]
    }

    def 'placement with multiple applicable substitutions generates error'() {
        given:
            ComponentPlacementSubstitutor substitutor = new ComponentPlacementSubstitutor()

        and:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: 'ORIGINAL_NAME', value: 'ORIGINAL_NAME'),
            ]

            List<PartSubstitution> partSubstitutions = [
                new PartSubstitution(namePattern: '/.*/', valuePattern: 'ORIGINAL_NAME', name: 'NEW_NAME_1', value: 'NEW_VALUE_1'),
                new PartSubstitution(namePattern: 'NOT_MATCHED', valuePattern: 'NOT_MATCHED', name: 'NEW_NAME_1', value: 'NEW_VALUE_1'),
                new PartSubstitution(namePattern: 'ORIGINAL_NAME', valuePattern: '/.*/', name: 'NEW_NAME_2', value: 'NEW_VALUE_2'),
            ]

        and:
            ComponentPlacement expectedPlacement = new ComponentPlacement(refdes: 'R1', name: 'ORIGINAL_NAME', value: 'ORIGINAL_NAME')

        and:
            List<PartSubstitution> applicableSubstitutions = [
                partSubstitutions[0],
                partSubstitutions[2],
            ]

        and:
            PlacementSubstitution expectedPlacementSubstitution = new PlacementSubstitution(
                // input
                placement: expectedPlacement,
                // results
                appliedSubstitution: Optional.empty(),
                originalPlacement: Optional.empty(),
                errors: ["multiple matching substitutions found, be more specific with substitutions or use refdes replacements. applicableSubstitutions: '${applicableSubstitutions}'"]
            )

        when:
            List<PlacementSubstitution> result = substitutor.process(placements, partSubstitutions)

        then:
            result == [expectedPlacementSubstitution]
    }
}