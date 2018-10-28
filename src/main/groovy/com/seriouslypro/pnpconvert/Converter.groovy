package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter

import java.text.ParseException
import java.text.SimpleDateFormat

class Converter {

    String inputFileName
    String outputPrefix
    String feedersFileName

    BoardRotation boardRotation = new BoardRotation()

    private BigDecimal lowestX = null
    private BigDecimal lowestY = null

    private static final boolean append = false

    Converter(String inputFileName, String feedersFileName, String outputPrefix, BoardRotation boardRotation) {
        this.inputFileName = inputFileName
        this.feedersFileName = feedersFileName
        this.outputPrefix = outputPrefix
        this.boardRotation = boardRotation
    }

    void convert() {

        String tempFileName = outputPrefix + "-rotation-applied.csv"


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

            System.out.println("$refdes,$x,$y,$rotation")

            Coordinate c = new Coordinate(x: x, y: y)

            Coordinate rotatedCoordinate = boardRotation.applyRotation(c)
            BigDecimal rotatedRotation = (rotation + boardRotation.degrees).remainder(360)

            if (!lowestX) {
                lowestX = rotatedCoordinate.x
            } else {
                lowestX = Math.min(lowestX, rotatedCoordinate.x)
            }

            if (!lowestY) {
                lowestY = rotatedCoordinate.y
            } else {
                lowestY = Math.min(lowestY, rotatedCoordinate.y)
            }


            System.out.println("$rotatedCoordinate, $rotatedRotation")

            String[] outputRow = [
                refdes,
                pattern,
                rotatedCoordinate.x,
                rotatedCoordinate.y,
                side,
                rotatedRotation,
                value,
                name
            ]
            tempCSVWriter.writeNext(outputRow)
        }


        inputCSVReader.close()
        tempCSVWriter.close()

        System.out.println("lowestX: $lowestX, lowestY: $lowestY")

        Coordinate offset = new Coordinate(x: 0 - lowestX, y: 0 - lowestY)
        System.out.println("offsetX: $offset.x, offsetY: $offset.y")

        Reader tempFileReader = new FileReader(tempFileName)
        CSVReader tempCSVReader = new CSVReader(tempFileReader, ',' as char)

        String[] tempHeaderValues = tempCSVReader.readNext()

        LinkedHashMap<DipTraceCSVHeaders, CSVHeader> tempHeaderMappings = createHeaderMappings(tempHeaderValues)
        verifyRequiredHeadersPresent(tempHeaderMappings, tempHeaderValues)

        while ((line = tempCSVReader.readNext()) != null) {
            String refdes = line[inputHeaderMappings[DipTraceCSVHeaders.REFDES].index]
            String pattern = line[inputHeaderMappings[DipTraceCSVHeaders.PATTERN].index]
            BigDecimal x = line[inputHeaderMappings[DipTraceCSVHeaders.X].index] as BigDecimal
            BigDecimal y = line[inputHeaderMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
            String side = line[inputHeaderMappings[DipTraceCSVHeaders.SIDE].index]
            BigDecimal rotation = line[inputHeaderMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal
            String value = line[inputHeaderMappings[DipTraceCSVHeaders.VALUE].index]
            String name = line[inputHeaderMappings[DipTraceCSVHeaders.NAME].index]

            Coordinate relocatedCoordinate = new Coordinate(x: x + offset.x, y: y + offset.y)

            String[] outputRow = [
                refdes,
                pattern,
                relocatedCoordinate.x,
                relocatedCoordinate.y,
                side,
                rotation,
                value,
                name
            ]
            System.out.println(outputRow.join(","))
        }

        tempCSVReader.close()


        String outputDPVFileName = outputPrefix + ".dpv"
        Writer fileWriter = new FileWriter(outputDPVFileName, append)
        writeHeader(fileWriter)

/*
        Reader feedersFileReader = new FileReader(inputFileName)
        CSVReader feedersCSVReader = new CSVReader(feedersFileReader, ',' as char)
*/

        fileWriter.close()
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
