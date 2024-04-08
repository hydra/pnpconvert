package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.seriouslypro.googlesheets.GridRangeConverter
import com.seriouslypro.pnpconvert.FeedersLoader
import com.seriouslypro.pnpconvert.MatchOption

import java.text.DecimalFormat

class FeedersSheetProcessor {

    private static final int HEADER_ROW_COUNT = 1
    private static final int ROWS_PER_BATCH = 50

    SheetProcessorResult process(Sheets service, Spreadsheet spreadsheet, Sheet sheet, DPVTable feedersTable, Set<MatchOption> matchOptions, BigDecimal visionCalibrationFactor) {
        SheetProcessorResult sheetProcessorResult = new SheetProcessorResult()

        VisionFormatter visionFormatter = new VisionFormatter(visionCalibrationFactor: visionCalibrationFactor)

        String spreadsheetId = spreadsheet.getSpreadsheetId()
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
        ValueRange headersValuesRangeResponse = service.spreadsheets().values().get(spreadsheetId, headersRange).execute()
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
            ValueRange dataValuesRangeResponse = service.spreadsheets().values().get(spreadsheetId, dataRange).execute()

            List<List<Object>> dataRowsValues = dataValuesRangeResponse.getValues()
            dumpRows(dataRowsValues)

            dataRowsValues.eachWithIndex { List<Object> feederRowValues, int index ->
                sheetProcessorResult.totalFeederCount++

                feedersTable.entries.each { List<String> dpvFeederEntryValues ->

                    int rowIndex = gridRange.getStartRowIndex() + index

                    GridRange gridRangeForRow = gridRange.clone()
                    gridRangeForRow.setStartRowIndex(rowIndex)
                    gridRangeForRow.setEndRowIndex(rowIndex)

                    String rangeForUpdate = sheetTitle + '!' + GridRangeConverter.toString(gridRangeForRow)
                    processEntry(service, spreadsheetId, rangeForUpdate, sheetToEntryHeaderMapping, feederRowValues as List<String>, dpvFeederEntryValues, matchOptions, sheetProcessorResult, visionFormatter)

                    // TODO - Optimization; avoid processing each feedersTable.entries twice, once it's updated it's safe to ignore.
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

    void processEntry(Sheets service, String spreadsheetId, String range, SheetToDPVHeaderMapping sheetToEntryHeaderMapping, List<String> sheetFeederRowValues, List<String> dpvFeederEntryValues, Set<MatchOption> matchOptions, SheetProcessorResult sheetProcessorResult, VisionFormatter visionFormatter) {

        if (!RowMatcher.match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)) {
            return
        }

        // update the sheet with the new values, check the result

        List<String> updatedRowValues = sheetFeederRowValues.collect()
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(FeedersLoader.FeederCSVColumn.X_OFFSET), dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.DELTA_X)])
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(FeedersLoader.FeederCSVColumn.Y_OFFSET), dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.DELTA_Y)])
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(FeedersLoader.FeederCSVColumn.VISION_WIDTH), visionFormatter.formatDimension(dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.VISION_WIDTH)]))
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(FeedersLoader.FeederCSVColumn.VISION_LENGTH), visionFormatter.formatDimension(dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.VISION_LENGTH)]))

        ValueRange valueRange = new ValueRange()
        List<List<Object>> updatedValues = [updatedRowValues]
        valueRange.setValues(updatedValues)

        boolean isIdentical = true
        updatedRowValues.eachWithIndex { String entry, int i ->
            isIdentical &= sheetFeederRowValues[i] == entry
        }

        if (isIdentical) {
            return
        }

        String message = String.format("'Updating range %s, matched via: %s\nold: %s\nnew: %s\n",
            range,
            matchOptions.join(','),
            sheetFeederRowValues,
            updatedRowValues
        )
        print(message)

        UpdateValuesResponse updateValuesResponse = service.spreadsheets().values().update(spreadsheetId, range, valueRange)
            .setValueInputOption('USER_ENTERED') // TODO feels like there should be an enum for this value
            .execute()


        sheetProcessorResult.updatedFeederCount += updateValuesResponse.getUpdatedRows()
    }
}

class VisionFormatter {
    DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")
    BigDecimal visionCalibrationFactor

    String formatDimension(String value) {
        twoDigitDecimalFormat.format((value.toFloat() * visionCalibrationFactor) as BigDecimal)
    }
}
