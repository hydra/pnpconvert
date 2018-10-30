package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVHeader
import com.seriouslypro.pnpconvert.CSVLineParser
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate

class DipTraceLineParser implements CSVLineParser {
    @Override
    ComponentPlacement parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {

        BigDecimal x = rowValues[headerMappings[DipTraceCSVHeaders.X].index] as BigDecimal
        BigDecimal y = rowValues[headerMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        ComponentPlacement c = new ComponentPlacement(
                refdes: rowValues[headerMappings[DipTraceCSVHeaders.REFDES].index],
                coordinate: coordinate,
                pattern: rowValues[headerMappings[DipTraceCSVHeaders.PATTERN].index],
                side: rowValues[headerMappings[DipTraceCSVHeaders.SIDE].index],
                rotation: rowValues[headerMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal,
                value: rowValues[headerMappings[DipTraceCSVHeaders.VALUE].index],
                name: rowValues[headerMappings[DipTraceCSVHeaders.NAME].index]
        )

        return c
    }
}
