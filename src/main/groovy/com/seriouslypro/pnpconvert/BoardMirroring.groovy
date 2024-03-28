package com.seriouslypro.pnpconvert

class BoardMirroring {
    Mirroring.Mode mode = Mirroring.Mode.NONE
    Coordinate origin = new Coordinate(x: 0.0G, y: 0.0G)

    Coordinate applyMirroring(Coordinate coordinate) {

        Coordinate m = null // CLOVER assignment needed to prevent 'EmptyExpression.INSTANCE is immutable' error

        if (mode == Mirroring.Mode.HORIZONTAL) {
            m = new Coordinate(x: coordinate.x, y: origin.y - (coordinate.y - origin.y))
        } else if (mode == Mirroring.Mode.VERTICAL) {
            m = new Coordinate(x: origin.x - (coordinate.x - origin.x), y: coordinate.y)
        } else if (mode == Mirroring.Mode.BOTH) {
            m = new Coordinate(x: origin.x - (coordinate.x - origin.x), y: origin.y - (coordinate.y - origin.y))
        } else {
            m = coordinate
        }
        return m
    }
}
