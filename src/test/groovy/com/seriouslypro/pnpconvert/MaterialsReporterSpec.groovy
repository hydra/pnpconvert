package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Specification

class MaterialsReporterSpec extends Specification {

    @OutputCapture capture

    def 'unloaded components summary'() {
        given:
            MaterialsReporter reporter = new MaterialsReporter()

        and:
            Set<PlacementMapping> unloadedPlacements = [
                new PlacementMapping(
                    placement: new ComponentPlacement(refdes: 'P1', name: 'NAME', value: 'VALUE'),
                    mappingResults: [
                        new MappingResult(
                            component: Optional.of(new Component(partCode: 'PC1', manufacturer: 'MFR1')),
                            criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                            partMapping: Optional.of(new PartMapping(namePattern: 'NAME', valuePattern: 'VALUE', partCode: 'PC1', manufacturer: 'MFR1')),
                        ),
                        new MappingResult(
                            component: Optional.of(new Component(partCode: 'PC2', manufacturer: 'MFR2')),
                            criteria: new ComponentCriteria(partCode: 'PC2', manufacturer: 'MFR2'),
                            partMapping: Optional.of(new PartMapping(namePattern: 'NAME', valuePattern: 'VALUE', partCode: 'PC2', manufacturer: 'MFR2')),
                        )
                    ]
                ),
                new PlacementMapping(
                    placement: new ComponentPlacement(refdes: 'P2', name: 'NAME', value: 'VALUE'),
                    mappingResults: [
                        new MappingResult(
                            component: Optional.of(new Component(partCode: 'PC1', manufacturer: 'MFR1')),
                            criteria: new ComponentCriteria(partCode: 'PC1', manufacturer: 'MFR1'),
                            partMapping: Optional.of(new PartMapping(namePattern: 'NAME', valuePattern: 'VALUE', partCode: 'PC1', manufacturer: 'MFR1')),
                        ),
                        new MappingResult(
                            component: Optional.of(new Component(partCode: 'PC2', manufacturer: 'MFR2')),
                            criteria: new ComponentCriteria(partCode: 'PC2', manufacturer: 'MFR2'),
                            partMapping: Optional.of(new PartMapping(namePattern: 'NAME', valuePattern: 'VALUE', partCode: 'PC2', manufacturer: 'MFR2')),
                        )
                    ]
                )
            ]
        and:
            MaterialsSelectionsResult result = new MaterialsSelectionsResult(
                unloadedPlacements: unloadedPlacements
            )

        when:
            reporter.report(result)

        then:
            String capturedOutput = capture.toString()

            /*
            unloadedComponents:
             ├─placement [refdes:P1, name:NAME, value:VALUE]
             │  ├─criteria [part code:PC1, manufacturer:MFR1]
             │  │  └─component [partCode:PC1, manufacturer:MFR1, description:null]
             │  └─criteria [part code:PC2, manufacturer:MFR2]
             │     └─component [partCode:PC2, manufacturer:MFR2, description:null]
             └─placement [refdes:P2, name:NAME, value:VALUE]
                ├─criteria [part code:PC1, manufacturer:MFR1]
                │  └─component [partCode:PC1, manufacturer:MFR1, description:null]
                └─criteria [part code:PC2, manufacturer:MFR2]
                   └─component [partCode:PC2, manufacturer:MFR2, description:null]
            */
            capturedOutput ==~ /(?sm).*unloadedComponents:(.*)placement(.*)refdes:P1(.*)criteria(.*)part code:PC1(.*)manufacturer:MFR1(.*)component(.*)partCode:PC1(.*)manufacturer:MFR1(.*)criteria(.*)part code:PC2(.*)manufacturer:MFR2(.*)placement(.*)refdes:P2(.*)criteria(.*)part code:PC1(.*)manufacturer:MFR1(.*)component(.*)partCode:PC1(.*)manufacturer:MFR1(.*)criteria(.*)part code:PC2(.*)manufacturer:MFR2(.*)component(.*)partCode:PC2(.*)manufacturer:MFR2(.*)/
    }
}
