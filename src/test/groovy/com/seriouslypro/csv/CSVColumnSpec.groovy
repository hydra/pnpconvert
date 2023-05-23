package com.seriouslypro.csv

import com.seriouslypro.csv.CSVColumn
import spock.lang.Specification
import spock.lang.Unroll

class CSVColumnSpec extends Specification {

    enum TestCSVColumn implements CSVColumn<TestCSVColumn> {
        SIMPLE,
        TWO_WORDS,
        LENGTH(["LEN", "X", "THE_LENGTH"])

        TestCSVColumn(List<String> aliases = []) {
            this.aliases = aliases
        }
    }

    @Unroll
    def 'exact match - column: #column, candiate: #candidate'(CSVColumn column, String candidate, boolean expectedResult) {
        expect:
            column.matches(candidate) == expectedResult

        and:
            (TestCSVColumn.fromString(TestCSVColumn, candidate) == column) == expectedResult

        where:
            column | candidate | expectedResult
            TestCSVColumn.SIMPLE | "SIMPLE" | true
            TestCSVColumn.SIMPLE | "Simple" | true
            TestCSVColumn.SIMPLE | "simple" | true
            TestCSVColumn.SIMPLE | " simple " | false
            TestCSVColumn.TWO_WORDS | "TWO_WORDS" | true
    }

    @Unroll
    def 'alias match - column: #column, candiate: #candidate'(CSVColumn column, String candidate, boolean expectedResult) {
        expect:
            column.matches(candidate) == expectedResult

        and:
            (TestCSVColumn.fromString(TestCSVColumn, candidate) == column) == expectedResult

        where:
            column | candidate | expectedResult
            TestCSVColumn.LENGTH | "LENGTH" | true
            TestCSVColumn.LENGTH | "LEN" | true
            TestCSVColumn.LENGTH | "X" | true
            TestCSVColumn.LENGTH | "THE LENGTH" | true
            TestCSVColumn.LENGTH | "THE_LENGTH" | true
    }
}
