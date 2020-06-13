package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridProperties
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.ValueRange
import org.junit.internal.builders.IgnoredBuilder
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

        when:
            SheetProcessorResult result = processor.process(mockSheetsService, spreadsheet, sheet, feedersTable)

        then:
            result == new SheetProcessorResult(totalFeederCount: 0, updatedFeederCount: 0)
    }

    def "process table"() {
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
            feedersTable.entries = [['1', TEST_COMPONENT_NAME, '0.12', '0.62']]

        and:
            Sheets.Spreadsheets mockSpreadsheets = Mock(Sheets.Spreadsheets)
            Sheets.Spreadsheets.Values.Get mockGet = Mock(Sheets.Spreadsheets.Values.Get)
            Sheets.Spreadsheets.Values mockValuesResponse = Mock(Sheets.Spreadsheets.Values)

        and: "use headers in a different order to the values in the dpv file"
            ValueRange headersValueRangeResponse = new ValueRange()
            List<List<Object>> headerValues = [["X Offset", "Y Offset", "Component Name", "ID"]]
            headersValueRangeResponse.setValues(headerValues)

        and: "use data that corresponds to the a feeder, same component and feeder id, but with different x/y co-ordinates"
            ValueRange dataValueRangeResponse = new ValueRange()
            List<List<Object>> dataValues = [['0.23', '0.73', TEST_COMPONENT_NAME, '1']]
            dataValueRangeResponse.setValues(dataValues)

        and:
            FeedersSheetProcessor processor = new FeedersSheetProcessor()

        when:
            SheetProcessorResult result = processor.process(mockSheetsService, spreadsheet, sheet, feedersTable)

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

// TODO
//        and: "sheet should contain updated values for X/Y offset"

        then:
            result == new SheetProcessorResult(totalFeederCount: 1, updatedFeederCount: 1)
    }

    @Ignore
    def "avoid errors when rate is limited"() {
        /*
            Requests must be batched to avoid rate limit, exception thrown is:
            "com.google.api.client.googleapis.json.GoogleJsonResponseException: 429 Too Many Requests"
         */
    }
}
