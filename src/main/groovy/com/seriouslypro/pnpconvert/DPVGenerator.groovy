package com.seriouslypro.pnpconvert

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

class DPVGenerator {
    DPVHeader dpvHeader
    List<ComponentPlacement> placements
    Components components
    Feeders feeders

    private PrintStream stream

    void generate(OutputStream outputStream) {
        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader)
        writeMaterials()
        writePlacements()
    }

    void writeHeader(DPVHeader dpvHeader) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('hh:mm:ss').format(now)

        String header = "separated\n" +
            DPVFileHeaders.FILE + ",$dpvHeader.fileName\n" +
            DPVFileHeaders.PCBFILE + ",$dpvHeader.pcbFileName\n" +
            DPVFileHeaders.DATE + ",$formattedDate\n" +
            DPVFileHeaders.TIME + ",$formattedTime\n" +
            DPVFileHeaders.PANELTYPE + ",0"

        stream.println(header)
    }

    def writeMaterials() {
        String sectionHeader =
            "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed"

        stream.println(sectionHeader)
    }

    void writePlacements() {
        String sectionHeader =
            "Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay"

        stream.println(sectionHeader)
    }
}
