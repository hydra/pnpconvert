package com.seriouslypro.pnpconvert

interface CSVLineParser<T> {
    T parse(Map<Object, CSVHeader> headerMappings, String[] rowValues)
}
