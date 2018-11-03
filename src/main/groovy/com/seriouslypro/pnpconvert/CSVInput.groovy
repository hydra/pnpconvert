package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader
import groovy.transform.InheritConstructors

class CSVInput<TResult, TColumn> {
    Reader reader
    CSVHeaderParser<TColumn> headerParser
    CSVLineParser<TResult, TColumn> lineParser

    CSVReader inputCSVReader

    CSVInput(Reader reader, CSVHeaderParser headerParser, CSVLineParser<TResult, TColumn> lineParser) {
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
        Map<TColumn, CSVHeader> headers = headerParser.parseHeaders(inputHeaderValues)

        lineParser.setHeaderMappings(headers)
    }

    void parseLines(Closure c) {
        String[] line
        int lineIndex = 1

        while ((line = inputCSVReader.readNext()) != null) {
            TResult t
            try {
                t = lineParser.parse(line)
            } catch(Exception cause) {
                throw new CSVParseException("parse error, lineNumber: $lineIndex, lineValues: $line", cause)
            }
            c(t, line)
        }
    }

    @InheritConstructors
    public static class CSVParseException extends RuntimeException {
    }
}
