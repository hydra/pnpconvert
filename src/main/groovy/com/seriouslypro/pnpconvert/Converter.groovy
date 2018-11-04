package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVWriter
import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVHeaders
import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVInput
import com.seriouslypro.pnpconvert.machine.CHMT48VB
import sun.reflect.annotation.ExceptionProxy

import java.awt.Color

class Converter {

    String inputFileName
    String traysFileName
    String feedersFileName
    String componentsFileName
    String outputPrefix

    BoardRotation boardRotation = new BoardRotation()
    Coordinate offset = new Coordinate()

    private static final boolean append = false

    Converter(String inputFileName, String traysFileName, String feedersFileName, String componentsFileName, String outputPrefix, BoardRotation boardRotation, Coordinate offset) {
        this.inputFileName = inputFileName
        this.traysFileName = traysFileName
        this.feedersFileName = feedersFileName
        this.componentsFileName = componentsFileName
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

        Reader reader = openFileOrUrl(inputFileName)
        CSVInput csvInput = new DipTraceCSVInput(inputFileName, reader)

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

        List<ComponentPlacement> placements = []

        csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->

            ComponentPlacement transformedComponentPlacement = transformAndRender(renderer, componentPlacement)

            String[] outputRow = [
                transformedComponentPlacement.refdes,
                transformedComponentPlacement.pattern,
                transformedComponentPlacement.coordinate.x,
                transformedComponentPlacement.coordinate.y,
                pcbSideToDipTraceSide(transformedComponentPlacement.side),
                transformedComponentPlacement.rotation,
                transformedComponentPlacement.value,
                transformedComponentPlacement.name
            ]
            transformCSVWriter.writeNext(outputRow)

            System.out.println(line.join(",").padRight(80) + " -> " + outputRow.join(","))

            placements << transformedComponentPlacement
        }

        csvInput.close()

        transformCSVWriter.close()

        String svgFileName = outputPrefix + ".svg"
        renderer.save(svgFileName)

        //
        // Load Components
        //

        Components components = loadComponents()

        System.out.println("known components:")
        components.components.each { Component component ->
            System.out.println(component)
        }


        //
        // Load Trays
        //

        Trays trays = loadTrays()

        //
        // Load Feeders
        //

        Feeders feeders = loadFeeders(trays)

        //
        // Generate DPV
        //

        String outputDPVFileName = outputPrefix + ".dpv"

        OutputStream outputStream = new FileOutputStream(outputDPVFileName, append)

                DPVHeader dpvHeader = new DPVHeader(
                fileName: outputDPVFileName,
                pcbFileName: inputFileName
        )

        DPVGenerator generator = new DPVGenerator(
                dpvHeader: dpvHeader,
                placements: placements,
                components: components,
                feeders: feeders
        )

        generator.generate(outputStream)

        outputStream.close()
    }

    Trays loadTrays() {
        Reader reader = openFileOrUrl(traysFileName)

        Trays trays = new Trays()
        trays.loadFromCSV(traysFileName, reader)

        trays
    }

    Feeders loadFeeders(Trays trays) {
        Reader reader = openFileOrUrl(feedersFileName)

        Feeders feeders = new Feeders(
            machine: new CHMT48VB(),
            trays: trays
        )

        feeders.loadFromCSV(feedersFileName, reader)
        feeders
    }

    private Components loadComponents() {
        Reader reader = openFileOrUrl(componentsFileName)

        Components components = new Components()
        components.loadFromCSV(componentsFileName, reader)

        components
    }

    boolean isUrl(String fileName) {
        try {
            new URL(fileName)
            return true
        } catch (Exception e) {
            return false
        }
    }

    private Reader openFileOrUrl(String fileName) {
        if (isUrl(fileName)) {
            URL url = new URL(fileName)
            return new StringReader(url.text)
        }

        InputStream inputStream = new FileInputStream(fileName)
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream)
        inputStreamReader
    }

    private String pcbSideToDipTraceSide(PCBSide side) {
        side == PCBSide.TOP ? "Top" : "Bottom"
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
                coordinate: relocatedCoordinateWithOffset,
                side: componentPlacement.side,
                rotation: rotatedRotation,
                value: componentPlacement.value,
                name: componentPlacement.name
        )

        transformedComponentPlacement
    }


}
