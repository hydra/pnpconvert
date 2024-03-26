package com.seriouslypro.pnpconvert


import com.seriouslypro.pnpconvert.machine.Machine
import com.seriouslypro.test.TestResources
import spock.lang.Ignore
import spock.lang.Specification

class ConverterSpec extends Specification implements TestResources {

    Converter converter

    final static String noPlacements = 'none'
    final static String noComponents = 'none'
    final static String noTrays = 'none'
    final static String noFeeders = 'none'

    void setup() {
        converter = new Converter()
    }

    void teardown() {
        System.out.flush()
    }

    def 'converter generates expected output files - without placements, components, trays and feeders'() {
        given:
            configureConverter(noPlacements, noComponents, noTrays, noFeeders)

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

            dpvContent.contains("Station,0,36,-0.07,0.35,2,PC1;MFR1;10K 0402 1%/RES_0402;RH,0.5,100,15,50,100,0,25,100")
            dpvContent.contains("EComponent,0,1,1,36,7.8,95,180,0.5,15,100,R1,10K 0402 1%/RES_0402,50")

        and:
            String svgContent = new File(expectedSVGFileName).text
            !svgContent.empty
    }

    @Ignore
    def 'disable selected components by refdes'() {
        expect:
            false
    }

    @Ignore
    def 'replace selected components by refdes'() {
        expect:
            false
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

        converter.optionalPanel = Optional.empty()
        converter.board = new Board()
        converter.optionalFiducials = Optional.empty()

        converter.machine = new TestMachine()

        converter.optionalJob = Optional.empty()
    }

    private static void dumpContent(String dpvContent) {
        dumpDividerLine()
        println dpvContent
        dumpDividerLine()
    }

    private static void dumpDividerLine() {
        println '*' * 80
    }

    private class TestMachine extends Machine {

        Range trayIds = 1001..1009
        @Override
        FeederProperties feederProperties(Integer id) {
            return defaultFeederProperties
        }
    }
}

