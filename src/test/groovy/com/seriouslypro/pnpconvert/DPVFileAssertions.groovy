package com.seriouslypro.pnpconvert

import java.text.DecimalFormat

trait DPVFileAssertions {

    final CRLF = "\r\n"
    final TEST_TABLE_LINE_ENDING = (CRLF * 2)

    static final int MATERIAL_COLUMN_COUNT = 15
    static final int COMPONENT_COLUMN_COUNT = 14
    static final int TRAY_COLUMN_COUNT = 10
    static final int FEEDER_SUMMARY_COLUMN_COUNT = 6

    void materialsPresent(String content, List<List<String>> materialRows) {
        assert content.contains("Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed")

        materialRows.each { List<String> materialRow ->
            assert(materialRow.size() == MATERIAL_COLUMN_COUNT)
            String row = materialRow.join(",")
            assert content.contains(row)
        }
    }

    void componentsPresent(String content, List<List<String>> componentRows) {
        assert content.contains("Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay")

        componentRows.each { List<String> componentRow ->
            assert(componentRow.size() == COMPONENT_COLUMN_COUNT)
            String row = componentRow.join(",")
            assert content.contains(row)
        }
    }

    void traysPresent(String content, List<List<String>> trayRows) {
        assert content.contains("Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start")
        trayRows.each { List<String> trayRow ->
            assert (trayRow.size() == TRAY_COLUMN_COUNT)
            String row = trayRow.join(",")
            assert content.contains(row)
        }
    }

    void defaultPanelPresent(String content) {
        assert content.contains(
            "Table,No.,ID,DeltX,DeltY" + TEST_TABLE_LINE_ENDING +
                "Panel_Coord,0,1,0,0" + TEST_TABLE_LINE_ENDING
        )
    }

    void panelArrayPresent(String content, Panel panel) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        assert content.contains(
            "Table,No.,ID,IntervalX,IntervalY,NumX,NumY" + TEST_TABLE_LINE_ENDING +
                "Panel_Array,0,1,${twoDigitDecimalFormat.format(panel.intervalX)},${twoDigitDecimalFormat.format(panel.intervalY)},${panel.numberX},${panel.numberY}" + TEST_TABLE_LINE_ENDING
        )
    }


    void feederSummaryPresent(String content, List<List<String>> feederSummaryRows) {
        assert content.contains('feederSummary:')
        assert content.contains('feederId,componentsPerUnit,componentsPerPanel,refdes,feeder,component')

        feederSummaryRows.each { List<String> feederSummaryRow ->
            assert (feederSummaryRow.size() == FEEDER_SUMMARY_COLUMN_COUNT)
            String row = feederSummaryRow.join(",")
            assert content.contains(row)
        }
    }
}
