package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.csv.CSVHeader
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext

class DipTraceHeaderParser implements CSVHeaderParser<DipTraceCSVHeaders> {

    private Map<DipTraceCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<DipTraceCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                DipTraceCSVHeaders dipTraceCSVHeader = parseHeader(context, headerValue)
                CSVHeader csvHeader = new CSVHeader()
                csvHeader.index = index
                headerMappings[dipTraceCSVHeader] = csvHeader
            } catch (IllegalArgumentException ignored) {
                // ignore unknown header
            }
        }
        headerMappings
    }

    @Override
    DipTraceCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        DipTraceCSVHeaders dipTraceCSVHeader = DipTraceCSVHeaders.fromString(DipTraceCSVHeaders, headerValue)
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
                it.name()
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
