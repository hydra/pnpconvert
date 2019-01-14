package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

// right-handed coordinate system
// x+ = right
// y+  = up
class CoordinateSpec extends Specification {

    @Unroll
    def 'relocate (0, 0) - degrees: #degrees, offset: (#offsetX, #offsetY)'(BigDecimal degrees, BigDecimal offsetX, BigDecimal offsetY, Coordinate expectedCoordinate) {
        given:
            Coordinate c = new Coordinate(x: 0, y: 0)

        when:
            Coordinate result = c.relocate(degrees, offsetX, offsetY)

        then:
            result == expectedCoordinate

        where:
            degrees | offsetX | offsetY | expectedCoordinate
            0       | 0       | 0       | new Coordinate(x: 0, y: 0)
            0       | 1       | 1       | new Coordinate(x: 1, y: 1)
            90      | 1       | 1       | new Coordinate(x: 1, y: -1)
            180     | 1       | 1       | new Coordinate(x: -1, y: -1)
            270     | 1       | 1       | new Coordinate(x: -1, y: 1)
            45      | 1       | 1       | new Coordinate(x: 1.41G, y: 0.00G)
    }

    @Unroll
    def "relocate (#x, #y) - degrees: #degrees, offset: (#offsetX, #offsetY)"() {
        given:
            Coordinate expectedCoordinate = new Coordinate(
                x: expectedX,
                y: expectedY
            )
        expect:
            expectedCoordinate == new Coordinate(x: x, y: y).relocate(degrees, offsetX, offsetY)

        where:
            x   | y    | degrees | offsetX | offsetY | expectedX | expectedY
            0   | 0    | 0       | 1       | 1       | 1         | 1
            1   | 1    | 0       | 0       | 0       | 1         | 1
            1   | 1    | 0       | 1       | 1       | 2         | 2
            1   | 1    | 90      | 1       | 1       | 2         | 0
            1   | 1    | 180     | 1       | 1       | 0         | 0
            1   | 1    | 270     | 1       | 1       | 0         | 2
    }
}