package com.seriouslypro.pnpconvert

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat

class DPVWriter {

    private String lineEnding
    private String tableLineEnding

    private PrintStream stream
    NumberSequence materialNumberSequence

    public void write(
        OutputStream outputStream,
        DPVHeader dpvHeader,
        Map<Integer, String[]> materials,
        List<String[]> placements,
        List<String[]> trays,
        Optional<Panel> optionalPanel,
        Optional<List<Fiducial>> optionalFiducials
    ) {

        lineEnding = "\r\n"

        tableLineEnding = lineEnding * 2

        materialNumberSequence = new NumberSequence(0)

        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader, optionalPanel)
        writeMaterials(materials)
        writePanel(optionalPanel)
        writePlacements(placements)
        writeTrays(trays)
        writeFiducials(optionalFiducials)
    }

    void writeHeader(DPVHeader dpvHeader, Optional<Panel> optionalPanel) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('HH:mm:ss').format(now)

        String panelTypeValue = optionalPanel.present ? "1" : "0" // Type 0 = batch of PCBs. Type 1 = panel of PCBs.

        String content = "separated" + lineEnding +
            DPVFileHeaders.FILE + ",$dpvHeader.fileName" + lineEnding +
            DPVFileHeaders.PCBFILE + ",$dpvHeader.pcbFileName" + lineEnding +
            DPVFileHeaders.DATE + ",$formattedDate" + lineEnding +
            DPVFileHeaders.TIME + ",$formattedTime" + lineEnding +
            DPVFileHeaders.PANELTYPE + "," + panelTypeValue + lineEnding

        stream.print(content)
        stream.print(lineEnding)
    }

    String replaceCommasWithSemicolons(String value)
    {
        return value.replace(',', ';')
    }

    def writeMaterials(Map<Integer, String[]> materials) {
        String sectionHeader =
            "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed"
        stream.print(sectionHeader + tableLineEnding)

        materials.toSorted { a, b ->
            a.key <=> b.key
        }.each { Integer feederId, String[] material ->
            String[] managedColumns = [
                "Station",
                materialNumberSequence.next()
            ]
            stream.print((managedColumns + material).collect(this.&replaceCommasWithSemicolons).join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }


    void writePlacements(List<String[]> placements) {
        String sectionHeader =
            "Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay"

        stream.print(sectionHeader + tableLineEnding)

        placements.each { placement ->
            stream.print(placement.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }

    void writeTrays(List<String[]> trays) {
        String sectionHeader = "Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start"

        stream.print(sectionHeader + tableLineEnding)

        trays.each { tray ->
            stream.print(tray.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }

        stream.print(tableLineEnding)
    }

    void writePanel(Optional<Panel> optionalPanel) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        if (optionalPanel.present) {
            Panel panel = optionalPanel.get()
            stream.print("Table,No.,ID,IntervalX,IntervalY,NumX,NumY" + tableLineEnding)
            stream.print("Panel_Array,0,1,${twoDigitDecimalFormat.format(panel.intervalX)},${twoDigitDecimalFormat.format(panel.intervalY)},${panel.numberX},${panel.numberY}" + tableLineEnding)
            stream.print(tableLineEnding)
        } else {
            stream.print("Table,No.,ID,DeltX,DeltY" + tableLineEnding)
            stream.print("Panel_Coord,0,1,0,0" + tableLineEnding)
            stream.print(tableLineEnding)
        }
    }

    void writeFiducials(Optional<List<Fiducial>> optionalFiducials) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        NumberSequence fiducialNumberSequence = new NumberSequence(0)
        NumberSequence fiducialIDSequence = new NumberSequence(1)

        if (optionalFiducials.present) {

            //nType: 0 = use components, 1 = use marks
            //nFinished: ? 0 = calibration pending, 1 = calibration completed

            String calibationModeSectionHeader =
                "Table,No.,nType,nAlg,nFinished"

            stream.print(calibationModeSectionHeader + tableLineEnding)
            stream.print("PcbCalib,0,1,0,0" + lineEnding) // Note: NOT tableLineEnding
            stream.print(lineEnding) // Note: NOT tableLineEnding


            String calibationMarksSectionHeader =
                "Table,No.,ID,offsetX,offsetY,Note"

            stream.print(calibationMarksSectionHeader + tableLineEnding)

            List<Fiducial> fiducialList = optionalFiducials.get()

            fiducialList.each { fiducial ->
                String[] fiducialValues = [
                    "CalibPoint",
                    fiducialNumberSequence.next(),
                    fiducialIDSequence.next(),
                    twoDigitDecimalFormat.format(fiducial.coordinate.x),
                    twoDigitDecimalFormat.format(fiducial.coordinate.y),
                    fiducial.note
                ]
                stream.print(fiducialValues.collect { it.replace(',', ';') }.join(",") + lineEnding) // Note: NOT tableLineEnding
            }
            stream.print(lineEnding) // Note: NOT tableLineEnding
        }
    }
}