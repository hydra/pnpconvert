package com.seriouslypro.pnpconvert

class PickSettings {
    BigDecimal xOffset = 0          // mm
    BigDecimal yOffset = 0          // mm

    boolean useVision = true
    boolean checkVacuum = true

    int head = 1                    // >= 1
    int placeSpeedPercentage = 100
    int placeDelay = 0              // ??
    int takeHeight = 0              // ??

    int packageAngle = 0            // degrees, >= 0 < 360 - CLOCKWISE

    // tape settings
    int tapeSpacing = 4             // mm
    int pullSpeed = 0
}
