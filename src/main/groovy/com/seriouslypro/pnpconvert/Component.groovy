package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class Component {
    String partCode
    String manufacturer
    String name
    BigDecimal width = 0            // mm
    BigDecimal length = 0           // mm
    BigDecimal height = 0.5G        // mm
    ArrayList<String> aliases = []
    BigDecimal placementOffsetX = 0 // mm
    BigDecimal placementOffsetY = 0 // mm

    BigDecimal area() {
        return width * length
    }

    boolean hasPartCodeAndManufacturer() {
        partCode && partCode.length() > 0 && manufacturer && manufacturer.length() > 0
    }
}

