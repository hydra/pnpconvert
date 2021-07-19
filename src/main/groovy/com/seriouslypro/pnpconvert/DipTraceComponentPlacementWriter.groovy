package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVWriter
import com.seriouslypro.eda.AngleConverter
import com.seriouslypro.eda.diptrace.DiptraceAngleConverter
import com.seriouslypro.eda.diptrace.placement.DipTracePlacementsCSVHeaders

class DipTraceComponentPlacementWriter implements ComponentPlacementWriter {

    private static final boolean append = false

    AngleConverter diptraceAngleConverter = new DiptraceAngleConverter()

    CSVWriter transformCSVWriter

    DipTraceComponentPlacementWriter(String transformFileName) {

        Writer transformFileWriter = new FileWriter(transformFileName, append)
        transformCSVWriter = new CSVWriter(transformFileWriter, ',' as char)

        String[] outputHeaderRow = [
            DipTracePlacementsCSVHeaders.REFDES.aliases.first(),
            DipTracePlacementsCSVHeaders.PATTERN.aliases.first(),
            DipTracePlacementsCSVHeaders.X.aliases.first(),
            DipTracePlacementsCSVHeaders.Y.aliases.first(),
            DipTracePlacementsCSVHeaders.SIDE.aliases.first(),
            DipTracePlacementsCSVHeaders.ROTATE.aliases.first(),
            DipTracePlacementsCSVHeaders.VALUE.aliases.first(),
            DipTracePlacementsCSVHeaders.NAME.aliases.first(),
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
