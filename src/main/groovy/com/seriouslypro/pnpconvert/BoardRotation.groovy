package com.seriouslypro.pnpconvert

// right-handed coordinate system
// x+ = right
// y+  = up
class BoardRotation {
    BigDecimal degrees = 0.0G
    Coordinate origin = new Coordinate(x: 0.0G, y: 0.0G)
    int scale = 2
    int roundingMode = java.math.BigDecimal.ROUND_HALF_UP;

    Coordinate applyRotation(Coordinate coordinate) {

        def radians = Math.toRadians(degrees)
        float s = Math.sin(radians)
        float c = Math.cos(radians)

        Coordinate p = new Coordinate(
                x: coordinate.x - origin.x,
                y: coordinate.y - origin.y
        )

        Coordinate r = new Coordinate(
            x: p.x * c + p.y * s,
            y: -p.x * s + p.y * c
        )

        Coordinate q = new Coordinate(
            x: (r.x + origin.x).setScale(scale, roundingMode),
            y: (r.y + origin.y).setScale(scale, roundingMode)
        )

        return q
    }
}
