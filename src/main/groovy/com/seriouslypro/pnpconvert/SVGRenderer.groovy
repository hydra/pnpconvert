package com.seriouslypro.pnpconvert

import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen.SVGGeneratorContext
import org.apache.batik.svggen.SVGGraphics2D
import org.w3c.dom.DOMImplementation
import org.w3c.dom.Document
import org.w3c.dom.Element

import java.awt.Color
import java.awt.Font

class SVGRenderer {

    SVGGraphics2D svgGenerator

    int scale = 2

    int refdesFontSize = 4
    Font refdesFont

    SVGRenderer() {

        DOMImplementation domImpl =
                GenericDOMImplementation.getDOMImplementation();

        String svgNS = "http://www.w3.org/2000/svg";
        Document document = domImpl.createDocument(svgNS, "svg", null)

        SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document)
        ctx.setEmbeddedFontsOn(true)

        svgGenerator = new SVGGraphics2D(ctx, false)
        refdesFont = new Font(Font.MONOSPACED, Font.PLAIN, refdesFontSize)
    }

    void drawPart(Color color, Coordinate coordinate, String refdes, BigDecimal rotation) {
        int pointSize = 2
        int x = (coordinate.x * scale)
        int y = - (coordinate.y * scale)

        //
        // origin
        //
        svgGenerator.setColor(color)
        svgGenerator.drawOval(x - (pointSize / 2) as int, y - (pointSize / 2) as int, pointSize, pointSize)

        //
        // rotation indicator
        //
        Float radians = Math.toRadians(rotation as Double)
        Float radius1 = pointSize - 1;
        Float radius2 = pointSize;

        int x1 = x + (radius1 * Math.cos(radians).round())
        int y1 = y + (radius1 * Math.sin(radians).round())

        int x2 = x + (radius2 * Math.cos(radians).round())
        int y2 = y + (radius2 * Math.sin(radians).round())


        svgGenerator.drawLine(x1, y1, x2, y2)

        //
        // refdes
        //
        svgGenerator.setFont(refdesFont)
        int baseline = refdesFont.getBaselineFor(refdes.charAt(0))
        svgGenerator.drawString(refdes, x + pointSize, y - baseline + ((refdesFontSize / 2) as int) - ((pointSize / 2) as int))

    }


    void save(String svgFileName) {
        Element root = svgGenerator.getRoot();
        List<Integer> viewBox = [-100,-100,200,200]
        viewBox = viewBox.collect { it * scale }

        root.setAttributeNS(null, "viewBox", viewBox.join(' '));

        boolean useCSS = true; // we want to use CSS style attributes

        Writer svgFileWriter = new OutputStreamWriter(new FileOutputStream(svgFileName), "UTF-8");

        svgGenerator.stream(root, svgFileWriter, useCSS, false);
        svgFileWriter.close()
    }
}
