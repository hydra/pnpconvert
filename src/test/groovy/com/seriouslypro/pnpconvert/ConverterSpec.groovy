package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.test.TestResources
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ConverterSpec extends Specification implements TestResources {

    Converter converter

    void setup() {
        converter = new Converter()
    }

    void teardown() {
        System.out.flush()
    }

    def 'converter generates expected output files'() {
        given:
            File inputFile = createTemporaryFileFromResource(temporaryFolder, testResource('/placements1.csv'))

            String inputFileName = inputFile.absolutePath
            converter.inputFileName = inputFileName

            String outputPrefix = stripFilenameExtension(inputFileName)
            converter.outputPrefix = outputPrefix

            File componentsFile = createTemporaryFileFromResource(temporaryFolder, testResource('/components1.csv'))
            converter.componentsFileName = componentsFile.absolutePath

            File traysFile = createTemporaryFileFromResource(temporaryFolder, testResource('/trays1.csv'))
            converter.traysFileName = traysFile.absolutePath

            File feedersFile = createTemporaryFileFromResource(temporaryFolder, testResource('/feeders1.csv'))
            converter.feedersFileName = feedersFile.absolutePath

        and:
            String expectedTransformedFileName = outputPrefix + '-transformed.csv'
            String expectedDPVFileName = outputPrefix + '.dpv'
            String expectedSVGFileName = outputPrefix + '.svg'

        when:
            converter.convert()

        then:
            String transformedContent = new File(expectedTransformedFileName).text
            dumpContent(transformedContent)

            !transformedContent.empty

        and:
            String dpvContent = new File(expectedDPVFileName).text
            dumpContent(dpvContent)

            !dpvContent.empty

        and:
            String svgContent = new File(expectedSVGFileName).text
            !svgContent.empty
    }

    private static void dumpContent(String dpvContent) {
        dumpDividerLine()
        println dpvContent
        dumpDividerLine()
    }

    private static void dumpDividerLine() {
        println '*' * 80
    }
}
