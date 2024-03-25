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
    NAME(["Name"]),
    PART_CODE(["Part Code"]),
    MANUFACTURER(["Manufacturer"]),

    // Optional: Can be added manually to allow filtering on job number
    JOB(["Job"])

    DipTracePlacementsCSVHeaders(List<String> aliases = []) {
        this.aliases = aliases
    }
}
