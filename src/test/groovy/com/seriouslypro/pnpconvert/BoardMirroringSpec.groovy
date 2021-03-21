package com.seriouslypro.pnpconvert

import spock.lang.Specification
import spock.lang.Unroll

class BoardMirroringSpec extends Specification {

    public static final Coordinate zeroCoordinate = new Coordinate(x: 0.0G, y: 0.0G)

    BoardMirroring boardMirroring

    void setup() {
    }

    def "no mirroring - zero coordinate"() {
        given:
            boardMirroring = new BoardMirroring(mode: Mirroring.Mode.NONE)

        expect:
            zeroCoordinate == boardMirroring.applyMirroring(zeroCoordinate)
    }

    def "both - zero coordinate"() {
        given:
            boardMirroring = new BoardMirroring(mode: Mirroring.Mode.BOTH)

        expect:
            zeroCoordinate == boardMirroring.applyMirroring(zeroCoordinate)
    }

    @Unroll
    def "mirroring with origin (point: #x, #y, mode: #mode, origin: #originX, #originY)"() {
        given:
            boardMirroring = new BoardMirroring(mode: mode, origin: new Coordinate(x: originX, y: originY))
            Coordinate expectedCoordinate = new Coordinate(
                x: expectedX,
                y: expectedY
            )
        expect:
            expectedCoordinate == boardMirroring.applyMirroring(new Coordinate(x: x, y: y))

        where:
            x   | y   | mode                      | originX | originY | expectedX | expectedY
            0   | 0   | Mirroring.Mode.NONE       | 0       | 0       | 0         | 0
            0   | 1   | Mirroring.Mode.HORIZONTAL | 0       | 0       | 0         | -1
            0   | 2   | Mirroring.Mode.HORIZONTAL | 0       | 1       | 0         | 0
            0   | 10  | Mirroring.Mode.HORIZONTAL | 0       | 5       | 0         | 0
            0   | 0   | Mirroring.Mode.HORIZONTAL | 0       | 5       | 0         | 10
            1   | 0   | Mirroring.Mode.VERTICAL   | 0       | 0       | -1        | 0
            2   | 0   | Mirroring.Mode.VERTICAL   | 1       | 0       | 0         | 0
            10  | 0   | Mirroring.Mode.VERTICAL   | 5       | 0       | 0         | 0
            0   | 0   | Mirroring.Mode.VERTICAL   | 5       | 0       | 10        | 0
            1   | 1   | Mirroring.Mode.BOTH       | 0       | 0       | -1        | -1
            2   | 2   | Mirroring.Mode.BOTH       | 1       | 1       | 0         | 0
            10  | 10  | Mirroring.Mode.BOTH       | 5       | 5       | 0         | 0
            0   | 0   | Mirroring.Mode.BOTH       | 5       | 5       | 10        | 10
    }

}
