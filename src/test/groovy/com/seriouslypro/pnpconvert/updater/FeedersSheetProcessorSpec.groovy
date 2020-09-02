package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridProperties
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.UpdateValuesResponse
import com.google.api.services.sheets.v4.model.ValueRange
import com.seriouslypro.pnpconvert.MatchOption
import spock.lang.Ignore
import spock.lang.Specification

import static com.seriouslypro.pnpconvert.Feeders.FeederCSVColumn.*

class FeedersSheetProcessorSpec extends Specification {

    private static final String TEST_SPREADSHEET_ID = 'SPREADSHEET_ID'
    private static final String TEST_SHEET_TITLE = 'SHEET_TITLE'
    public static final String TEST_COMPONENT_NAME = 'COMPONENT_NAME'

    def "process empty table"() {
        given:
            Sheets mockSheetsService = GroovyMock(Sheets)
            Spreadsheet spreadsheet = new Spreadsheet()
            spreadsheet.setSpreadsheetId(TEST_SPREADSHEET_ID)

        and:
            Sheet sheet = new Sheet()
            SheetProperties sheetProperties = new SheetProperties()
            sheetProperties.setTitle(TEST_SHEET_TITLE)
            sheet.setProperties(sheetProperties)

        and:
            final int headerRowCount = 1
            final int valueRowCount = 0

            GridProperties gridProperties = new GridProperties(columnCount: 4, rowCount: headerRowCount + valueRowCount)
            sheetProperties.setGridProperties(gridProperties)

        and:
            DPVTable feedersTable = new DPVTable()

        and:
            FeedersSheetProcessor processor = new FeedersSheetProcessor()
            Set<MatchOption> matchOptions = [MatchOption.FEEDER_ID, MatchOption.COMPONENT_NAME]

        when:
            SheetProcessorResult result = processor.process(mockSheetsService, spreadsheet, sheet, feedersTable, matchOptions)

        then:
            result == new SheetProcessorResult(totalFeederCount: 0, updatedFeederCount: 0)
    }

    def "process table and update single row"() {
        given:
            Sheets mockSheetsService = GroovyMock(Sheets)
            Spreadsheet spreadsheet = new Spreadsheet()
            spreadsheet.setSpreadsheetId(TEST_SPREADSHEET_ID)

        and:
            Sheet sheet = new Sheet()
            SheetProperties sheetProperties = new SheetProperties()
            sheetProperties.setTitle(TEST_SHEET_TITLE)
            sheet.setProperties(sheetProperties)

        and:
            final int headerRowCount = 1
            final int valueRowCount = 1

            GridProperties gridProperties = new GridProperties(columnCount: 4, rowCount: headerRowCount + valueRowCount)

            sheetProperties.setGridProperties(gridProperties)

        and:
            DPVTable feedersTable = new DPVTable()
            feedersTable.headers = ["ID", "Note", "DeltX", "DeltY"]
            feedersTable.entries = [['1', TEST_COMPONENT_NAME + ' A NOTE', '0.12', '0.62']]

        and:
            Sheets.Spreadsheets mockSpreadsheets = Mock(Sheets.Spreadsheets)
            Sheets.Spreadsheets.Values.Get mockGet = Mock(Sheets.Spreadsheets.Values.Get)
            Sheets.Spreadsheets.Values mockValuesResponse = Mock(Sheets.Spreadsheets.Values)

        and: "use headers in a different order to the values in the dpv file"
            ValueRange headersValueRangeResponse = new ValueRange()
            List<List<Object>> headerValues = [["X Offset", "Y Offset", "Component Name", "ID", "Flags"]]
            headersValueRangeResponse.setValues(headerValues)

        and: "use data that corresponds to the a feeder, same component and feeder id, but with different x/y co-ordinates"
            ValueRange dataValueRangeResponse = new ValueRange()
            List<List<Object>> dataValues = [['0.23', '0.73', TEST_COMPONENT_NAME, '1','']]
            dataValueRangeResponse.setValues(dataValues)

        and: "use updated data for row to be updated"
            Sheets.Spreadsheets.Values.Update mockUpdate = Mock(Sheets.Spreadsheets.Values.Update)
            ValueRange expectedValueRange = new ValueRange()
            List<List<Object>> updatedValues = [['0.12', '0.62', TEST_COMPONENT_NAME, '1','']]
            expectedValueRange.setValues(updatedValues)
            UpdateValuesResponse updateValuesResponse = new UpdateValuesResponse()

            // If the cell content is the same, the API still includes it in the count of updated cells.
            updateValuesResponse.setUpdatedCells(4)
            updateValuesResponse.setUpdatedRows(1)

        and:
            FeedersSheetProcessor processor = new FeedersSheetProcessor()
            Set<MatchOption> matchOptions = [MatchOption.FEEDER_ID, MatchOption.COMPONENT_NAME]

        when:
            SheetProcessorResult result = processor.process(mockSheetsService, spreadsheet, sheet, feedersTable, matchOptions)

        then: "header row should be retrieved"
            1 * mockSheetsService.spreadsheets() >> mockSpreadsheets
            1 * mockSpreadsheets.values() >> mockValuesResponse
            1 * mockValuesResponse.get(TEST_SPREADSHEET_ID, "SHEET_TITLE!A1:D1") >> mockGet
            1 * mockGet.execute() >> headersValueRangeResponse
            0 * _

        then: "data row should be retrieved"
            1 * mockSheetsService.spreadsheets() >> mockSpreadsheets
            1 * mockSpreadsheets.values() >> mockValuesResponse
            1 * mockValuesResponse.get(TEST_SPREADSHEET_ID, "SHEET_TITLE!A2:D2") >> mockGet
            1 * mockGet.execute() >> dataValueRangeResponse
            0 * _

        then: "an updated matching row should be sent"
            1 * mockSheetsService.spreadsheets() >> mockSpreadsheets
            1 * mockSpreadsheets.values() >> mockValuesResponse
            1 * mockValuesResponse.update(TEST_SPREADSHEET_ID, "SHEET_TITLE!A2:D2", expectedValueRange) >> mockUpdate

            // See: https://developers.google.com/sheets/api/reference/rest/v4/ValueInputOption
            // 'RAW' removes any existing formatting/justification.
            1 * mockUpdate.setValueInputOption('USER_ENTERED') >> mockUpdate
            1 * mockUpdate.execute() >> updateValuesResponse
            0 * _

        then:
            result == new SheetProcessorResult(totalFeederCount: 1, updatedFeederCount: 1)
    }

    @Ignore
    def "process table and update multiple rows"() {
    }

    @Ignore
    def "process table whilst avoiding updating of rows that don't need updating"() {
    }

    @Ignore
    def "avoid errors when rate is limited"() {
        /*
            Requests must be batched to avoid rate limit, exception thrown is:
            "com.google.api.client.googleapis.json.GoogleJsonResponseException: 429 Too Many Requests"
         */
    }

    @Ignore
    def "use specific match options"() {
    }

    @org.junit.Ignore
    def "ensure rows to be updated that contain empty strings (for x/y) are updated"() {
        /*
            old: ["W25N01GVxxIG/IT", "", "62", "", "Y", "16", "8", "", "", "25", "0.1", "", "10", "YES", "180", "2", "Y", "Y", "Y"] <-- has "" for X/Y
            new: ["W25N01GVxxIG/IT", "", "62", "", "Y", "16", "8", "-4.38", "1.32", "25", "0.1", "", "10", "YES", "180", "2", "Y", "Y", "Y"]

            didn't work when using "updatedRowValues.containsAll(sheetFeederRowValues)"
         */
    }
}
