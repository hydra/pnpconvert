package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true, includePackage = false)
class Tray {
    String name
    BigDecimal firstComponentX
    BigDecimal firstComponentY
    BigDecimal lastComponentX
    BigDecimal lastComponentY
    int rows
    int columns
    int firstComponentIndex // 0 based
}
