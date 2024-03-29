package com.seriouslypro.pnpconvert


import com.seriouslypro.pnpconvert.machine.Machine
import com.seriouslypro.test.TestResources
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Ignore
import spock.lang.Specification

class ConverterSpec extends Specification implements TestResources {

    @OutputCapture capture

    Converter converter

    final static String noPlacements = 'none'
    final static String noComponents = 'none'
    final static String noPartMappings = 'none'
    final static String noPartSubstitutions = 'none'
    final static String noTrays = 'none'
    final static String noFeeders = 'none'

    void setup() {
        converter = new Converter()
    }

    void teardown() {
        System.out.flush()
    }

    def 'converter generates expected output files - without placements, substitutions, mappings, components, trays and feeders'() {
        given:
            configureConverter(noPlacements, noComponents, noPartSubstitutions, noPartMappings, noTrays, noFeeders)

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

    def 'converter generates expected output files - with placements, substitutions, mappings, components, trays and feeders'() {
        given:
            configureConverter('some', 'some', 'some', 'some', 'some', 'some')

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

            dpvContent.contains("Station,0,4,0.15,0.15,2,0402B104K500CT;Walsin Tech Corp;CAP 100nF 50V 0402 X7R 10%;HQ,0.5,100,14,50,100,0,10,20")
            dpvContent.contains("Station,1,36,-0.07,0.35,2,CRG0402F10K;TE CONNECTIVITY;RES 10K 0402 1%;RH,0.5,100,14,50,100,0,25,100")
            dpvContent.contains("EComponent,0,1,1,4,4.2,6.9,180,0.5,14,100,C1,100nF 50V 0402 X7R 10%/CAP_0402,50")
            dpvContent.contains("EComponent,1,2,1,36,7.8,95,180,0.5,14,100,R1,10K 0402 1%/RES_0402;10K 0402 1,50")

        and:
            String svgContent = new File(expectedSVGFileName).text
            !svgContent.empty

        and:
            String capturedOutput = capture.toString()

            !capturedOutput.contains("*** ISSUES ***")
            capturedOutput ==~ /(?s)(.*)mappedPlacements:(.*)placement(.*)refdes:R1(.*)component(.*)CRG0402F10K(.*)placement(.*)refdes:C1(.*)component(.*)0402B104K500CT(.*)/
    }

    /**
     * This demonstrates using explicit part codes in the placements, which requires very strict schematic/BOM configuration
     * which is usually not what you want for things like resistors, capacitors, which is where part substitutions and mappings increases BOM flexibility.
     */
    def 'converter generates expected output files - with explicit placements, no-substitutions, no-mappings, components, trays and feeders'() {
        given:
            configureConverter('strict', 'some', 'none', 'none', 'some', 'some')

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

            dpvContent.contains("Station,0,36,-0.07,0.35,2,CRG0402F10K;TE CONNECTIVITY;RES 10K 0402 1%;RH,0.5,100,14,50,100,0,25,100")
            dpvContent.contains("EComponent,0,1,1,36,7.8,95,180,0.5,14,100,R1,10K 0402 1%/RES_0402;10K 0402 1,50")

        and:
            String svgContent = new File(expectedSVGFileName).text
            !svgContent.empty

        and:
            String capturedOutput = capture.toString()
            !capturedOutput.contains("*** ISSUES ***")
            capturedOutput ==~ /(?s)(.*)mappedPlacements:(.*)placement(.*)refdes:R1(.*)component(.*)CRG0402F10K(.*)/
    }

    /**
     * This demonstrates what happens when substitutions are not found
     */
    def 'converter generates expected output files - with explicit placements, without-substitutions, mappings, components, trays and feeders'() {
        given:
            configureConverter('some', 'some', 'none', 'some', 'some', 'some')

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

            dpvContent.contains("Station,0,36,-0.07,0.35,2,CRG0402F10K;TE CONNECTIVITY;RES 10K 0402 1%;RH,0.5,100,14,50,100,0,25,100")
            dpvContent.contains("EComponent,0,1,1,36,7.8,95,180,0.5,14,100,R1,10K 0402 1%/RES_0402;10K 0402 1,50")

        and:
            String svgContent = new File(expectedSVGFileName).text
            !svgContent.empty

        and:
            String capturedOutput = capture.toString()

            capturedOutput ==~ /(?s)(.*)mappedPlacements:(.*)refdes:C1(.*)/
            capturedOutput ==~ /(?s)(.*)ISSUES(.*)unmappedPlacements:(.*)refdes:C1(.*)no matching components(.*)/
    }

    /**
     * This is the old approach, where components don't have part code and manufacturer, no part substitutions or mappings are used and feeders don't have the part code and manufacturer columns
     * @return
     */
    def 'converter generates expected output files - with placements, components, trays and feeders'() {
        given:
            configureConverter('some', 'legacy', 'none', 'none', 'some', 'legacy')

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

        and:
            String capturedOutput = capture.toString()
            capturedOutput ==~ /(?s)(.*)ISSUES(.*)unmappedPlacements:(.*)refdes:R1(.*)no matching components(.*)refdes:C1(.*)no matching components(.*)/
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

    private void configureConverter(String placements, String components, String partSubstitutions, String partMappings, String trays, String feeders) {
        File inputFile = createTemporaryFileFromResource(temporaryFolder, testResource("/placements-${placements}.csv"))

        String inputFileName = inputFile.absolutePath
        converter.inputFileName = inputFileName

        String outputPrefix = stripFilenameExtension(inputFileName)
        converter.outputPrefix = outputPrefix

        File componentsFile = createTemporaryFileFromResource(temporaryFolder, testResource("/components-${components}.csv"))
        converter.componentsFileName = componentsFile.absolutePath

        File partSubstitutionsFile = createTemporaryFileFromResource(temporaryFolder, testResource("/part-substitutions-${partSubstitutions}.csv"))
        converter.partSubstitutionsFileName = partSubstitutionsFile.absolutePath

        File partMappingsFile = createTemporaryFileFromResource(temporaryFolder, testResource("/part-mappings-${partMappings}.csv"))
        converter.partMappingsFileName = partMappingsFile.absolutePath

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

