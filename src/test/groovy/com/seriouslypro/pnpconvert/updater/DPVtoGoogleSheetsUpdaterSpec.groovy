package com.seriouslypro.pnpconvert.updater

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import spock.lang.Specification

class DPVtoGoogleSheetsUpdaterSpec extends Specification {

    private static final String CREDENTIALS_FILE_NAME = 'credentials.json'
    private static final String TEST_SPREADSHEET_TITLE = "TEST TITLE"
    private static final String TEST_SHEET_ID = "TEST"
    private static final String TEST_SHEET_TITLE_FEEDERS = "Feeders"

    TestTransportFactory testTransportFactory

    void setup() {
        testTransportFactory = new TestTransportFactory()
    }

    def 'Update'() {
        given:
            def updater = new DPVtoGoogleSheetsUpdater()

            updater.credentialsFileName = CREDENTIALS_FILE_NAME
            updater.sheetId = TEST_SHEET_ID

        and:
            CredentialFactory mockCredentialFactory = Mock(CredentialFactory)

            updater.credentialFactory = mockCredentialFactory

            def mockCredential = GroovyMock(Credential)

        and:
            def expectedTransport = GroovyMock(NetHttpTransport, name: "mockTransport")
            testTransportFactory.instances = [expectedTransport]
            updater.transportFactory = testTransportFactory

        and:
            SheetsBuilder mockSheetsBuilder = Mock(SheetsBuilder)
            updater.sheetsBuilder = mockSheetsBuilder

            Sheets mockSheetsService = Mock(Sheets)
            Sheets.Spreadsheets mockSpreadsheets = Mock(Sheets.Spreadsheets)
            Sheets.Spreadsheets.Get mockGet = Mock(Sheets.Spreadsheets.Get)

            SpreadsheetProperties mockSpreadsheetProperties = GroovyMock(SpreadsheetProperties)
            Spreadsheet mockSpreadsheet = GroovyMock(Spreadsheet, name: "mockSpreadsheet")

        and:
            SheetFinder mockSheetFinder = Mock(SheetFinder)
            updater.sheetFinder = mockSheetFinder

            Sheet mockFeedersSheet = GroovyMock(Sheet)

        and:
            def mockReporter = Mock(UpdaterReporter)
            updater.reporter = mockReporter

        when:
            updater.update()

        then:
            1 * mockCredentialFactory.getCredential(expectedTransport, CREDENTIALS_FILE_NAME) >> mockCredential
            1 * mockSheetsBuilder.build(expectedTransport, mockCredential) >> mockSheetsService
            1 * mockSheetsService.spreadsheets() >> mockSpreadsheets
            1 * mockSpreadsheets.get(TEST_SHEET_ID) >> mockGet
            1 * mockGet.execute() >> mockSpreadsheet

        then:
            1 * mockSpreadsheet.getProperties() >> mockSpreadsheetProperties
            1 * mockSpreadsheetProperties.getTitle() >> TEST_SPREADSHEET_TITLE

        then:
            1 * mockSheetFinder.findByTitle(mockSpreadsheet, TEST_SHEET_TITLE_FEEDERS) >> mockFeedersSheet

        then:
            1 * mockReporter.reportSummary(TEST_SPREADSHEET_TITLE, 0, 0)

        then:
            0 * _
    }

    /* Transport factory that builds Mock instances */

    class TestTransportFactory implements TransportFactory {

        List<NetHttpTransport> instances
        int instanceIndex = 0

        NetHttpTransport build() {
            return instances[instanceIndex++]
        }
    }
}
