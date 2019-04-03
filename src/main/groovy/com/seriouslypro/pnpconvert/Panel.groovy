package com.seriouslypro.pnpconvert

import groovy.transform.ToString

@ToString(includePackage = false)
class Panel {
    int intervalX // interval spacing. e.g. 15 for a design of 20mm wide with a gap of 5mm between the right edge of the first design and the left edge of the next.
    int intervalY // as above, but for Y axis.
    int numberX // number of designs on X axis.
    int numberY // as above, but for Y axis.
}
