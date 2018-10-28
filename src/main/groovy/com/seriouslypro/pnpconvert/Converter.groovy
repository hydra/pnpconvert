package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader

import java.text.ParseException
import java.text.SimpleDateFormat

class Converter {

    String inputFileName
    String outputFileName
    String feedersFileName

    BoardRotation boardRotation = new BoardRotation()

    Converter(String inputFileName, String feedersFileName, String outputFileName, BoardRotation boardRotation) {
        this.inputFileName = inputFileName
        this.feedersFileName = feedersFileName
        this.outputFileName = outputFileName
        this.boardRotation = boardRotation
    }

    void go() {

        boolean append = false
        Writer fileWriter = new FileWriter(outputFileName, append)

        Reader inputFileReader = new FileReader(inputFileName)
        CSVReader inputCSVReader = new CSVReader(inputFileReader, ',' as char)

        writeHeader(fileWriter)

        String[] headerValues = inputCSVReader.readNext()

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

        verifyRequiredHeadersPresent(headerMappings, headerValues)

        String[] line
        while ((line = inputCSVReader.readNext()) != null) {
            String refdes = line[headerMappings[DipTraceCSVHeaders.REFDES].index]
            BigDecimal x = line[headerMappings[DipTraceCSVHeaders.X].index] as BigDecimal
            BigDecimal y = line[headerMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
            BigDecimal rotation = line[headerMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal

            System.out.println("$refdes,$x,$y,$rotation")

            Coordinate c = new Coordinate(x: x, y: y)

            Coordinate rotatedCoordinate = boardRotation.applyRotation(c)
            BigDecimal rotatedRotation = (rotation + boardRotation.degrees).remainder(360)

            System.out.println("$rotatedCoordinate, $rotatedRotation")
        }

/*
        Reader feedersFileReader = new FileReader(inputFileName)
        CSVReader feedersCSVReader = new CSVReader(feedersFileReader, ',' as char)
*/
        inputCSVReader.close()
        fileWriter.close()
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
                "FILE,$outputFileName\n" +
                "PCBFILE,$inputFileName\n" +
                "DATE,$formattedDate\n" +
                "TIME,$formattedTime\n" +
                "PANELYPE,0"

        fileWriter.append(header)
    }
}
