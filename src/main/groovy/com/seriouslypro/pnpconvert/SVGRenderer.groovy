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

    void drawPart(Color color, Coordinate coordinate, String refdes) {
        int pointSize = 2
        int x = coordinate.x
        int y = coordinate.y

        svgGenerator.setColor(color)
        svgGenerator.drawOval(x - (pointSize / 2) as int, -y - (pointSize / 2) as int, pointSize, pointSize)
        svgGenerator.setFont(refdesFont)
        int baseline = refdesFont.getBaselineFor(refdes.charAt(0))
        svgGenerator.drawString(refdes, x + pointSize, -y - baseline + ((refdesFontSize / 2) as int) - ((pointSize / 2) as int))
    }


    void save(String svgFileName) {
        Element root = svgGenerator.getRoot();
        root.setAttributeNS(null, "viewBox", "-125 -125 250 250");

        boolean useCSS = true; // we want to use CSS style attributes

        Writer svgFileWriter = new OutputStreamWriter(new FileOutputStream(svgFileName), "UTF-8");

        svgGenerator.stream(root, svgFileWriter, useCSS, false);
        svgFileWriter.close()
    }

}
