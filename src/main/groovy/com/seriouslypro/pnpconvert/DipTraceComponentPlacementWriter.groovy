package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVWriter
import com.seriouslypro.pnpconvert.diptrace.AngleConverter
import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVHeaders
import com.seriouslypro.pnpconvert.diptrace.DiptraceAngleConverter

class DipTraceComponentPlacementWriter implements ComponentPlacementWriter {

    private static final boolean append = false

    AngleConverter diptraceAngleConverter = new DiptraceAngleConverter()

    CSVWriter transformCSVWriter

    DipTraceComponentPlacementWriter(String transformFileName) {

        Writer transformFileWriter = new FileWriter(transformFileName, append)
        transformCSVWriter = new CSVWriter(transformFileWriter, ',' as char)

        String[] outputHeaderRow = [
            DipTraceCSVHeaders.REFDES.aliases.first(),
            DipTraceCSVHeaders.PATTERN.aliases.first(),
            DipTraceCSVHeaders.X.aliases.first(),
            DipTraceCSVHeaders.Y.aliases.first(),
            DipTraceCSVHeaders.SIDE.aliases.first(),
            DipTraceCSVHeaders.ROTATE.aliases.first(),
            DipTraceCSVHeaders.VALUE.aliases.first(),
            DipTraceCSVHeaders.NAME.aliases.first(),
        ]

        transformCSVWriter.writeNext(outputHeaderRow)
    }

    @Override
    void close() {
        transformCSVWriter.close()
    }

    @Override
    void process(ComponentPlacement transformedComponentPlacement, String[] line) {
        String[] outputRow = [
            transformedComponentPlacement.refdes,
            transformedComponentPlacement.pattern,
            transformedComponentPlacement.coordinate.x,
            transformedComponentPlacement.coordinate.y,
            pcbSideToDipTraceSide(transformedComponentPlacement.side),
            diptraceAngleConverter.designToEDA(transformedComponentPlacement.rotation),
            transformedComponentPlacement.value,
            transformedComponentPlacement.name
        ]
        transformCSVWriter.writeNext(outputRow)

        System.out.println(line.join(",").padRight(80) + " -> " + outputRow.join(","))
    }

    private String pcbSideToDipTraceSide(PCBSide side) {
        side == PCBSide.TOP ? "Top" : "Bottom"
    }
}
