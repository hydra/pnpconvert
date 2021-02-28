package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.DefaultMachine
import com.seriouslypro.pnpconvert.machine.Machine
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class DPVWriterSpec extends Specification implements DPVFileAssertions {

    OutputStream outputStream

    List<ComponentPlacement> componentPlacements
    Components components
    Feeders feeders
    Map<ComponentPlacement, MaterialAssignment> materialAssignments
    List<String[]> placements
    List<String[]> trays
    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials
    DPVHeader dpvHeader
    BigDecimal offsetZ
    Machine machine

    void setup() {
        dpvHeader = new DPVHeader(
            fileName: "TEST-FILE",
            pcbFileName: "TEST-PCB-FILE"
        )

        machine = new DefaultMachine()
        materialAssignments = [:]
        placements = []
        trays = []
        optionalPanel = Optional.empty()
        optionalFiducials = Optional.empty()

        componentPlacements = []
        components = new Components()
        feeders = new Feeders()

        offsetZ = 0

        outputStream = new ByteOutputStream()

        System.out.flush()
    }

    def 'generate empty dpv'() {
        given:
            DPVWriter writer = new DPVWriter()

        when:
            writer.write(outputStream, machine, offsetZ, dpvHeader, materialAssignments, optionalPanel, optionalFiducials)

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
            writer.write(outputStream, machine, offsetZ, dpvHeader, materialAssignments, optionalPanel, optionalFiducials)

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
            writer.write(outputStream, machine, offsetZ, dpvHeader, materialAssignments, optionalPanel, optionalFiducials)

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
            writer.write(outputStream, machine, offsetZ, dpvHeader, materialAssignments, optionalPanel, optionalFiducials)

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


    @Unroll
    def 'placement angle - #designAngle, #pickAngle, #feederAngle'(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle, BigDecimal expectedMachineAngle) {

        /*
            Design, Feeder, Pick angles are positive clockwise, 0 to 360.
            DipTrace EDA angles are negative clockwise, 0 to 360.  Also, Diptrace UI allows you to setting a component angle of -90, but when component is re-insepected in Diptrace UI the angle is converted to 270.
            Machine angles are negative clockwise, -180 to +180.

            Design angle = angle after conversion from EDA angle, this happens when component placements are loaded.
            Pick angle = angle relative to pick head and feeder e.g. angle of component in tape to tape feed direction
            Feeder angle = feeder angle relative to design

            Example and Notes:
            feeders on the left are 270 degrees out relative to the design (clockwise)
            feeders on the right are 90 degrees out relative to the design (clockwise)
            feeders on the left are 180 degrees out relative to the feeders on the right
            components in a tape, e.g. usb connectors, can be different relative to the design.

            IMPORTANT: Pick angles other than 0/90/180/270 will cause confusion for the vision system which will just assume
            that the part has been picked at a strange angle and will correct to the nearest 90 degrees on the nozzle
            during placement.

            When vision is disabled the pick angle on the feeder needs to be correct.

         */
        expect:
            expectedMachineAngle == new DPVWriter().calculateMachineAngle(designAngle, pickAngle, feederAngle)

        where:
            designAngle | pickAngle | feederAngle  | expectedMachineAngle
            180         | 0         | 270          | 90
            270         | 0         | 270          | 0
            0           | 0         | 270          | -90
            90          | 0         | 270          | 180
            180         | 0         | 90           | -90
            270         | 0         | 90           | 180
            0           | 0         | 90           | 90
            90          | 0         | 90           | 0
            45          | 0         | 270          | -135
            315         | 0         | 270          | -45
            0           | 90        | 270          | 180
            90          | 90        | 270          | 90
    }

}
