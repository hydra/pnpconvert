package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVHeader
import com.seriouslypro.pnpconvert.CSVHeaderParser

import java.text.ParseException

class DipTraceHeaderParser implements CSVHeaderParser<DipTraceCSVHeaders> {

    private Map<DipTraceCSVHeaders, CSVHeader> createHeaderMappings(String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                DipTraceCSVHeaders dipTraceCSVHeader = parseHeader(headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[dipTraceCSVHeader] = csvHeader
            } catch (IllegalArgumentException) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    DipTraceCSVHeaders parseHeader(String headerValue) {
        DipTraceCSVHeaders dipTraceCSVHeader = DipTraceCSVHeaders.fromString(headerValue)
        dipTraceCSVHeader
    }

    private void verifyRequiredHeadersPresent(Map<DipTraceCSVHeaders, CSVHeader> dipTraceCSVHeadersCSVHeaderMap, String[] headerValues) {
        def requiredDipTraceCSVHeaders = [
                DipTraceCSVHeaders.REFDES,
                DipTraceCSVHeaders.PATTERN,
                DipTraceCSVHeaders.X,
                DipTraceCSVHeaders.Y,
                DipTraceCSVHeaders.SIDE,
                DipTraceCSVHeaders.ROTATE,
                DipTraceCSVHeaders.VALUE,
                DipTraceCSVHeaders.NAME
        ]

        boolean haveRequiredHeaders = dipTraceCSVHeadersCSVHeaderMap.keySet().containsAll(
                requiredDipTraceCSVHeaders
        )

        if (!haveRequiredHeaders) {
            String requiredHeaders = requiredDipTraceCSVHeaders.collect {
                it.value
            }.toArray().join(',')

            throw new ParseException("Input CSV file does not contail all required headers, required: '$requiredHeaders', found: '$headerValues'", 0)
        }
    }

    @Override
    Map<DipTraceCSVHeaders, CSVHeader> parseHeaders(String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(headerValues)

        verifyRequiredHeadersPresent(headerMappings, headerValues)

        headerMappings
    }
}
