package com.seriouslypro.pnpconvert

import java.awt.Color


class DiptraceComponentPlacementTransformer implements ComponentPlacementTransformer {

    SVGRenderer renderer
    Board board
    BoardRotation boardRotation
    BoardMirroring boardMirroring
    Coordinate offset
    Optional<Panel> optionalPanel

    DiptraceComponentPlacementTransformer(SVGRenderer renderer, Board board, BoardRotation boardRotation, BoardMirroring boardMirroring, Coordinate offset, Optional<Panel> optionalPanel) {
        this.renderer = renderer
        this.board = board
        this.optionalPanel = optionalPanel
        this.boardRotation = boardRotation
        this.boardMirroring = boardMirroring
        this.offset = offset
    }

    @Override
    ComponentPlacement process(ComponentPlacement componentPlacement) {
        ComponentPlacement transformedComponentPlacement = transformAndRender(renderer, componentPlacement)

        return transformedComponentPlacement
    }

    private ComponentPlacement transformAndRender(SVGRenderer renderer, ComponentPlacement componentPlacement) {

        Coordinate coordinate = componentPlacement.coordinate
        BigDecimal rotation = componentPlacement.rotation

        // render original position
        renderer.drawPart(coordinate, Color.RED, componentPlacement.refdes, rotation)

        // apply board to bottom left and EDA export offset
        coordinate = coordinate + board.bottomLeftOffset
        coordinate = coordinate - board.exportOffset

        renderer.drawPart(coordinate, Color.LIGHT_GRAY, componentPlacement.refdes, rotation)

        Coordinate centerOffset = new Coordinate(
            x: board.bottomLeftOffset.x + board.width / 2,
            y: board.bottomLeftOffset.y + board.height / 2,
        )
        coordinate = coordinate - centerOffset
        renderer.drawPart(coordinate, Color.DARK_GRAY, componentPlacement.refdes, rotation)

        // apply board mirroring
        coordinate = boardMirroring.applyMirroring(coordinate)

        if (boardMirroring.mode != Mirroring.Mode.NONE) {
            rotation = 360.0 - componentPlacement.rotation.remainder(360.0)
        }
        renderer.drawPart(coordinate, Color.YELLOW, componentPlacement.refdes, rotation)

        // apply board rotation
        coordinate = boardRotation.applyRotation(coordinate)
        rotation = (rotation + boardRotation.degrees).remainder(360.0)
        renderer.drawPart(coordinate, Color.BLUE, componentPlacement.refdes, rotation)

        // apply board origin
        coordinate = coordinate - boardRotation.origin
        renderer.drawPart(coordinate, Color.PINK, componentPlacement.refdes, rotation)

        // undo board to bottom left and center offsets
        coordinate = coordinate + centerOffset
        coordinate = coordinate - board.bottomLeftOffset
        renderer.drawPart(coordinate, Color.CYAN, componentPlacement.refdes, rotation)

        // apply panel offset
        if (optionalPanel.present) {
            Panel panel = optionalPanel.get()
            Coordinate panelOffset = new Coordinate(
                x: panel.railWidthL,
                y: panel.railWidthB,
            )

            coordinate = coordinate + panelOffset
            renderer.drawPart(coordinate, Color.MAGENTA, componentPlacement.refdes, rotation)
        }


        // apply offset
        coordinate = coordinate + offset
        renderer.drawPart(coordinate, Color.GREEN, componentPlacement.refdes, rotation)

        ComponentPlacement transformedComponentPlacement = new ComponentPlacement(
            refdes: componentPlacement.refdes,
            pattern: componentPlacement.pattern,
            coordinate: coordinate,
            side: componentPlacement.side,
            rotation: rotation,
            value: componentPlacement.value,
            name: componentPlacement.name
        )

        transformedComponentPlacement
    }

}
