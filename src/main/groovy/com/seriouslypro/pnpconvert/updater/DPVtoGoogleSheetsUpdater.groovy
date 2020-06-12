package com.seriouslypro.pnpconvert.updater

import com.google.api.client.auth.oauth2.Credential
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties

class DPVtoGoogleSheetsUpdater {

    public static final String SHEET_TITLE_FEEDERS = "Feeders"

    String inputFileName
    String sheetId
    String credentialsFileName

    CredentialFactory credentialFactory = new CredentialFactory()
    TransportFactory transportFactory = new GoogleSheetsTransportFactory()
    UpdaterReporter reporter = new UpdaterReporter()

    SheetsBuilder sheetsBuilder = new SheetsBuilder()
    SheetFinder sheetFinder = new SheetFinder()

    def update() {
        def transport = transportFactory.build()

        Credential credentials = credentialFactory.getCredential(transport, credentialsFileName)

        Sheets service = sheetsBuilder.build(transport, credentials)

        Spreadsheet spreadsheet = service.spreadsheets().get(sheetId).execute()

        SpreadsheetProperties spreadsheetProperties = spreadsheet.getProperties()

        String sheetTitle = spreadsheetProperties.getTitle()

        Sheet feedersSheet = sheetFinder.findByTitle(spreadsheet, SHEET_TITLE_FEEDERS)
        if (feedersSheet == null) {
            throw new SheetNotFoundException(SHEET_TITLE_FEEDERS)
        }

        FileInputStream dpvFileInputStream = new FileInputStream(inputFileName)
        DPVFileParser dpvFileParser = new DPVFileParser()
        DPVFile dpvFile = dpvFileParser.parse(dpvFileInputStream)

        reporter.reportDPVSummary(dpvFile)

        int totalRowCount = 0
        int updatedRowCount = 0
        reporter.reportSummary(sheetTitle, totalRowCount, updatedRowCount)
    }
}

class SheetNotFoundException extends RuntimeException {
    SheetNotFoundException(String title) {
        new RuntimeException("Sheet not found, title: ${title}")
    }
}
