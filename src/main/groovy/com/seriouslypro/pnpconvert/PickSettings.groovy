package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PickSettings {
    // mm
    BigDecimal xOffset = 0
    // mm
    BigDecimal yOffset = 0

    boolean useVision = true
    boolean checkVacuum = true
    // false = allow other components on other heads, true = pick then place this component only, other heads empty.  - useful when placing large or wobbly components so that picking/placing another part on the other heads does not affect the component on the other head.
    boolean separateMount = false

    // >= 1
    int head = 1

    int placeSpeedPercentage = 100

    // how long to wait after extending the pick nozzle when PLACING component, 0-5 seconds, resolution of 0.01 seconds
    BigDecimal placeDelay = 0

    // 0-5mm (2.0 = 200 in DPV file)
    BigDecimal takeHeight = 0

    // how long to wait after extending the pick nozzle when PICKING component, 0-3 seconds, resolution of 0.01 seconds.
    // using short takeDelay of around 0.25 seconds can prevent small components bouncing out of the tape.
    BigDecimal takeDelay = 0

    // degrees, >= 0 < 360 - CLOCKWISE, relative to feeder angle
    BigDecimal packageAngle = 0.0

    // tape settings
    // mm
    BigDecimal tapeSpacing = 4
    int pullSpeed = 0

    Optional<VisionSettings> visionSettings = Optional.empty()
    Optional<VisionSize> visionSize = Optional.empty()
}

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class VisionSettings {
    int visualThreshold
    int visualRadio
}

/**
 *
 * Used to override the width/length settings from the component.
 * Often the camera only picks up the shiny leads resulting in a difference between the component's maximum width
 * and maximum length and the required vision width/length.
 */
@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class VisionSize {
    BigDecimal width = 0            // mm
    BigDecimal length = 0           // mm
}

