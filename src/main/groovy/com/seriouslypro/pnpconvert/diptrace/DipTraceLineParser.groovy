package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVLineParserBase
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide

class DipTraceLineParser extends CSVLineParserBase<ComponentPlacement, DipTraceCSVHeaders> {
    @Override
    ComponentPlacement parse(String[] rowValues) {

        BigDecimal x = rowValues[columnIndex(DipTraceCSVHeaders.X)] as BigDecimal
        BigDecimal y = rowValues[columnIndex(DipTraceCSVHeaders.Y)] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        PCBSide side

        try {
            side = pcbSideFromDiptraceSide(rowValues[headerMappings[DipTraceCSVHeaders.SIDE].index])
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row contains invalid 'side': $rowValues", e)
        }

        ComponentPlacement c = new ComponentPlacement(
            refdes: rowValues[columnIndex(DipTraceCSVHeaders.REFDES)],
            coordinate: coordinate,
            pattern: rowValues[columnIndex(DipTraceCSVHeaders.PATTERN)],
            side: side,
            rotation: rowValues[columnIndex(DipTraceCSVHeaders.ROTATE)] as BigDecimal,
            value: rowValues[columnIndex(DipTraceCSVHeaders.VALUE)],
            name: rowValues[columnIndex(DipTraceCSVHeaders.NAME)]
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
