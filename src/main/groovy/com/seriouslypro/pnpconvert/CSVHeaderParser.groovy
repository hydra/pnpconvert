package com.seriouslypro.pnpconvert

interface CSVHeaderParser<TColumn> {
    Map<TColumn, CSVHeader> parseHeaders(String[] headerValues)
    TColumn parseHeader(String headerValue)
}

class CSVHeaderParserBase<TColumn> implements CSVHeaderParser<TColumn> {

    @Override
    Map<TColumn, CSVHeader> parseHeaders(String[] headerValues) {
        int index = 0
        Map<TColumn, CSVHeader> headerMappings = headerValues.collectEntries { String headerValue ->

            TColumn column = parseHeader(headerValue)
            CSVHeader csvHeader = new CSVHeader(index: index)

            index++

            new MapEntry(column, csvHeader)
        }

        return headerMappings
    }

    @Override
    TColumn parseHeader(String headerValue) {
        TColumn column = (TColumn)(headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_")) as TColumn // FIXME this doesn't appear to work, TColumn is always of type "String"
        column
    }
}