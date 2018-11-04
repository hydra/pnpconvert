package com.seriouslypro.pnpconvert

interface CSVHeaderParser<TColumn> {
    Map<TColumn, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues)
    TColumn parseHeader(CSVInputContext context, String headerValue)
}

class CSVHeaderParserBase<TColumn> implements CSVHeaderParser<TColumn> {

    @Override
    Map<TColumn, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        int index = 0
        Map<TColumn, CSVHeader> headerMappings = headerValues.findResults { String headerValue ->

            index++
            try {
                TColumn column = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader(index: index - 1)
                new MapEntry(column, csvHeader)
            } catch (IllegalArgumentException e) {
                // ignore unknown headers
                return null
            }
        }.collectEntries()

        return headerMappings
    }

    @Override
    TColumn parseHeader(CSVInputContext context, String headerValue) {
        TColumn column = (TColumn)(headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_")) as TColumn // FIXME this doesn't appear to work, TColumn is always of type "String"
        column
    }
}