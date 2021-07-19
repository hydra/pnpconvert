package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVHeader
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext

class DipTracePlacementsHeaderParser implements CSVHeaderParser<DipTracePlacementsCSVHeaders> {

    private Map<DipTracePlacementsCSVHeaders, CSVHeader> createHeaderMappings(CSVInputContext context, String[] headerValues) {
        Map<DipTracePlacementsCSVHeaders, CSVHeader> headerMappings = [:]
        headerValues.eachWithIndex { String headerValue, Integer index ->
            try {
                DipTracePlacementsCSVHeaders dipTraceCSVHeader = parseHeader(context, headerValue)
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
    DipTracePlacementsCSVHeaders parseHeader(CSVInputContext context, String headerValue) {
        DipTracePlacementsCSVHeaders dipTraceCSVHeader = DipTracePlacementsCSVHeaders.fromString(DipTracePlacementsCSVHeaders, headerValue)
        dipTraceCSVHeader
    }

    private void verifyRequiredHeadersPresent(Map<DipTracePlacementsCSVHeaders, CSVHeader> dipTraceCSVHeadersCSVHeaderMap, String[] headerValues) {
        def requiredDipTraceCSVHeaders = [
            DipTracePlacementsCSVHeaders.REFDES,
            DipTracePlacementsCSVHeaders.PATTERN,
            DipTracePlacementsCSVHeaders.X,
            DipTracePlacementsCSVHeaders.Y,
            DipTracePlacementsCSVHeaders.SIDE,
            DipTracePlacementsCSVHeaders.ROTATE,
            DipTracePlacementsCSVHeaders.VALUE,
            DipTracePlacementsCSVHeaders.NAME
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
    Map<DipTracePlacementsCSVHeaders, CSVHeader> parseHeaders(CSVInputContext context, String[] headerValues) {
        Map<DipTracePlacementsCSVHeaders, CSVHeader> headerMappings = createHeaderMappings(context, headerValues)

        verifyRequiredHeadersPresent(headerMappings, headerValues)

        headerMappings
    }
}
