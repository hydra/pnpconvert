package com.seriouslypro.eda.part

import com.seriouslypro.pnpconvert.Component
import com.seriouslypro.test.TestResources
import spock.lang.Specification

class PartSubstitutionsLoaderSpec extends Specification implements TestResources {

    def 'unexpected-headers, substitutions - aka wrong file'() {
        given:
            File inputFile = createTemporaryFileFromResource(temporaryFolder, testResource("/part-substitutions-empty.csv"))
            String inputFileName = inputFile.absolutePath

        and:
            PartSubstitutionsLoader partSubstitutor = new PartSubstitutionsLoader()

        and:
            List<Component> expectedPartSubstitutions = []

        when:
            partSubstitutor.loadFromCSV(inputFileName)

        then:
            partSubstitutor.partSubstitutions == expectedPartSubstitutions
    }

    def 'load'() {
        given:
            File inputFile = createTemporaryFileFromResource(temporaryFolder, testResource("/part-substitutions-some.csv"))
            String inputFileName = inputFile.absolutePath

        and:
            PartSubstitutionsLoader partSubstitutor = new PartSubstitutionsLoader()

        and:
            List<Component> expectedPartSubstitutions = [
                new PartSubstitution(namePattern: "NAME_PATTERN_1", valuePattern: "VALUE_PATTERN_1", name: "NAME_1", value: "VALUE_1"),
                new PartSubstitution(namePattern: "NAME_PATTERN_2", valuePattern: "VALUE_PATTERN_2", name: "NAME_2", value: "VALUE_2"),
                new PartSubstitution(namePattern: "NAME_PATTERN_3", valuePattern: "VALUE_PATTERN_3", name: "NAME_3", value: "VALUE_3"),
            ]

        when:
            partSubstitutor.loadFromCSV(inputFileName)

        then:
            partSubstitutor.partSubstitutions == expectedPartSubstitutions
    }

}
