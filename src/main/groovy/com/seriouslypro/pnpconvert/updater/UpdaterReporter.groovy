package com.seriouslypro.pnpconvert.updater

class UpdaterReporter {
    void reportSummary(String sheetTitle, int totalRowCount, int updatedRowCount) {
        println("Sheet Title: ${sheetTitle}")
        println("Rows, total: ${totalRowCount}, updated: ${updatedRowCount}")
    }
}
