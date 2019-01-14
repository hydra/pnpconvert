package com.seriouslypro.pnpconvert

// right-handed coordinate system
// x+ = right
// y+  = up
class BoardRotation {
    BigDecimal degrees = 0.0G
    Coordinate origin = new Coordinate(x: 0.0G, y: 0.0G)

    Coordinate applyRotation(Coordinate coordinate) {

        Coordinate r = origin.relocate(degrees, coordinate.x - origin.x, coordinate.y - origin.y)

        Coordinate q = new Coordinate(
            x: (r.x).setScale(origin.scale, origin.roundingMode),
            y: (r.y).setScale(origin.scale, origin.roundingMode)
        )

        return q
    }
}
