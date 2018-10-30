package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter

import java.awt.*
import java.text.ParseException
import java.text.SimpleDateFormat

interface CSVHeaderParser {
    void parse(String[] headerValues)
    Map<Object, CSVHeader> getHeaderMappings()
}

class ComponentPlacement {
    String refdes
    String pattern
    Coordinate coordinate
    String side
    BigDecimal rotation
    String value
    String name
}

class DipTraceHeaderParser implements CSVHeaderParser {
    Map<DipTraceCSVHeaders, CSVHeader> headerMappings

    private Map<DipTraceCSVHeaders, CSVHeader> createHeaderMappings(String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, int index ->
            try {
                DipTraceCSVHeaders dipTraceCSVHeader = DipTraceCSVHeaders.fromString(headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[dipTraceCSVHeader] = csvHeader
            } catch (IllegalArgumentException) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    private void verifyRequiredHeadersPresent(Map<DipTraceCSVHeaders, CSVHeader> dipTraceCSVHeadersCSVHeaderMap, String[] headerValues) {
        def requiredDipTraceCSVHeaders = [
                DipTraceCSVHeaders.REFDES,
                DipTraceCSVHeaders.PATTERN,
                DipTraceCSVHeaders.X,
                DipTraceCSVHeaders.Y,
                DipTraceCSVHeaders.SIDE,
                DipTraceCSVHeaders.ROTATE,
                DipTraceCSVHeaders.VALUE,
                DipTraceCSVHeaders.NAME
        ]

        boolean haveRequiredHeaders = dipTraceCSVHeadersCSVHeaderMap.keySet().containsAll(
                requiredDipTraceCSVHeaders
        )

        if (!haveRequiredHeaders) {
            String requiredHeaders = requiredDipTraceCSVHeaders.collect {
                it.value
            }.toArray().join(',')

            throw new ParseException("Input CSV file does not contail all required headers, required: '$requiredHeaders', found: '$headerValues'", 0)
        }
    }

    @Override
    void parse(String[] headerValues) {
        headerMappings = createHeaderMappings(headerValues)

        verifyRequiredHeadersPresent(headerMappings, headerValues)
    }
}

interface CSVLineParser {
    ComponentPlacement parse(Map<Object, CSVHeader> headerMappings, String[] rowValues)
}

class DipTraceLineParser implements CSVLineParser {
    @Override
    ComponentPlacement parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {

        BigDecimal x = rowValues[headerMappings[DipTraceCSVHeaders.X].index] as BigDecimal
        BigDecimal y = rowValues[headerMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        ComponentPlacement c = new ComponentPlacement(
            refdes: rowValues[headerMappings[DipTraceCSVHeaders.REFDES].index],
            coordinate: coordinate,
            pattern: rowValues[headerMappings[DipTraceCSVHeaders.PATTERN].index],
            side: rowValues[headerMappings[DipTraceCSVHeaders.SIDE].index],
            rotation: rowValues[headerMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal,
            value: rowValues[headerMappings[DipTraceCSVHeaders.VALUE].index],
            name: rowValues[headerMappings[DipTraceCSVHeaders.NAME].index]
        )

        return c
    }
}

class CSVInput {
    Reader reader
    CSVHeaderParser headerParser
    CSVLineParser lineParser

    CSVReader inputCSVReader

    CSVInput(Reader reader, CSVHeaderParser headerParser, CSVLineParser lineParser) {
        this.headerParser = headerParser
        this.lineParser = lineParser
        this.reader = reader

        inputCSVReader = new CSVReader(reader, ',' as char)

    }

    void close() {
        inputCSVReader.close()
    }

    void parseHeader() {
        String[] inputHeaderValues = inputCSVReader.readNext()
        headerParser.parse(inputHeaderValues)
    }

    void parseLines(Closure c) {
        String[] line

        while ((line = inputCSVReader.readNext()) != null) {
            ComponentPlacement componentPlacement = lineParser.parse(headerParser.headerMappings, line)
            c(componentPlacement, line)
        }
    }
}

class DipTraceCSVInput extends CSVInput {

    DipTraceCSVInput(Reader reader) {
        super(reader, new DipTraceHeaderParser(), new DipTraceLineParser())
    }
}

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

            renderer.drawPart(Color.RED, componentPlacement.coordinate, componentPlacement.refdes)

            Coordinate rotatedCoordinate = boardRotation.applyRotation(componentPlacement.coordinate)
            BigDecimal rotatedRotation = (componentPlacement.rotation + boardRotation.degrees).remainder(360)

            renderer.drawPart(Color.BLUE, rotatedCoordinate, componentPlacement.refdes)

            Coordinate relocatedCoordinate = rotatedCoordinate - boardRotation.origin

            renderer.drawPart(Color.PINK, relocatedCoordinate, componentPlacement.refdes)

            Coordinate relocatedCoordinateWithOffset = relocatedCoordinate + offset

            renderer.drawPart(Color.GREEN, relocatedCoordinateWithOffset, componentPlacement.refdes)

            String[] outputRow = [
                componentPlacement.refdes,
                componentPlacement.pattern,
                relocatedCoordinate.x,
                relocatedCoordinate.y,
                componentPlacement.side,
                rotatedRotation,
                componentPlacement.value,
                componentPlacement.name
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
