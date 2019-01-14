package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class BoardRotationSpec extends Specification {

    // right-handed coordinate system
    // x+ = right
    // y+  = up
    // rotation+ = clockwise

    public static final Coordinate zeroCoordinate = new Coordinate(x: 0.0G, y: 0.0G)
    public static final Coordinate nonzeroCoordinate = new Coordinate(x: 123.0G, y: 321.0G)

    BoardRotation boardRotation

    void setup() {
    }

    def "no rotation - zero coordinate"() {
        given:
            boardRotation = new BoardRotation(degrees: 0)

        expect:
            zeroCoordinate == boardRotation.applyRotation(zeroCoordinate)
    }

    def "no rotation - non-zero coordinate"() {
        given:
            boardRotation = new BoardRotation(degrees: 0)

        expect:
            nonzeroCoordinate == boardRotation.applyRotation(nonzeroCoordinate)
    }

    def "rotation - 90 degrees clockwise"() {
        given:
            boardRotation = new BoardRotation(degrees: 90)
            Coordinate expectedCoordinate = new Coordinate(
                x: 0.0 + nonzeroCoordinate.y,
                y: 0.0 - nonzeroCoordinate.x
            )
        expect:
            expectedCoordinate == boardRotation.applyRotation(nonzeroCoordinate)
    }

    def "rotation - 90 degrees counter-clockwise"() {
        given:
            boardRotation = new BoardRotation(degrees: -90)
            Coordinate expectedCoordinate = new Coordinate(
                    x: 0.0 - nonzeroCoordinate.y,
                    y: 0.0 + nonzeroCoordinate.x
            )
        expect:
            expectedCoordinate == boardRotation.applyRotation(nonzeroCoordinate)
    }

    def "rotation - 45 degrees clockwise"() {
        given:
            boardRotation = new BoardRotation(degrees: 45)
            Coordinate expectedCoordinate = new Coordinate(
                    x: 313.96,
                    y: 140.01
            )
        expect:
            expectedCoordinate == boardRotation.applyRotation(nonzeroCoordinate)
    }

    @Unroll
    def "rotation with origin (point: #x, #y, degrees: #degrees, origin: #originX, #originY)"() {
        given:
            boardRotation = new BoardRotation(degrees: degrees, origin: new Coordinate(x: originX, y: originY))
            Coordinate expectedCoordinate = new Coordinate(
                    x: expectedX,
                    y: expectedY
            )
        expect:
            expectedCoordinate == boardRotation.applyRotation(new Coordinate(x: x, y: y))

        where:
            x  | y  | degrees | originX | originY | expectedX | expectedY
            0  | 0  | 0       | 10      | 10      | 0         | 0
            0  | 0  | 90      | 10      | 10      | 0         | 20
            0  | 0  | 180     | 10      | 10      | 20        | 20
            0  | 0  | 270     | 10      | 10      | 20        | 0
            0  | 0  | 360     | 10      | 10      | 0         | 0
            10 | 20 | 0       | 10      | 10      | 10        | 20
            10 | 20 | 90      | 10      | 10      | 20        | 10
            10 | 20 | 180     | 10      | 10      | 10        | 0
            10 | 20 | 270     | 10      | 10      | 0         | 10
            10 | 20 | 360     | 10      | 10      | 10        | 20
            100 | -100 | 45   | 50      | 50      | -20.71G   | -91.42G
            100 | -100 | -45  | 50      | 50      | 191.42G   | -20.71G
            100 | -100 | 225  | 50      | 50      | 120.71G   | 191.42G
            100 | -100 | -225 | 50      | 50      | -91.42G   | 120.71G
    }
}
