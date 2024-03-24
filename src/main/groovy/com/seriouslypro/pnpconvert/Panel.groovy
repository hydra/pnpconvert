package com.seriouslypro.pnpconvert

import groovy.transform.ToString

/**
 * Reasoning:
 * - board is always designed first
 * - panel comes after board design is complete
 * - gaps only known if there's component overlap or if v-cut or edge-routing is required
 * - rails are specified based on machinery used and sometimes re-calculated after the number/interval is known
 * - width/height only known after the above are defined.
 */
@ToString(includePackage = false)
class Panel {
    BigDecimal intervalX // interval spacing. e.g. 15 for a design of 20mm wide with a gap of 5mm between the right edge of the first design and the left edge of the next.
    BigDecimal intervalY // as above, but for Y axis.
    int numberX // number of designs on X axis.
    int numberY // as above, but for Y axis.
    BigDecimal railWidthT // top/rear rail width
    BigDecimal railWidthB // bottom/fromt rail width
    BigDecimal railWidthL // left rail width
    BigDecimal railWidthR // right rail width

    //
    // computed values
    //
    BigDecimal width // X
    BigDecimal height // Y
    BigDecimal gapX
    BigDecimal gapY

    void applyBoard(Board board) {
        gapX = intervalX - board.width
        gapY = intervalY - board.height

        width = railWidthL + (intervalX * numberX) - gapX + railWidthR
        height = railWidthB + (intervalY * numberY) - gapY + railWidthT
    }
}
