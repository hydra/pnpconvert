package com.seriouslypro.pnpconvert

import java.awt.Color


class DiptraceComponentPlacementTransformer implements ComponentPlacementTransformer {

    SVGRenderer renderer = new SVGRenderer()
    String outputPrefix
    BoardRotation boardRotation
    BoardMirroring boardMirroring
    Coordinate offset

    DiptraceComponentPlacementTransformer(String outputPrefix, BoardRotation boardRotation, BoardMirroring boardMirroring, Coordinate offset) {
        this.outputPrefix = outputPrefix
        this.boardRotation = boardRotation
        this.boardMirroring = boardMirroring
        this.offset = offset
    }

    @Override
    ComponentPlacement process(ComponentPlacement componentPlacement) {
        ComponentPlacement transformedComponentPlacement = transformAndRender(renderer, componentPlacement)

        return transformedComponentPlacement
    }

    @Override
    void close() {
        String svgFileName = outputPrefix + ".svg"
        renderer.save(svgFileName)
    }

    private ComponentPlacement transformAndRender(SVGRenderer renderer, ComponentPlacement componentPlacement) {

        // render original position
        renderer.drawPart(Color.RED, componentPlacement.coordinate, componentPlacement.refdes, componentPlacement.rotation)

        // apply board mirroring
        Coordinate mirroredCoordinate = boardMirroring.applyMirroring(componentPlacement.coordinate)
        BigDecimal mirroredRotation
        if (boardMirroring.mode != Mirroring.Mode.NONE) {
            mirroredRotation = 360 - componentPlacement.rotation.remainder(360)
        } else {
            mirroredRotation = componentPlacement.rotation
        }
        renderer.drawPart(Color.YELLOW, mirroredCoordinate, componentPlacement.refdes, mirroredRotation)

        // apply board rotation
        Coordinate rotatedCoordinate = boardRotation.applyRotation(mirroredCoordinate)
        BigDecimal rotatedRotation = (mirroredRotation + boardRotation.degrees).remainder(360)
        renderer.drawPart(Color.BLUE, rotatedCoordinate, componentPlacement.refdes, rotatedRotation)


        // apply board origin
        Coordinate relocatedCoordinate = rotatedCoordinate - boardRotation.origin
        renderer.drawPart(Color.PINK, rotatedCoordinate, componentPlacement.refdes, rotatedRotation)

        // apply offset
        Coordinate relocatedCoordinateWithOffset = relocatedCoordinate + offset
        renderer.drawPart(Color.GREEN, relocatedCoordinateWithOffset, componentPlacement.refdes, rotatedRotation)

        ComponentPlacement transformedComponentPlacement = new ComponentPlacement(
            refdes: componentPlacement.refdes,
            pattern: componentPlacement.pattern,
            coordinate: relocatedCoordinateWithOffset,
            side: componentPlacement.side,
            rotation: rotatedRotation,
            value: componentPlacement.value,
            name: componentPlacement.name
        )

        transformedComponentPlacement
    }

}
