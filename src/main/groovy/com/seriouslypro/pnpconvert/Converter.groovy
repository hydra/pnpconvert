package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import org.apache.batik.svggen.SVGGeneratorContext
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.Element

import java.awt.Color
import java.awt.Font

import java.text.ParseException
import java.text.SimpleDateFormat


import org.apache.batik.svggen.SVGGraphics2D
import org.apache.batik.dom.GenericDOMImplementation;


class Converter {

    String inputFileName
    String outputPrefix
    String feedersFileName

    BoardRotation boardRotation = new BoardRotation()
    Coordinate offset = new Coordinate()

    private BigDecimal lowestX = null
    private BigDecimal lowestY = null

    SVGRenderer renderer

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

        String tempFileName = outputPrefix + "-converted.csv"


        Reader inputFileReader = new FileReader(inputFileName)
        CSVReader inputCSVReader = new CSVReader(inputFileReader, ',' as char)

        Writer tempFileWriter = new FileWriter(tempFileName, append)
        CSVWriter tempCSVWriter = new CSVWriter(tempFileWriter, ',' as char)

        String[] inputHeaderValues = inputCSVReader.readNext()

        LinkedHashMap<DipTraceCSVHeaders, CSVHeader> inputHeaderMappings = createHeaderMappings(inputHeaderValues)

        verifyRequiredHeadersPresent(inputHeaderMappings, inputHeaderValues)

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
        tempCSVWriter.writeNext(outputHeaderRow)


        String[] line
        while ((line = inputCSVReader.readNext()) != null) {
            String refdes = line[inputHeaderMappings[DipTraceCSVHeaders.REFDES].index]
            String pattern = line[inputHeaderMappings[DipTraceCSVHeaders.PATTERN].index]
            BigDecimal x = line[inputHeaderMappings[DipTraceCSVHeaders.X].index] as BigDecimal
            BigDecimal y = line[inputHeaderMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
            String side = line[inputHeaderMappings[DipTraceCSVHeaders.SIDE].index]
            BigDecimal rotation = line[inputHeaderMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal
            String value = line[inputHeaderMappings[DipTraceCSVHeaders.VALUE].index]
            String name = line[inputHeaderMappings[DipTraceCSVHeaders.NAME].index]


            Coordinate c = new Coordinate(x: x, y: y)

            renderer.drawPart(Color.RED, c, refdes)

            Coordinate rotatedCoordinate = boardRotation.applyRotation(c)
            BigDecimal rotatedRotation = (rotation + boardRotation.degrees).remainder(360)

            renderer.drawPart(Color.BLUE, rotatedCoordinate, refdes)

            Coordinate relocatedCoordinate = rotatedCoordinate - boardRotation.origin

            renderer.drawPart(Color.PINK, relocatedCoordinate, refdes)

            Coordinate relocatedCoordinateWithOffset = relocatedCoordinate + offset

            renderer.drawPart(Color.GREEN, relocatedCoordinateWithOffset, refdes)

            String[] outputRow = [
                refdes,
                pattern,
                relocatedCoordinate.x,
                relocatedCoordinate.y,
                side,
                rotatedRotation,
                value,
                name
            ]
            tempCSVWriter.writeNext(outputRow)
            System.out.println(line.join(",").padRight(80) + " -> " + outputRow.join(","))
        }


        inputCSVReader.close()
        tempCSVWriter.close()

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

    void verifyRequiredHeadersPresent(Map<DipTraceCSVHeaders, CSVHeader> dipTraceCSVHeadersCSVHeaderMap, String[] headerValues) {
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

    void writeHeader(FileWriter fileWriter) {
        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('hh:mm:ss').format(now)

        String header = "separated\n" +
                "FILE,$outputPrefix\n" +
                "PCBFILE,$inputFileName\n" +
                "DATE,$formattedDate\n" +
                "TIME,$formattedTime\n" +
                "PANELYPE,0"

        fileWriter.append(header)
    }
}
