package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVColumn

enum DipTracePlacementsCSVHeaders implements CSVColumn<DipTracePlacementsCSVHeaders> {
    REFDES(["RefDes"]),
    PATTERN(["Pattern"]),
    X(["X (mm)", "Center X (mm)"]),
    Y(["Y (mm)", "Center Y (mm)"]),
    SIDE(["Side"]),
    ROTATE(["Rotate", "Rotation"]),
    VALUE(["Value"]),
    NAME(["Name"])

    DipTracePlacementsCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}
