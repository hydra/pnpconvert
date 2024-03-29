package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine
import io.github.joke.spockoutputcapture.OutputCapture
import spock.lang.Specification

class DPVGeneratorSpec extends Specification {

    @OutputCapture capture

    def 'generate'() {
        given:
            Machine mockMachine = Mock(Machine)
            MaterialsAssigner mockMaterialsAssigner = Mock(MaterialsAssigner)
            MaterialAssignmentSorter mockMaterialAssignmentSorter = Mock(MaterialAssignmentSorter)
            DPVHeader dpvHeader = new DPVHeader()
            Range<Integer> trayIds = new IntRange(200, 210)

        and:
            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]

        and:
            DPVGenerator generator = new DPVGenerator(
                machine: mockMachine,
                dpvHeader: dpvHeader,
                materialsAssigner: mockMaterialsAssigner,
                materialAssignmentSorter: mockMaterialAssignmentSorter,
            )

        and:
            OutputStream outputStream = new ByteArrayOutputStream()
            Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        when:
            generator.generate(outputStream, materialSelections)

        then:
            1 * mockMachine.getTrayIds() >> trayIds
            1 * mockMaterialsAssigner.assignMaterials(_, _) >> materialAssignments

        then:
            1 * mockMaterialAssignmentSorter.sort(materialAssignments) >> materialAssignments
            0 * _._

        and:
            noExceptionThrown()

        and:
            String content = outputStream.toString()
            System.out.println(content)

            content
    }

    def 'summary and error should be displayed if assignment exceptions occur'() {
        given:
            Machine mockMachine = Mock(Machine)
            MaterialsAssigner mockMaterialsAssigner = Mock(MaterialsAssigner)
            DPVHeader dpvHeader = new DPVHeader()
            Range<Integer> trayIds = new IntRange(200, 200)

        and:
            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]

        and:
            DPVGenerator generator = new DPVGenerator(
                machine: mockMachine,
                dpvHeader: dpvHeader,
                materialsAssigner: mockMaterialsAssigner,
            )

        and:
            OutputStream outputStream = new ByteArrayOutputStream()
            Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        when:
            generator.generate(outputStream, materialSelections)

        then:
            1 * mockMachine.getTrayIds() >> trayIds
            1 * mockMaterialsAssigner.assignMaterials(_, _) >> { throw new MaterialAssignmentException(materialAssignments, new InsufficientTrayIDsException('cause message')) }

        then:
            0 * _._

        and:
            String capturedOutput = capture.toString()
            capturedOutput ==~ /(?s)(.*)SUMMARY(.*)EXCEPTION(.*)cause message(.*)/
    }
}
