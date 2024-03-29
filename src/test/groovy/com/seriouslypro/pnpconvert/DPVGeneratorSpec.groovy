package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine
import spock.lang.Ignore
import spock.lang.Specification

class DPVGeneratorSpec extends Specification {

    def 'generate'() {
        given:
            Machine mockMachine = Mock(Machine)
            DPVHeader dpvHeader = new DPVHeader()
            Range<Integer> trayIds = new IntRange(200, 210)

        and:
            DPVGenerator generator = new DPVGenerator(
                machine: mockMachine,
                dpvHeader: dpvHeader,
                optionalPanel: Optional.empty(),
                optionalFiducials: Optional.empty(),
                offsetZ: 0,
            )

        and:
            OutputStream outputStream = new ByteArrayOutputStream()
            Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        when:
            generator.generate(outputStream, materialSelections)

        then:
            1 * mockMachine.getTrayIds() >> trayIds
            0 * _._

        and:
            noExceptionThrown()
    }

    @Ignore
    def 'materials should be sorted by feederId before assigning ids'() {
        expect:
            false
    }

    @Ignore
    def 'error should be generated if no more tray ids are available when assigning IDs to trays'() {
        expect:
            false
    }
}
