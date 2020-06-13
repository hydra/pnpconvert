package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange
import com.seriouslypro.pnpconvert.Feeders

class FeedersSheetProcessor {

    private static final int HEADER_ROW_COUNT = 1
    private static final int ROWS_PER_BATCH = 50

    SheetProcessorResult process(Sheets service, Spreadsheet spreadsheet, Sheet sheet, DPVTable feedersTable) {
        SheetProcessorResult sheetProcessorResult = new SheetProcessorResult()

        String speadsheetId = spreadsheet.getSpreadsheetId()
        String sheetTitle = sheet.getProperties().getTitle()

        int columnCount = sheet.getProperties().getGridProperties().getColumnCount()
        int rowCount = sheet.getProperties().getGridProperties().getRowCount()

        if (rowCount <= HEADER_ROW_COUNT) {
            // need header row + data rows to do something useful
            return sheetProcessorResult
        }

        GridRange gridRange = new GridRange()
        gridRange.setStartColumnIndex(0)
        gridRange.setEndColumnIndex(columnCount - 1)

        gridRange.setStartRowIndex(0)
        gridRange.setEndRowIndex(HEADER_ROW_COUNT - 1)

        String headersRange = sheetTitle + '!' + GridRangeConverter.toString(gridRange)
        ValueRange headersValuesRangeResponse = service.spreadsheets().values().get(speadsheetId, headersRange).execute()
        List<List<Object>> headersValues = headersValuesRangeResponse.getValues()
        dumpRows(headersValues)

        SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(feedersTable.headers, headersValues.last() as List<String>)

        int rowsRemaining = rowCount - HEADER_ROW_COUNT
        int nextStartRowIndex = gridRange.getEndRowIndex() + 1

        while (rowsRemaining > 0) {
            gridRange.setStartRowIndex(nextStartRowIndex)
            int rowsToRetrieve = ROWS_PER_BATCH;
            if (rowsToRetrieve > rowsRemaining) {
                rowsToRetrieve = rowsRemaining
            }
            gridRange.setEndRowIndex(nextStartRowIndex + rowsToRetrieve - 1) // -1 for inclusive

            String dataRange = sheetTitle + '!' + GridRangeConverter.toString(gridRange)
            ValueRange dataValuesRangeResponse = service.spreadsheets().values().get(speadsheetId, dataRange).execute()

            List<List<Object>> dataRowsValues = dataValuesRangeResponse.getValues()
            dumpRows(dataRowsValues)

            dataRowsValues.each { feederRowValues ->
                feedersTable.entries.each { List<String> dpvFeederEntryValues ->
                    processEntry(sheetToEntryHeaderMapping, feederRowValues as List<String>, dpvFeederEntryValues, sheetProcessorResult)
                }
            }

            int rowsRetrieved = dataRowsValues.size()
            nextStartRowIndex = gridRange.getStartRowIndex() + rowsRetrieved

            rowsRemaining -= rowsRetrieved
        }
        return sheetProcessorResult
    }

    static void dumpRows(List<List<Object>> rowsValues) {
        rowsValues.each { rowValues ->
            println(rowValues)
        }
    }

    void processEntry(SheetToDPVHeaderMapping sheetToEntryHeaderMapping, List<String> sheetFeederRowValues, List<String> dpvFeederEntryValues, SheetProcessorResult sheetProcessorResult) {
        sheetProcessorResult.totalFeederCount++

        int dpvIdIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.ID)
        int sheetIdIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.ID)

        boolean idMatched = dpvFeederEntryValues[dpvIdIndex] == sheetFeederRowValues[sheetIdIndex]
        if (!idMatched) {
            return
        }

        int dpvNoteIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.NOTE)
        int sheetComponentNameIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.COMPONENT_NAME)

        boolean componentNameMatched = dpvFeederEntryValues[dpvNoteIndex].startsWith(sheetFeederRowValues[sheetComponentNameIndex])
        if (!componentNameMatched) {
            return
        }

        // TODO update the sheet with the new values
        sheetProcessorResult.updatedFeederCount++
    }
}
