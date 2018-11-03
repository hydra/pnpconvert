package com.seriouslypro.pnpconvert

interface CSVLineParser<TResult, TColumn> {
    TResult parse(String[] rowValues)

    void setHeaderMappings(Map<TColumn, CSVHeader> headerMappings)
}

abstract class CSVLineParserBase<TResult, TColumn> implements CSVLineParser<TResult, TColumn> {

    Map<TColumn, CSVHeader> headerMappings

    boolean hasColumn(TColumn column) {
        headerMappings.containsKey(column)
    }

    int columnIndex(TColumn column) {
        if (!hasColumn(column)) {
            throw new RuntimeException("missing column, column: $column")
        }
        return headerMappings[column].index
    }
}