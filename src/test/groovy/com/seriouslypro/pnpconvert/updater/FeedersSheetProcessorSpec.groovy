package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.GridProperties
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import spock.lang.Specification

import static com.seriouslypro.pnpconvert.Feeders.FeederCSVColumn.*

class FeedersSheetProcessorSpec extends Specification {

    private static final String TEST_SPREADSHEET_ID = 'SPREADSHEET_ID'
    private static final String TEST_SHEET_TITLE = 'SHEET_TITLE'
    private static final String TEST_SHEET_ID = "TEST"

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
            final int valueRowCount = 3

            GridProperties gridProperties = new GridProperties(columnCount: 4, rowCount: headerRowCount + valueRowCount)

            sheetProperties.setGridProperties(gridProperties)

        and:
            DPVTable feedersTable = new DPVTable()
            feedersTable.headers = [ID, COMPONENT_NAME, X_OFFSET, Y_OFFSET]
            feedersTable.entries = [['1', 'TEST_COMPONENT_NAME', '0.12', '0.62']]

        and:
            Sheets.Spreadsheets mockSpreadsheets = Mock(Sheets.Spreadsheets)
            Sheets.Spreadsheets.Values.Get mockGet = Mock(Sheets.Spreadsheets.Values.Get)
            Sheets.Spreadsheets.Values mockValues = Mock(Sheets.Spreadsheets.Values)
            Spreadsheet spreadsheetWithValues = new Spreadsheet()

        and:
            FeedersSheetProcessor processor = new FeedersSheetProcessor()

        when:
            SheetProcessorResult result = processor.process(mockSheetsService, spreadsheet, sheet, feedersTable)

        then: "header row should be retrieved"
            1 * mockSheetsService.spreadsheets() >> mockSpreadsheets
            1 * mockSpreadsheets.values() >> mockValues
            1 * mockValues.get(TEST_SPREADSHEET_ID, "SHEET_TITLE!A1:D1") >> mockGet
            1 * mockGet.execute() >> spreadsheetWithValues
            0 * _

// TODO
//        and: "sheet should contain updated values for X/Y offset"

        then:
            result == new SheetProcessorResult(totalFeederCount: 1, updatedFeederCount: 0)
    }
}
