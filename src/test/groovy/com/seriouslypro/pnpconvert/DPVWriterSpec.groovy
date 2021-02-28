package com.seriouslypro.pnpconvert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Specification

import javax.swing.text.html.Option

class DPVWriterSpec extends Specification implements DPVFileAssertions {

    List<ComponentPlacement> componentPlacements
    Components components
    Feeders feeders

    OutputStream outputStream

    void setup() {
        componentPlacements = []
        components = new Components()
        feeders = new Feeders()

        outputStream = new ByteOutputStream()

        System.out.flush()
    }

    def 'generate empty dpv'() {
        given:
            DPVHeader dpvHeader = new DPVHeader(
                fileName: "TEST-FILE",
                pcbFileName: "TEST-PCB-FILE"
            )

            Map<Integer, String[]> materials = [:]
            List<String[]> placements = []
            List<String[]> trays = []
            Optional<Panel> optionalPanel = Optional.empty()
            Optional<List<Fiducial>> optionalFiducials = Optional.empty()

        and:

            DPVWriter writer = new DPVWriter()

        when:
            writer.write(outputStream, dpvHeader, materials, placements, trays, optionalPanel, optionalFiducials)

        then:
            String content = outputStream.toString()
            content.startsWith("separated")

        and:
            materialsPresent(content, [])

        and:
            componentsPresent(content, [])

        and:
            traysPresent(content, [])

        and:
            defaultPanelPresent(content)
    }
}
