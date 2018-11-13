package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Coordinate {
    BigDecimal x = 0.0G
    BigDecimal y = 0.0G

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
