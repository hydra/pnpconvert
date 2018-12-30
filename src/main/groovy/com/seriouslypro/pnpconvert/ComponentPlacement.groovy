package com.seriouslypro.pnpconvert

import groovy.transform.ToString

enum PCBSide {
    TOP,
    BOTTOM
}

@ToString(includeNames = true, includePackage = false)
class ComponentPlacement {
    String refdes
    String name
    String value
    String pattern
    Coordinate coordinate
    PCBSide side
    BigDecimal rotation
}
