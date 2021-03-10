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
    boolean separateMount = false   // false = allow other components on other heads, true = pick then place this component only, other heads empty.  - useful when placing large or wobbly components so that picking/placing another part on the other heads does not affect the component on the other head.

    int head = 1                    // >= 1

    int placeSpeedPercentage = 100
    BigDecimal placeDelay = 0              // how long to wait after extending the pick nozzle when PLACING component, 0-5 seconds, resolution of 0.01 seconds

    BigDecimal takeHeight = 0              // 0-5mm (2.0 = 200 in DPV file)

    BigDecimal takeDelay = 0        // how long to wait after extending the pick nozzle when PICKING component, 0-3 seconds, resolution of 0.01 seconds.
                                    // using short takeDelay of around 0.25 seconds can prevent small components bouncing out of the tape.

    BigDecimal packageAngle = 0.0            // degrees, >= 0 < 360 - CLOCKWISE, relative to feeder angle

    // tape settings
    BigDecimal tapeSpacing = 4             // mm
    int pullSpeed = 0
}
