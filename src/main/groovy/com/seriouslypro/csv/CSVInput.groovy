package com.seriouslypro.csv

import au.com.bytecode.opencsv.CSVReader
import groovy.transform.InheritConstructors

class CSVInputContext {
    int lineIndex = 0
    String reference // filename/url/etc.

    String columnName
}

class CSVInput<TResult, TColumn extends Enum> {
    Reader reader
    CSVHeaderParser<TColumn> headerParser
    CSVLineParser<TResult, TColumn> lineParser

    CSVReader inputCSVReader
    CSVInputContext context

    CSVInput(String reference, Reader reader, CSVHeaderParser headerParser, CSVLineParser<TResult, TColumn> lineParser, char separator = ',') {
        this.headerParser = headerParser
        this.lineParser = lineParser
        this.reader = reader

        inputCSVReader = new CSVReader(reader, separator)
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

    void defaultExceptionHandler(CSVInputContext context, String[] line, Exception cause) {
        System.out.println("Parse error, reference: $context.reference, lineNumber: $context.lineIndex, column: $context.columnName, lineValues: $line, cause: $cause")
    }

    void parseLines(Closure c, Closure exceptionHandler = this.&defaultExceptionHandler) {
        String[] line

        while ((line = inputCSVReader.readNext()) != null) {
            context.lineIndex++
            context.columnName = null

            TResult t
            try {
                t = lineParser.parse(context, line)
            } catch(Exception cause) {
                exceptionHandler(context, line, cause)

                continue
            }
            c(context, t, line)
        }
    }

    @InheritConstructors
    public static class CSVParseException extends RuntimeException {
    }
}
