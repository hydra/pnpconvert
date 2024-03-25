package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.seriouslypro.googlesheets.GridRangeConverter
import com.seriouslypro.pnpconvert.Feeders
import com.seriouslypro.pnpconvert.MatchOption

class FeedersSheetProcessor {

    private static final int HEADER_ROW_COUNT = 1
    private static final int ROWS_PER_BATCH = 50

    SheetProcessorResult process(Sheets service, Spreadsheet spreadsheet, Sheet sheet, DPVTable feedersTable, Set<MatchOption> matchOptions) {
        SheetProcessorResult sheetProcessorResult = new SheetProcessorResult()

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
                    processEntry(service, spreadsheetId, rangeForUpdate, sheetToEntryHeaderMapping, feederRowValues as List<String>, dpvFeederEntryValues, matchOptions, sheetProcessorResult)

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

    void processEntry(Sheets service, String spreadsheetId, String range, SheetToDPVHeaderMapping sheetToEntryHeaderMapping, List<String> sheetFeederRowValues, List<String> dpvFeederEntryValues, Set<MatchOption> matchOptions, SheetProcessorResult sheetProcessorResult) {

        if (matchOptions.empty) {
            throw new IllegalArgumentException("At least one match option is required")
        }

        if (matchOptions.contains(MatchOption.FLAG_ENABLED)) {
            int sheetFlagsIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.FLAGS)

            boolean enabledFlagMatched = !sheetFeederRowValues[sheetFlagsIndex].contains('!')
            if (!enabledFlagMatched) {
                return
            }
        }

        if (matchOptions.contains(MatchOption.FEEDER_ID)) {
            int dpvIdIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.ID)
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.ID)

            boolean idMatched = dpvFeederEntryValues[dpvIdIndex] == sheetFeederRowValues[sheetColumnIndex]
            if (!idMatched) {
                return
            }
        }

        // use of the note field is fragile due to the dynamic content, and constrains of DPV note field, e.g. maximum length.
        // part code and manufacturer may not be present, require description to be present in order to detect
        int dpvNoteIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.NOTE)
        List<String> noteParts = dpvFeederEntryValues[dpvNoteIndex].split(";")

        if (matchOptions.contains(MatchOption.PART_CODE)) {
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.PART_CODE)

            if (noteParts.size() < 2) {
               return
            }
            String partCode = noteParts[0]

            boolean partCodeMatched = partCode == sheetFeederRowValues[sheetColumnIndex]
            if (!partCodeMatched) {
                return
            }
        }

        if (matchOptions.contains(MatchOption.MANUFACTURER)) {
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.MANUFACTURER)

            if (noteParts.size() < 2) {
                return
            }
            String partCode = noteParts[1]

            boolean partCodeMatched = partCode == sheetFeederRowValues[sheetColumnIndex]
            if (!partCodeMatched) {
                return
            }
        }

        // if there are 1,2,3 or 4 parts the description is available, but it's index will either be 1 or 3 depending
        // on the presence of part code and manufacturer
        // the description may become trimmed in the DPV file due to the field's maximum length, so partial matches
        // may match more rows than intended!

        if (matchOptions.contains(MatchOption.DESCRIPTION)) {
            int sheetComponentNameIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.DESCRIPTION)

            int descriptionIndex
            switch (noteParts.size()) {
                case 1:
                case 2:
                    descriptionIndex = 0
                    break
                case 3:
                case 4:
                    descriptionIndex = 2
                    break
                default:
                    return
            }
            String description = noteParts[descriptionIndex]

            boolean componentNameMatched = description.startsWith(sheetFeederRowValues[sheetComponentNameIndex])
            if (!componentNameMatched) {
                return
            }
        }

        // update the sheet with the new values, check the result

        List<String> updatedRowValues = sheetFeederRowValues.collect()
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.X_OFFSET), dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.DELTA_X)])
        updatedRowValues.set(sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.Y_OFFSET), dpvFeederEntryValues[sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.DELTA_Y)])

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
