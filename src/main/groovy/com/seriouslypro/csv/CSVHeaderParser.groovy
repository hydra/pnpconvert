package com.seriouslypro.csv

interface CSVHeaderParser<TColumn extends Enum> {
    Map<TColumn, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues)
    TColumn parseHeader(CSVInputContext context, String headerValue)
}

class CSVHeaderParserBase<TColumn extends Enum> implements CSVHeaderParser<TColumn> {

    Map<TColumn, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        int index = 0
        Map<TColumn, CSVHeader> headerMappings = headerValues.findResults { String headerValue ->

            index++
            try {
                TColumn column = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader(index: index - 1)
                new MapEntry(column, csvHeader)
            } catch (IllegalArgumentException ignored) {
                // ignore unknown headers
                return null
            }
        }.collectEntries()

        return headerMappings
    }

    @Override
    Map<TColumn, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        createHeaderMappings(context, headerValues)
    }

    @Override
    TColumn parseHeader(CSVInputContext context, String headerValue) {
        String candidate = headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_")
        return TColumn.valueOf(TColumn, candidate)
    }
}