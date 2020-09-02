package com.seriouslypro.pnpconvert.updater

import com.seriouslypro.pnpconvert.Feeders

enum DPVStationTableColumn {
    TABLE('Table'),
    NUMBER('No.'),
    ID('ID'),
    DELTA_X('DeltX'),
    DELTA_Y('DeltY'),
    FEED_RATE('FeedRates'),
    NOTE('Note'),
    HEIGHT('Height'),
    SPEED('Speed'),
    STATUS('Status'),
    SIZE_X('SizeX'),
    SIZE_Y('SizeY'),
    HEIGHT_TAKE('HeightTake'),
    DELAY_TAKE('DelayTake'),
    PULL_SPEED('nPullStripSpeed')

    String value

    DPVStationTableColumn(String value) {
        this.value = value
    }

    boolean matches(String candidateTitle) {
        boolean valueMatch = value == candidateTitle
        if (valueMatch) {
            return valueMatch
        }
        String simpleCandidateTitle = candidateTitle.toLowerCase().replaceAll('[^A-Za-z0-9]', "_")

        boolean titleMatch = (this.toString().toLowerCase() == simpleCandidateTitle)
        if (titleMatch) {
            return titleMatch
        }

        return false
    }

    static DPVStationTableColumn fromString(String column) {
        def enumSet = EnumSet.allOf(DPVStationTableColumn)
        return enumSet.find { it ->
            it.matches(column)
        }
    }
}

class SheetToDPVHeaderMapping {

    private final List<String> dpvTableHeaders
    private final List<String> sheetHeaders

    Map<DPVStationTableColumn, Integer> dpvStationTableColumnIndexMap = [:]
    Map<Feeders.FeederCSVColumn, Integer> feederCSVColumnIndexMap = [:]

    SheetToDPVHeaderMapping(List<String> dpvTableHeaders, List<String>sheetHeaders) {
        this.sheetHeaders = sheetHeaders
        this.dpvTableHeaders = dpvTableHeaders

        sheetHeaders.eachWithIndex { String sheetHeader, int sheetHeaderIndex ->
            try {
                Feeders.FeederCSVColumn sheetHeaderCSVColumn = sheetHeader.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as Feeders.FeederCSVColumn
                feederCSVColumnIndexMap[sheetHeaderCSVColumn] = sheetHeaderIndex
            } catch (java.lang.IllegalArgumentException e) {
                println("Ignoring column, name: '${sheetHeader}'")
            }
        }

        dpvTableHeaders.eachWithIndex{ String entry, int dpvHeaderIndex ->
            DPVStationTableColumn dpvStationTableColumn = DPVStationTableColumn.fromString(entry)
            dpvStationTableColumnIndexMap[dpvStationTableColumn] = dpvHeaderIndex
        }
    }

    int dpvIndex(DPVStationTableColumn dpvStationTableColumn) {
        return dpvStationTableColumnIndexMap[dpvStationTableColumn]
    }

    int sheetIndex(Feeders.FeederCSVColumn feederCSVColumn) {
        return feederCSVColumnIndexMap[feederCSVColumn]
    }
}
