package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader

class CSVInput<T> {
    Reader reader
    CSVHeaderParser headerParser
    CSVLineParser<T> lineParser

    CSVReader inputCSVReader

    CSVInput(Reader reader, CSVHeaderParser headerParser, CSVLineParser<T> lineParser) {
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
            T t = lineParser.parse(headerParser.headerMappings, line)
            c(t, line)
        }
    }
}
