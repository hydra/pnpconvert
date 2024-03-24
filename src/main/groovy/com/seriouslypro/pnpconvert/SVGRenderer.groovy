package com.seriouslypro.pnpconvert

import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.Element

import java.awt.Color
import java.awt.Font
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

class SVGRenderer {

    SVGGraphics2D svgGenerator

    int scale = 10

    int refdesFontSize = 1 * scale
    Font refdesFont

    int fiducialFontSize = 1 * scale
    Font fiducialFont

    int pcbFontSize = 1 * scale
    Font pcbFont

    SVGRenderer() {

        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();

        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null)

        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document)
        ctx.setEmbeddedFontsOn(true)

        svgGenerator = new SVGGraphics2D(ctx, false)
        refdesFont = new Font(Font.MONOSPACED, Font.PLAIN, refdesFontSize)
        fiducialFont = new Font(Font.MONOSPACED, Font.PLAIN, fiducialFontSize)
        pcbFont = new Font(Font.MONOSPACED, Font.PLAIN, pcbFontSize)
    }

    void drawPart(Coordinate coordinate, Color color, String refdes, BigDecimal rotation) {
        BigDecimal pointSize = 1 * scale
        BigDecimal x = (coordinate.x * scale)
        BigDecimal y = -(coordinate.y * scale)

        //
        // origin
        //
        svgGenerator.setColor(color)
        svgGenerator.drawOval(x - (pointSize / 2) as int, y - (pointSize / 2) as int, pointSize as int, pointSize as int)
        drawPlus(coordinate, color, 1)

        //
        // rotation indicator
        //
        Double radians = Math.toRadians(rotation as Double)
        Double radius1 = pointSize / 2 - 1;
        Double radius2 = pointSize / 2 + 1;

        int x1 = x + (radius1 * Math.cos(radians)) as int
        int y1 = y + (radius1 * Math.sin(radians)) as int

        int x2 = x + (radius2 * Math.cos(radians)) as int
        int y2 = y + (radius2 * Math.sin(radians)) as int

        svgGenerator.drawLine(x as int, y as int, x2, y2)

        //
        // refdes
        //
        Point2D refdesOffset = new Point2D.Double(x: 1 * scale, y: 1)
        drawStringCentered(coordinate, refdes, color, refdesFont, refdesOffset)
    }

    private void drawStringCentered(Coordinate coordinate, String string, Color color, Font font, Point2D offset) {
        BigDecimal x = (coordinate.x * scale)
        BigDecimal y = -(coordinate.y * scale)
        svgGenerator.setColor(color)
        svgGenerator.setFont(font)
        int baseline = font.getBaselineFor(string.charAt(0))

        Rectangle2D bounds = fiducialFont.getStringBounds(string, 0, string.length(), svgGenerator.fontRenderContext)

        //svgGenerator.drawRect(x + offset.x as int, y - offset.y - (bounds.height / 2) as int, bounds.width as int, bounds.height as int)
        svgGenerator.drawString(string, x + offset.x as int, y - offset.y  - (bounds.height / 2) + baseline - bounds.y as int)
    }

    void drawFiducials(Optional<List<Fiducial>> optionalFiducials, Color color) {
        if (!optionalFiducials.present) {
            return
        }

        List<Fiducial> fiducialList = optionalFiducials.get()

        fiducialList.each { fiducial ->
            drawFiducial(fiducial, color)
        }
    }

    void drawFiducial(Fiducial fiducial, Color color) {

        //
        // mark
        //

        drawPlus(fiducial.coordinate, color, 1)
        drawCross(fiducial.coordinate, color, 1)

        //
        // note
        //
        Point2D noteOffset = new Point2D.Double(x: 1 * scale, y: 1)
        drawStringCentered(fiducial.coordinate, fiducial.note, color, fiducialFont, noteOffset)
    }

    void drawPanel(Optional<Panel> optionalPanel, Board board, Color color) {
        if (!optionalPanel.present) {
            return
        }

        Panel panel = optionalPanel.get()
        BigDecimal panelGapX = panel.gapX(board)
        BigDecimal panelGapY = panel.gapY(board)

        //
        // bounding box, with rails
        //
        svgGenerator.setColor(color)

        BigDecimal panelWidthScaled = (panel.railWidthL + (panel.intervalX * panel.numberX - panelGapX) + panel.railWidthR) * scale
        BigDecimal panelHeightScaled = (panel.railWidthT + (panel.intervalY * panel.numberY - panelGapY) + panel.railWidthB) * scale
        svgGenerator.drawRect(0, 0 - panelHeightScaled as int, panelWidthScaled as int, panelHeightScaled as int)

        //
        // inner area, inside rails
        //
        svgGenerator.setColor(color)

        BigDecimal innerWidthScaled = (panel.intervalX * panel.numberX - panelGapX) * scale
        BigDecimal innerHeightScaled = (panel.intervalY * panel.numberY - panelGapY) * scale
        svgGenerator.drawRect(panel.railWidthL * scale as int, 0 - innerHeightScaled - (panel.railWidthB * scale) as int, innerWidthScaled as int, innerHeightScaled as int)
    }

    void drawBoard(Board board, Color color) {
        svgGenerator.setColor(color)
        drawPlus(board.origin, color, 3)

        svgGenerator.drawRect(
            (board.origin.x + board.bottomLeftOffset.x) * scale as int,
            (board.origin.y + board.bottomLeftOffset.y) * scale as int,
            board.width * scale as int,
            board.height * scale as int
        )
        //svgGenerator.drawRect(board.bottomLeftOffset.x * scale as int, 0 - board.height * scale - board.bottomLeftOffset.y * scale as int, board.width * scale as int, board.height * scale as int)
    }

    void drawPCBs(Optional<Panel> optionalPanel, Board board, Color color) {
        if (!optionalPanel.present) {
            return
        }

        Panel panel = optionalPanel.get()

        for (pass in [0,1]) {
            int number = 0
            for (int indexY = 0; indexY < panel.numberY; indexY += 1) {
                for (int indexX = 0; indexX < panel.numberX; indexX += 1) {

                    int x = (panel.railWidthL + indexX * panel.intervalX) * scale as int
                    int y = (0 - board.height - indexY * panel.intervalY - panel.railWidthB) * scale as int

                    switch (pass) {
                        case 0:
                            //
                            // bounding box
                            //
                            svgGenerator.setColor(color)

                            svgGenerator.drawRect(x, y, board.width * scale as int, board.height * scale as int)
                        break
                        case 1:

                            //
                            // number
                            //
                            String index = "$number ($indexX,$indexY)"

                            Point2D indexOffset = new Point2D.Double(x: 1 * scale, y: 1)
                            drawStringCentered(new Coordinate(x: x / scale, y: -y / scale), index, Color.BLACK, pcbFont, indexOffset)
                            break
                    }

                    number += 1
                }
            }
        }
    }

    private void drawPlus(Coordinate point, Color color, int size) {
        size *= scale
        BigDecimal x = (point.x * scale)
        BigDecimal y = - (point.y * scale)

        svgGenerator.setColor(color)
        svgGenerator.drawLine(x as int, y - (size / 2) as int, x as int, y + (size / 2) as int)
        svgGenerator.drawLine(x - (size / 2) as int, y as int, x + (size / 2) as int, y as int)
    }

    private void drawCross(Coordinate point, Color color, int size) {
        size *= scale
        BigDecimal x = (point.x * scale)
        BigDecimal y = - (point.y * scale)

        svgGenerator.setColor(color)
        svgGenerator.drawLine(x - (size / 2) as int, y - (size / 2) as int, x + (size / 2) as int, y + (size / 2) as int)
        svgGenerator.drawLine(x + (size / 2) as int, y - (size / 2) as int, x - (size / 2) as int, y + (size / 2) as int)
    }

    void drawOrigin(Coordinate origin, Color color) {
        drawPlus(origin, color, 5)
    }

    void save(String svgFileName) {
        Element root = svgGenerator.getRoot()

        List<Integer> viewBox = [-50,-100,300,200]
        viewBox = viewBox.collect { it * scale }

        root.setAttributeNS(null, "viewBox", viewBox.join(' '));

        boolean useCSS = true // we want to use CSS style attributes

        Writer svgFileWriter = new OutputStreamWriter(new FileOutputStream(svgFileName), "UTF-8")

        svgGenerator.stream(root, svgFileWriter, useCSS, false)
        svgFileWriter.close()
    }
}
