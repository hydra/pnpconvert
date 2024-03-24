package com.seriouslypro.pnpconvert

// right-handed coordinate system
// x+ = right
// y+  = up
class Board {
    Coordinate origin = new Coordinate(x: 0, y: 0)
    Coordinate exportOffset = new Coordinate(x: 0, y: 0)

    // for a board with the origin other than lower-left, the origin to lower-left distance must be given
    // e.g. for a 10x10 board, with the origin in the center at (0,0), the offset would be (-5, -5)
    // e.g. for a 10x10 board, with the origin upper left at (0,10), the offset would be (0, -10)
    // you can verify or find it by measuring from the origin point to the lower left of the board in the EDA tool
    Coordinate bottomLeftOffset = new Coordinate(x: 0, y: 0)

    BigDecimal width = 0.0G // width/X
    BigDecimal height = 0.0G // height/Y
    BigDecimal depth = 0.0G // depth/Z - board depth not used yet, but might be useful when calculating Z axis offset for components
}


