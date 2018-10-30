package com.seriouslypro.pnpconvert

interface CSVLineParser {
    ComponentPlacement parse(Map<Object, CSVHeader> headerMappings, String[] rowValues)
}
