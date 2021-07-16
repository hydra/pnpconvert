package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.csv.CSVColumn

enum DipTraceCSVHeaders implements CSVColumn<DipTraceCSVHeaders> {
    REFDES(["RefDes"]),
    PATTERN(["Pattern"]),
    X(["X (mm)"]),
    Y(["Y (mm)"]),
    SIDE(["Side"]),
    ROTATE(["Rotate"]),
    VALUE(["Value"]),
    NAME(["Name"])

    DipTraceCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}
