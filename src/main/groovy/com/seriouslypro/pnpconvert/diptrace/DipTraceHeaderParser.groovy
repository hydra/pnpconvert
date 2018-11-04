package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVHeader
import com.seriouslypro.pnpconvert.CSVHeaderParser
import com.seriouslypro.pnpconvert.CSVInput
import com.seriouslypro.pnpconvert.CSVInputContext

import java.text.ParseException

class DipTraceHeaderParser implements CSVHeaderParser<DipTraceCSVHeaders> {

    private Map<DipTraceCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                DipTraceCSVHeaders dipTraceCSVHeader = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[dipTraceCSVHeader] = csvHeader
            } catch (IllegalArgumentException) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    @Override
    DipTraceCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
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

            throw new CSVInput.CSVParseException("Input CSV file does not contain all required headers, required: '$requiredHeaders', found: '$headerValues'")
        }
    }

    @Override
    Map<DipTraceCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        verifyRequiredHeadersPresent(headerMappings, headerValues)

        headerMappings
    }
}
