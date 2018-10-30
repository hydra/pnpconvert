package com.seriouslypro.pnpconvert

interface CSVHeaderParser {
    void parse(String[] headerValues)

    Map<Object, CSVHeader> getHeaderMappings()
}
