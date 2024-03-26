package com.seriouslypro.pnpconvert.updater

import com.seriouslypro.pnpconvert.MatchOption
import spock.lang.Specification
import spock.lang.Unroll

class RowMatcherSpec extends Specification {

    def 'match with feeder id'() {
        given:
            Set<MatchOption> matchOptions = [MatchOption.FEEDER_ID]

        and:
            List<String> sheetHeaders = ['X Offset', 'Y Offset', 'Description', 'ID', 'Flags']
            List<String> sheetFeederRowValues = ['0.23', '0.73', 'DESCRIPTION', '1', ]

        and:
            List<String> dpvTableHeaders = ['ID', 'Note', 'DeltX', 'DeltY']
            List<String> dpvFeederEntryValues = ['1', 'DESCRIPTION;A NOTE', '0.12', '0.62']

        and:
            SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(dpvTableHeaders, sheetHeaders)

        when:
            boolean result = new RowMatcher().match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)

        then:
            result
    }

    @Unroll
    def 'match with description only - #scenario'() {
        given:
            Set<MatchOption> matchOptions = [MatchOption.DESCRIPTION]

        and:
            List<String> sheetHeaders = ['X Offset', 'Y Offset', 'Description', 'ID', 'Flags']
            List<String> sheetFeederRowValues = ['0.23', '0.73', sheetDescription, 'OLD_ID', ]

        and:
            List<String> dpvTableHeaders = ['ID', 'Note', 'DeltX', 'DeltY']
            List<String> dpvFeederEntryValues = ['NEW_ID', dpvNote, '0.12', '0.62']

        and:
            SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(dpvTableHeaders, sheetHeaders)

        when:
            boolean result = new RowMatcher().match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)

        then:
            result == expectedResult

        where:
            dpvNote                             | sheetDescription | expectedResult | scenario
            'DESCRIPTION'                       | 'DESCRIPTION'    | true           | 'exact'
            'DESCRI'                            | 'DESCRIPTION'    | true           | 'partial'
            'DESCRIPTION'                       | 'DESCRI'         | false          | 'reverse partial (only the dpv note gets truncated, not the description in sheet)'
            'DESCRIPTION;FEEDER_NOTE'           | 'DESCRIPTION'    | true           | 'exact, with feeder note'
            'DESCRI;FEEDER_NOTE'                | 'DESCRIPTION'    | true           | 'partial, with feeder note'
            'DESCRI;FEEDER_NOTE'                | 'DESCRIPTION'    | true           | 'partial, with feeder note'
            'PART;MFGR;DESCRIPTION;FEEDER_NOTE' | 'DESCRIPTION'    | true           | 'exact, with part code, manufacturer and feeder note'
            'PART;MFGR;DESCRI;FEEDER_NOTE'      | 'DESCRIPTION'    | true           | 'partial, with part code, manufacturer and feeder note'
    }

    def 'match with id and enabled flag'() {
        given:
            Set<MatchOption> matchOptions = [MatchOption.DESCRIPTION]

        and:
            List<String> sheetHeaders = ['X Offset', 'Y Offset', 'Description', 'ID', 'Flags']
            List<String> sheetFeederRowValues = ['0.23', '0.73', 'DESCRIPTION', 'OLD_ID', ]

        and:
            List<String> dpvTableHeaders = ['ID', 'Note', 'DeltX', 'DeltY']
            List<String> dpvFeederEntryValues = ['NEW_ID', 'DESCRIPT', '0.12', '0.62']

        and:
            SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(dpvTableHeaders, sheetHeaders)

        when:
            boolean result = new RowMatcher().match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)

        then:
            result
    }

    def 'do not match with id and enabled flag when feeder is disabled'() {
        given:
            Set<MatchOption> matchOptions = [MatchOption.FEEDER_ID, MatchOption.FLAG_ENABLED]

        and:
            List<String> sheetHeaders = ['X Offset', 'Y Offset', 'Description', 'ID', 'Flags']
            List<String> sheetFeederRowValues = ['0.23', '0.73', 'DESCRIPTION', 'OLD_ID', '!']

        and:
            List<String> dpvTableHeaders = ['ID', 'Note', 'DeltX', 'DeltY']
            List<String> dpvFeederEntryValues = ['NEW_ID', 'DESCRIPT', '0.12', '0.62']

        and:
            SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(dpvTableHeaders, sheetHeaders)

        when:
            boolean result = new RowMatcher().match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)

        then:
            !result
    }

    @Unroll
    def 'match with part code and manufacturer - #scenario'() {
        given:
            Set<MatchOption> matchOptions = [MatchOption.PART_CODE, MatchOption.MANUFACTURER]

        and:
            List<String> sheetHeaders = ['X Offset', 'Y Offset', 'Description', 'ID', 'Flags', 'Part Code', 'Manufacturer']
            List<String> sheetFeederRowValues = ['0.23', '0.73', 'OLD_DESCRIPTION', 'OLD_ID', null, 'ABCD', 'WXYZ']

        and:
            List<String> dpvTableHeaders = ['ID', 'Note', 'DeltX', 'DeltY']
            List<String> dpvFeederEntryValues = ['NEW_ID', note, '0.12', '0.62']

        and:
            SheetToDPVHeaderMapping sheetToEntryHeaderMapping = new SheetToDPVHeaderMapping(dpvTableHeaders, sheetHeaders)

        when:
            boolean result = new RowMatcher().match(matchOptions, sheetToEntryHeaderMapping, sheetFeederRowValues, dpvFeederEntryValues)

        then:
            result == expectedResult

        where:
            note                               | expectedResult | scenario
            'ABCD;WXYZ;NEW_DESCRIPTION;A NOTE' | true           | '4 fields'
            'ABCD;WXYZ;NEW_DESCRIPTION'        | true           | '3 fields'
            'ABCD;WXYZ'                        | true           | '2 fields'
            'ABCD'                             | false          | 'missing manufacturer'
            null                               | false          | 'no description'
            ''                                 | false          | 'empty description'
            'ABCD;QWER'                        | false          | 'only part code matches'
            'ASDF;WXYZ'                        | false          | 'only manufacturer matches'
    }
}
