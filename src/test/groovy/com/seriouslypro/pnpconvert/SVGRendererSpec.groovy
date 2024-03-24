package com.seriouslypro.pnpconvert

import spock.lang.Specification

import java.awt.Color

class SVGRendererSpec extends Specification {

    // FUTURE currently requires visual inspection of artifacts
    def draw() {
        given:
            SVGRenderer renderer = new SVGRenderer()

        and:
            Board board = new Board(
                origin: new Coordinate(x: 0.0G, y: 0.0G),
                exportOffset: new Coordinate(x: 0.0G, y: 0.0G),
                bottomLeftOffset: new Coordinate(x: -25.0G, y: -12.5G),
                width: 50.0G,
                height: 25.0G,
            )

        and:
            Panel panel = new Panel(
                intervalX: board.width + 2.0G, // 2.0mm horizontal gap between designs in panel
                intervalY: board.height + 4.0G, // 2.0mm vertical gap between designs in panel

                numberX: 3,
                numberY: 2,

                // using different widths, so it's easy to verify the result
                railWidthL: 6,
                railWidthR: 3,
                railWidthT: 8,
                railWidthB: 4,

                width: 163.0G, // 6 + 50 + 2 + 50 + 2 + 50 + 3
                height: 66.0G, // 4 + 25 + 4 + 25 + 8
            )
            Optional<Panel> optionalPanel = Optional.of(panel)

        and:
            Optional<List<Fiducial>> optionalFiducials = Optional.of([
                new Fiducial(note: "FL", coordinate: new Coordinate(x: 10.0G, y: (panel.railWidthB / 2))),
                new Fiducial(note: "FR", coordinate: new Coordinate(x: panel.width - 10.0G, y: (panel.railWidthB / 2))),
                new Fiducial(note: "RL", coordinate: new Coordinate(x: 10.0G, y: panel.height - (panel.railWidthT / 2))),
                new Fiducial(note: "RR", coordinate: new Coordinate(x: panel.width - 15.0G, y: panel.height - (panel.railWidthT / 2))),
            ])

        and:
            Coordinate origin = new Coordinate(x: 0, y: 0)

        and:
            def fileName = "out/svg-renderer-spec/draw.svg"
            new File(fileName).parentFile.mkdirs()

        when:
            renderer.drawBoard(board, Color.DARK_GRAY)

            renderer.drawPanel(optionalPanel, board, Color.MAGENTA)
            renderer.drawPCBs(optionalPanel, board, Color.GREEN)
            renderer.drawFiducials(optionalFiducials, Color.ORANGE)
            renderer.drawOrigin(origin, Color.BLUE)

            renderer.drawPart(new Coordinate(x: panel.railWidthL + 12.5G, y: panel.railWidthB + 6.25G), Color.RED, "D1", 135.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 25.0G, y: panel.railWidthB + 6.25G), Color.RED, "D2", 90.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 37.5G, y: panel.railWidthB + 6.25G), Color.RED, "D3", 45.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 12.5G, y: panel.railWidthB + 12.5G), Color.RED, "D4", 180.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 25.0G, y: panel.railWidthB + 12.5G), Color.RED, "D5", 0.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 37.5G, y: panel.railWidthB + 12.5G), Color.RED, "D6", 360.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 12.5G, y: panel.railWidthB + 18.75G), Color.RED, "D7", 225.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 25.0G, y: panel.railWidthB + 18.75G), Color.RED, "D8", 270.0G)
            renderer.drawPart(new Coordinate(x: panel.railWidthL + 37.5G, y: panel.railWidthB + 18.75G), Color.RED, "D9", 315.0G)
            renderer.save(fileName)

        then:
            noExceptionThrown()
    }
}
