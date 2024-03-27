package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import spock.lang.Specification

class PlacementPartMapperSpec extends Specification {

    def 'no mappings'() {
        expect:
            new PlacementPartMapper().applyPartMappings([],[]) == []
    }

    def 'mapped component'() {
        given:
            List<ComponentPlacement> placements = [
                new ComponentPlacement(refdes: 'R1', name: '', value: '')
            ]

            List<PartMapping> partMappings = [
                new PartMapping(namePattern: "/.*/", valuePattern: "/.*/", partCode: 'PC2', manufacturer: 'MFR2')
            ]

        when:
            List<PartMappingResult> result = new PlacementPartMapper().applyPartMappings(partMappings, placements)

        then:
            result == [
                new PartMappingResult(
                    partMapping: partMappings[0],
                    placement: placements[0],
                    modifiedFields: [
                        'partCode' : new Tuple2(null, 'PC2'),
                        'manufacturer' : new Tuple2(null, 'MFR2'),
                    ]
                )
            ]
    }
}
