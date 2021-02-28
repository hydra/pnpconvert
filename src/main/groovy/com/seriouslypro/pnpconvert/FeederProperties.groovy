package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class FeederProperties {
    BigDecimal feederAngle = 0.0 // degrees, >= 0 < 360, CLOCKWISE, relative to design angle
}
