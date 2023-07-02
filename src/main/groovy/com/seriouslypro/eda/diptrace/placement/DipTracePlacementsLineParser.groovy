package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParserBase
import com.seriouslypro.eda.diptrace.DiptraceAngleConverter
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide

class DipTracePlacementsLineParser extends CSVLineParserBase<ComponentPlacement, DipTracePlacementsCSVHeaders> {

    DiptraceAngleConverter diptraceAngleConverter = new DiptraceAngleConverter()

    @Override
    ComponentPlacement parse(CSVInputContext context, String[] rowValues) {

        BigDecimal x = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.X)] as BigDecimal
        BigDecimal y = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.Y)] as BigDecimal
        Coordinate coordinate = new Coordinate(x: x, y: y)

        PCBSide side
        String sideValue = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.SIDE)]

        try {
            side = pcbSideFromDiptraceSide(sideValue)
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Row contains invalid 'side': $sideValue, reference: $context.reference, line: $context.lineIndex", e)
        }

        BigDecimal diptraceRotation = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.ROTATE)] as BigDecimal
        BigDecimal placementRotation = diptraceAngleConverter.edaToDesign(diptraceRotation)

        String value = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.VALUE)].trim()
        String name = rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.NAME)].trim()

        if (!(value || name)) {
            throw new IllegalArgumentException("Row requires one or both of the 'value' and 'name' fields, reference: $context.reference, line: $context.lineIndex")
        }

        Optional<Integer> optionalJob = Optional.empty()

        if (hasColumn(DipTracePlacementsCSVHeaders.JOB) && rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.JOB)]) {
            optionalJob = Optional.of(rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.JOB)] as Integer)
        }

        ComponentPlacement c = new ComponentPlacement(
            refdes: rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.REFDES)],
            coordinate: coordinate,
            pattern: rowValues[columnIndex(context, DipTracePlacementsCSVHeaders.PATTERN)],
            side: side,
            rotation: placementRotation,
            value: value,
            name: name,
            optionalJob: optionalJob,
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
