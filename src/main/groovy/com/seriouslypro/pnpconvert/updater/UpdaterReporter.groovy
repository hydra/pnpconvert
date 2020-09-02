package com.seriouslypro.pnpconvert.updater

class UpdaterReporter {
    void reportSummary(String sheetTitle, SheetProcessorResult sheetProcessorResult) {
        println("Sheet Title: ${sheetTitle}")
        println("Feeders, total: ${sheetProcessorResult.totalFeederCount}, updated: ${sheetProcessorResult.updatedFeederCount}")
    }

    void reportDPVSummary(DPVFile dpvFile) {
        println("DPV FILE: ${dpvFile.properties.getProperty('FILE')}")
        println("DPV PCBFILE: ${dpvFile.properties.getProperty('PCBFILE')}")

        String tableNames = dpvFile.tables.keySet().join(',')
        println("Tables: ${tableNames}")
    }
}
