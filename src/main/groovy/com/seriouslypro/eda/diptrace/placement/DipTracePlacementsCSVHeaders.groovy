package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVColumn

enum DipTracePlacementsCSVHeaders implements CSVColumn<DipTracePlacementsCSVHeaders> {
    REFDES(["RefDes"]),
    PATTERN(["Pattern"]),
    X(["X (mm)"]),
    Y(["Y (mm)"]),
    SIDE(["Side"]),
    ROTATE(["Rotate"]),
    VALUE(["Value"]),
    NAME(["Name"])

    DipTracePlacementsCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}
