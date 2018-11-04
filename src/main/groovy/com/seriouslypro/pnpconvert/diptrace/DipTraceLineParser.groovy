package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVInputContext
import com.seriouslypro.pnpconvert.CSVLineParserBase
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide

class DipTraceLineParser extends CSVLineParserBase<ComponentPlacement, DipTraceCSVHeaders> {
    @Override
    ComponentPlacement parse(CSVInputContext context, String[] rowValues) {

        BigDecimal x = rowValues[columnIndex(context, DipTraceCSVHeaders.X)] as BigDecimal
        BigDecimal y = rowValues[columnIndex(context, DipTraceCSVHeaders.Y)] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        PCBSide side
        String sideValue = rowValues[columnIndex(context, DipTraceCSVHeaders.SIDE)]

        try {
            side = pcbSideFromDiptraceSide(sideValue)
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row contains invalid 'side': $sideValue, reference: $context.reference, line: $context.lineIndex", e)
        }

        ComponentPlacement c = new ComponentPlacement(
            refdes: rowValues[columnIndex(context, DipTraceCSVHeaders.REFDES)],
            coordinate: coordinate,
            pattern: rowValues[columnIndex(context, DipTraceCSVHeaders.PATTERN)],
            side: side,
            rotation: rowValues[columnIndex(context, DipTraceCSVHeaders.ROTATE)] as BigDecimal,
            value: rowValues[columnIndex(context, DipTraceCSVHeaders.VALUE)],
            name: rowValues[columnIndex(context, DipTraceCSVHeaders.NAME)]
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
