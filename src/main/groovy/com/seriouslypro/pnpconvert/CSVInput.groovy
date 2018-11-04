package com.seriouslypro.pnpconvert

import au.com.bytecode.opencsv.CSVReader
import groovy.transform.InheritConstructors

class CSVInputContext {
    int lineIndex = 0
    String reference // filename/url/etc.
}

class CSVInput<TResult, TColumn> {
    Reader reader
    CSVHeaderParser<TColumn> headerParser
    CSVLineParser<TResult, TColumn> lineParser

    CSVReader inputCSVReader
    CSVInputContext context

    CSVInput(String reference, Reader reader, CSVHeaderParser headerParser, CSVLineParser<TResult, TColumn> lineParser) {
        this.headerParser = headerParser
        this.lineParser = lineParser
        this.reader = reader

        inputCSVReader = new CSVReader(reader, ',' as char)
        context = new CSVInputContext(reference: reference)
    }

    void close() {
        inputCSVReader.close()
    }

    void parseHeader() {
        String[] inputHeaderValues = inputCSVReader.readNext()
        context.lineIndex++
        Map<TColumn, CSVHeader> headers = headerParser.parseHeaders(context, inputHeaderValues)

        lineParser.setHeaderMappings(headers)
    }

    void parseLines(Closure c) {
        String[] line

        while ((line = inputCSVReader.readNext()) != null) {
            context.lineIndex++

            TResult t
            try {
                t = lineParser.parse(context, line)
            } catch(Exception cause) {
                System.out.println("Parse error, reference: $context.reference, lineNumber: $context.lineIndex, lineValues: $line, cause: $cause")
                continue
            }
            c(context, t, line)
        }
    }

    @InheritConstructors
    public static class CSVParseException extends RuntimeException {
    }
}
