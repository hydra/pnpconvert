package com.seriouslypro.pnpconvert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Ignore
import spock.lang.Specification

class DPVWriterSpec extends Specification implements DPVFileAssertions {


    OutputStream outputStream

    List<ComponentPlacement> componentPlacements
    Components components
    Feeders feeders
    Map<Integer, String[]> materials
    List<String[]> placements
    List<String[]> trays
    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials
    DPVHeader dpvHeader

    void setup() {
        dpvHeader = new DPVHeader(
            fileName: "TEST-FILE",
            pcbFileName: "TEST-PCB-FILE"
        )

        materials = [:]
        placements = []
        trays = []
        optionalPanel = Optional.empty()
        optionalFiducials = Optional.empty()

        componentPlacements = []
        components = new Components()
        feeders = new Feeders()

        outputStream = new ByteOutputStream()

        System.out.flush()
    }

    def 'generate empty dpv'() {
        given:
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

    def 'generate default panel'() {
        given:
            DPVWriter writer = new DPVWriter()

        when:
            writer.write(outputStream, dpvHeader, materials, placements, trays, optionalPanel, optionalFiducials)

        then:
            String content = outputStream.toString()
            content.contains("PANELYPE,0")

        and:
            defaultPanelPresent(content)
    }

    def 'generate array panel'() {
        given:
            DPVWriter writer = new DPVWriter()

        and:
            Panel panel = new Panel(intervalX: 1.501, intervalY: 2.759, numberX: 3, numberY: 4)
            optionalPanel = Optional.of(panel)

        when:
            writer.write(outputStream, dpvHeader, materials, placements, trays, optionalPanel, optionalFiducials)

        then:
            String content = outputStream.toString()
            content.contains("PANELYPE,1")

        and:
            panelArrayPresent(content, panel)
    }

    def 'generate fiducial markers'() {
        given:
            DPVWriter writer = new DPVWriter()

        and:
            List<Fiducial> fiducialList = [
                new Fiducial(note: "Mark1", coordinate: new Coordinate(x: 10, y: 3)),
                new Fiducial(note: "Mark2", coordinate: new Coordinate(x: 90, y: 97)),
            ]

            optionalFiducials = Optional.of(fiducialList)

        when:
            writer.write(outputStream, dpvHeader, materials, placements, trays, optionalPanel, optionalFiducials)

        then:
            String content = outputStream.toString()
            content.contains(
                "Table,No.,nType,nAlg,nFinished" + TEST_TABLE_LINE_ENDING +
                    "PcbCalib,0,1,0,0" + TEST_TABLE_LINE_ENDING
            )

        and:
            content.contains(
                "Table,No.,ID,offsetX,offsetY,Note" + TEST_TABLE_LINE_ENDING +
                    "CalibPoint,0,1,10,3,Mark1" + CRLF +
                    "CalibPoint,1,2,90,97,Mark2" + CRLF
            )

    }

    @Ignore
    def 'trays should be sorted by id'() {
        expect:
            false
    }

}
