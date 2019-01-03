package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PickSettings {
    BigDecimal xOffset = 0          // mm
    BigDecimal yOffset = 0          // mm

    boolean useVision = true
    boolean checkVacuum = true

    int head = 1                    // >= 1

    int placeSpeedPercentage = 100
    BigDecimal placeDelay = 0              // how long to wait after extending the pick nozzle when PLACING component, 0-5 seconds, resolution of 0.01 seconds

    int takeHeight = 0              // ??

    BigDecimal takeDelay = 0        // how long to wait after extending the pick nozzle when PICKING component, 0-3 seconds, resolution of 0.01 seconds.
                                    // using short takeDelay of around 0.25 seconds can prevent small components bouncing out of the tape.

    int packageAngle = 0            // degrees, >= 0 < 360 - CLOCKWISE, relative to feeder angle

    // tape settings
    int tapeSpacing = 4             // mm
    int pullSpeed = 0
}
