package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.test.TestResources
import spock.lang.Specification

class ConverterSpec extends Specification implements TestResources {

    Converter converter

    void setup() {
        converter = new Converter()
    }

    void teardown() {
        System.out.flush()
    }

    def 'converter generates expected output files - without placements, components, trays and feeders'() {
        given:

            configureConverter('none', 'none', 'none', 'none')

            String outputPrefix = converter.outputPrefix

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

    def 'converter generates expected output files - with placements, components, trays and feeders'() {
        given:
            configureConverter('some', 'some', 'some', 'some')

            String outputPrefix = converter.outputPrefix

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

    private void configureConverter(String placements, String components, String trays, String feeders) {
        File inputFile = createTemporaryFileFromResource(temporaryFolder, testResource("/placements-${placements}.csv"))

        String inputFileName = inputFile.absolutePath
        converter.inputFileName = inputFileName

        String outputPrefix = stripFilenameExtension(inputFileName)
        converter.outputPrefix = outputPrefix

        File componentsFile = createTemporaryFileFromResource(temporaryFolder, testResource("/components-${components}.csv"))
        converter.componentsFileName = componentsFile.absolutePath

        File traysFile = createTemporaryFileFromResource(temporaryFolder, testResource("/trays-${trays}.csv"))
        converter.traysFileName = traysFile.absolutePath

        File feedersFile = createTemporaryFileFromResource(temporaryFolder, testResource("/feeders-${feeders}.csv"))
        converter.feedersFileName = feedersFile.absolutePath
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
