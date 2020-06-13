package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.model.GridRange

class GridRangeConverter {

    static String toString(GridRange gridRange) {
        String range = String.format(
            "%s%d:%s%d",
            ColumnIdentifier.fromInt(gridRange.getStartColumnIndex() + 1),
            gridRange.getStartRowIndex() + 1,
            ColumnIdentifier.fromInt(gridRange.getEndColumnIndex() + 1),
            gridRange.getEndRowIndex() + 1
        )
        return range
    }
}

