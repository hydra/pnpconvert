package com.seriouslypro.pnpconvert

interface CSVLineParser<TResult, TColumn> {
    TResult parse(CSVInputContext context, String[] rowValues)

    void setHeaderMappings(Map<TColumn, CSVHeader> headerMappings)
}

abstract class CSVLineParserBase<TResult, TColumn> implements CSVLineParser<TResult, TColumn> {

    Map<TColumn, CSVHeader> headerMappings

    boolean hasColumn(TColumn column) {
        headerMappings.containsKey(column)
    }

    int columnIndex(CSVInputContext context, TColumn column) {
        if (!hasColumn(column)) {
            throw new CSVInput.CSVParseException("missing column, reference: $context.reference, line: $context.lineIndex, column: $column")
        }
        return headerMappings[column].index
    }
}