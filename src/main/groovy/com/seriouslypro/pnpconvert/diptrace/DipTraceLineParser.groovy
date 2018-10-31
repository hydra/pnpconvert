package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVHeader
import com.seriouslypro.pnpconvert.CSVLineParser
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide

import java.text.ParseException

class DipTraceLineParser implements CSVLineParser {
    @Override
    ComponentPlacement parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {

        BigDecimal x = rowValues[headerMappings[DipTraceCSVHeaders.X].index] as BigDecimal
        BigDecimal y = rowValues[headerMappings[DipTraceCSVHeaders.Y].index] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        PCBSide side

        try {
            side = pcbSideFromDiptraceSide(rowValues[headerMappings[DipTraceCSVHeaders.SIDE].index])
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row contains invalid 'side': $rowValues", e)
        }

        ComponentPlacement c = new ComponentPlacement(
            refdes: rowValues[headerMappings[DipTraceCSVHeaders.REFDES].index],
            coordinate: coordinate,
            pattern: rowValues[headerMappings[DipTraceCSVHeaders.PATTERN].index],
            side: side,
            rotation: rowValues[headerMappings[DipTraceCSVHeaders.ROTATE].index] as BigDecimal,
            value: rowValues[headerMappings[DipTraceCSVHeaders.VALUE].index],
            name: rowValues[headerMappings[DipTraceCSVHeaders.NAME].index]
        )

        return c
    }

    private static PCBSide pcbSideFromDiptraceSide(String side) {
        if (side == "Top") {
            return PCBSide.TOP
        }
        if (side == "Bottom") {
            return PCBSide.BOTTOM
        }

        throw new IllegalArgumentException("Unknown side: $side")
    }
}
