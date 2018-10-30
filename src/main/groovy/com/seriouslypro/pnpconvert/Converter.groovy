package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVWriter
import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVHeaders
import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVInput

import java.awt.*
import java.text.SimpleDateFormat


class Converter {

    String inputFileName
    String outputPrefix
    String feedersFileName

    BoardRotation boardRotation = new BoardRotation()
    Coordinate offset = new Coordinate()

    private static final boolean append = false

    Converter(String inputFileName, String feedersFileName, String outputPrefix, BoardRotation boardRotation, Coordinate offset) {
        this.inputFileName = inputFileName
        this.feedersFileName = feedersFileName
        this.outputPrefix = outputPrefix
        this.boardRotation = boardRotation
        this.offset = offset
    }

    void convert() {

        SVGRenderer renderer = new SVGRenderer()

        //
        // CSV processing
        //

        String transformFileName = outputPrefix + "-transformed.csv"

        Reader inputFileReader = new FileReader(inputFileName)
        CSVInput csvInput = new DipTraceCSVInput(inputFileReader)

        Writer transformFileWriter = new FileWriter(transformFileName, append)
        CSVWriter transformCSVWriter = new CSVWriter(transformFileWriter, ',' as char)

        csvInput.parseHeader()

        String[] outputHeaderRow = [
            DipTraceCSVHeaders.REFDES.value,
            DipTraceCSVHeaders.PATTERN.value,
            DipTraceCSVHeaders.X.value,
            DipTraceCSVHeaders.Y.value,
            DipTraceCSVHeaders.SIDE.value,
            DipTraceCSVHeaders.ROTATE.value,
            DipTraceCSVHeaders.VALUE.value,
            DipTraceCSVHeaders.NAME.value,
        ]
        transformCSVWriter.writeNext(outputHeaderRow)


        csvInput.parseLines { ComponentPlacement componentPlacement, String[] line ->

            ComponentPlacement transformedComponentPlacement = transformAndRender(renderer, componentPlacement)

            String[] outputRow = [
                transformedComponentPlacement.refdes,
                transformedComponentPlacement.pattern,
                transformedComponentPlacement.coordinate.x,
                transformedComponentPlacement.coordinate.y,
                transformedComponentPlacement.side,
                transformedComponentPlacement.rotation,
                transformedComponentPlacement.value,
                transformedComponentPlacement.name
            ]
            transformCSVWriter.writeNext(outputRow)
            System.out.println(line.join(",").padRight(80) + " -> " + outputRow.join(","))
        }

        csvInput.close()

        transformCSVWriter.close()

        String svgFileName = outputPrefix + ".svg"
        renderer.save(svgFileName)

/*
        String outputDPVFileName = outputPrefix + ".dpv"
        Writer fileWriter = new FileWriter(outputDPVFileName, append)
        writeHeader(fileWriter)

        Reader feedersFileReader = new FileReader(inputFileName)
        CSVReader feedersCSVReader = new CSVReader(feedersFileReader, ',' as char)

        fileWriter.close()
*/
    }

    private ComponentPlacement transformAndRender(SVGRenderer renderer, ComponentPlacement componentPlacement) {

        // render original position
        renderer.drawPart(Color.RED, componentPlacement.coordinate, componentPlacement.refdes, componentPlacement.rotation)

        // apply board rotation
        Coordinate rotatedCoordinate = boardRotation.applyRotation(componentPlacement.coordinate)
        BigDecimal rotatedRotation = (componentPlacement.rotation + boardRotation.degrees).remainder(360)
        renderer.drawPart(Color.BLUE, rotatedCoordinate, componentPlacement.refdes, rotatedRotation)

        // apply board origin
        Coordinate relocatedCoordinate = rotatedCoordinate - boardRotation.origin
        renderer.drawPart(Color.PINK, relocatedCoordinate, componentPlacement.refdes, rotatedRotation)

        // apply offset
        Coordinate relocatedCoordinateWithOffset = relocatedCoordinate + offset
        renderer.drawPart(Color.GREEN, relocatedCoordinateWithOffset, componentPlacement.refdes, rotatedRotation)

        ComponentPlacement transformedComponentPlacement = new ComponentPlacement(
                refdes: componentPlacement.refdes,
                pattern: componentPlacement.pattern,
                coordinate: rotatedCoordinate,
                side: componentPlacement.side,
                rotation: rotatedRotation,
                value: componentPlacement.value,
                name: componentPlacement.name
        )

        transformedComponentPlacement
    }


    void writeHeader(FileWriter fileWriter) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('hh:mm:ss').format(now)


        String header = "separated\n" +
            DPVFileHeaders.FILE + ",$outputPrefix\n" +
            DPVFileHeaders.PCBFILE + ",$inputFileName\n" +
            DPVFileHeaders.DATE + ",$formattedDate\n" +
            DPVFileHeaders.TIME + ",$formattedTime\n" +
            DPVFileHeaders.PANELTYPE + ",0"

        fileWriter.append(header)
    }
}
