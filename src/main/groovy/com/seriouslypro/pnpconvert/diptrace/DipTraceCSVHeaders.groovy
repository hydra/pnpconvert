package com.seriouslypro.pnpconvert.diptrace

enum DipTraceCSVHeaders {
    REFDES("RefDes"),
    PATTERN("Pattern"),
    X("X (mm)"),
    Y("Y (mm)"),
    SIDE("Side"),
    ROTATE("Rotate"),
    VALUE("Value"),
    NAME("Name")

    private final String value

    DipTraceCSVHeaders(String headerValue) {
        this.value = headerValue
    }

    static DipTraceCSVHeaders fromString(String headerValue) {
        def result = values().find { it.value == headerValue }
        if (result == null) {
            throw new IllegalArgumentException("Unknown header value '$headerValue'")
        }
        result
    }
}
