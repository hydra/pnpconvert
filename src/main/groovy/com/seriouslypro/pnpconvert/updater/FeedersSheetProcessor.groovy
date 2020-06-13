package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange

class FeedersSheetProcessor {
    SheetProcessorResult process(Sheets service, Spreadsheet spreadsheet, Sheet sheet, DPVTable feedersTable) {
        SheetProcessorResult sheetProcessorResult = new SheetProcessorResult()

        String speadsheetId = spreadsheet.getSpreadsheetId()
        String sheetTitle = sheet.getProperties().getTitle()

        int columnCount = sheet.getProperties().getGridProperties().getColumnCount()
        int rowCount = sheet.getProperties().getGridProperties().getRowCount()

        if (rowCount < 2) {
            // need header row + data row to do something useful
            return sheetProcessorResult
        }

        GridRange gridRange = new GridRange()
        gridRange.setStartColumnIndex(0)
        gridRange.setEndColumnIndex(columnCount - 1)

        gridRange.setStartRowIndex(0)
        gridRange.setEndRowIndex(0)

        String range = sheetTitle + '!' + GridRangeConverter.toString(gridRange)

        ValueRange valuesRange = service.spreadsheets().values().get(speadsheetId, range).execute()

        feedersTable.entries.each { List<String> entryValues ->
            processEntry(entryValues, sheetProcessorResult)
        }
        return sheetProcessorResult
    }

    void processEntry(List<String> entryValues, SheetProcessorResult sheetProcessorResult) {
        // TODO match up the components in the entryValues against a row in feeders sheet and update positions (and later, other stuff too)
        sheetProcessorResult.totalFeederCount++
    }

}
