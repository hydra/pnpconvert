package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode

class Coordinate {
    BigDecimal x = 0.00G
    BigDecimal y = 0.00G

    int scale = 2
    int roundingMode = java.math.BigDecimal.ROUND_HALF_UP;

    Coordinate relocate(BigDecimal angle, BigDecimal offsetX, BigDecimal offsetY) {

        def radians = Math.toRadians(angle)
        float s = Math.sin(radians)
        float c = Math.cos(radians)

        Coordinate p = new Coordinate(
            x: offsetX,
            y: offsetY
        )

        Coordinate r = new Coordinate(
            x: new BigDecimal(p.x * c + p.y * s).setScale(scale, roundingMode),
            y: new BigDecimal(-p.x * s + p.y * c).setScale(scale, roundingMode)
        )

        return this.plus(r)
    }

    Coordinate plus(Coordinate other) {
        return new Coordinate(x: x + other.x, y: y + other.y)
    }

    Coordinate minus(Coordinate other) {
        return new Coordinate(x: x - other.x, y: y - other.y)
    }

    String toString() {
        return "[x: $x, y: $y]"
    }
}
