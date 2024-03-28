package com.seriouslypro.pnpconvert

import groovy.transform.AutoClone
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

enum PCBSide {
    TOP,
    BOTTOM
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
@AutoClone
class ComponentPlacement {
    boolean enabled = true
    String refdes
    String partCode
    String manufacturer
    String name
    String value
    String pattern
    Coordinate coordinate
    PCBSide side
    BigDecimal rotation
    Optional<Integer> optionalJob
}
