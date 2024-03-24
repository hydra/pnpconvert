package com.seriouslypro.pnpconvert

import groovy.transform.ToString

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

    BigDecimal width // X
    BigDecimal height // Y

    BigDecimal gapX(Board board) {
        intervalX - board.width
    }

    BigDecimal gapY(Board board) {
        intervalY - board.height
    }
}
