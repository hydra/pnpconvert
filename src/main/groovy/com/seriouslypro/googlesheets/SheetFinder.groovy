package com.seriouslypro.googlesheets

import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.Spreadsheet

class SheetFinder {

    Sheet findByTitle(Spreadsheet spreadsheet, String title) {
        Sheet sheet = spreadsheet.getSheets().find { Sheet sheet ->
            sheet.getProperties().getTitle() == title
        }
        return sheet
    }
}
