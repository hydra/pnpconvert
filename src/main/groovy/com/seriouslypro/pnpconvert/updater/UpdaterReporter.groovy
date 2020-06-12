package com.seriouslypro.pnpconvert.updater

class UpdaterReporter {
    void reportSummary(String sheetTitle, int totalRowCount, int updatedRowCount) {
        println("Sheet Title: ${sheetTitle}")
        println("Rows, total: ${totalRowCount}, updated: ${updatedRowCount}")
    }

    void reportDPVSummary(DPVFile dpvFile) {
        println("DPV FILE: ${dpvFile.properties.getProperty('FILE')}")
        println("DPV PCBFILE: ${dpvFile.properties.getProperty('PCBFILE')}")

        String tableNames = dpvFile.tables.keySet().join(',')
        println("Tables: ${tableNames}")
    }
}
