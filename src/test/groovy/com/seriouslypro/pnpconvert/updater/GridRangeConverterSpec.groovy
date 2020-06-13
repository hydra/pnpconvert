package com.seriouslypro.pnpconvert.updater

import com.google.api.services.sheets.v4.model.GridRange
import org.junit.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class GridRangeConverterSpec extends Specification {
    @Unroll
    def "toString - #sc, #sr - #ec, #er = #expectedValue"() {
        given:
            GridRange gridRange = new GridRange()
            gridRange.setStartColumnIndex(sc)
            gridRange.setStartRowIndex(sr)

            gridRange.setEndColumnIndex(ec)
            gridRange.setEndRowIndex(er)

        when:
            String range = GridRangeConverter.toString(gridRange)

        then:
            range == expectedValue

        where:
            sc | sr | ec | er | expectedValue
            0  | 0  | 0  | 0  | "A1:A1"
            25 | 1  | 26 | 2  | "Z2:AA3"
            200 | 999  | 400 | 1999  | "GS1000:OK2000"
    }

    @Ignore
    def "toString with start row/column less than end row column"() {
        // Current implementation doesn't care
    }
}
