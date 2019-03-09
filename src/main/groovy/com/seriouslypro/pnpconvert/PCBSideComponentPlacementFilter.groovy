package com.seriouslypro.pnpconvert

class PCBSideComponentPlacementFilter implements ComponentPlacementFilter {

    static enum SideExclusions {
        NONE,
        TOP,
        BOTTOM
    }

    final boolean INCLUDE = true
    final boolean EXCLUDE = false

    SideExclusions sideExclusion = SideExclusions.NONE


    @Override
    boolean shouldFilter(ComponentPlacement componentPlacement) {
        if (
            (componentPlacement.side == PCBSide.TOP && sideExclusion == SideExclusions.TOP) ||
            (componentPlacement.side == PCBSide.BOTTOM && sideExclusion == SideExclusions.BOTTOM)
        ) {
            return EXCLUDE
        }

        return INCLUDE
    }
}
