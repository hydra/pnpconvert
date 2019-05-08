package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

enum PCBSide {
    TOP,
    BOTTOM
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class ComponentPlacement {
    boolean enabled = true
    String refdes
    String name
    String value
    String pattern
    Coordinate coordinate
    PCBSide side
    BigDecimal rotation
}
