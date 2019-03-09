package com.seriouslypro.pnpconvert

class PCBSideComponentPlacementFilter implements ComponentPlacementFilter {

    static enum SideInclusion {
        ALL,
        TOP,
        BOTTOM
    }

    final boolean INCLUDE = true
    final boolean EXCLUDE = false

    SideInclusion sideInclusion = SideInclusion.ALL

    @Override
    boolean shouldFilter(ComponentPlacement componentPlacement) {
        boolean include = sideInclusion == SideInclusion.ALL || (
            (componentPlacement.side == PCBSide.TOP && sideInclusion == SideInclusion.TOP) ||
            (componentPlacement.side == PCBSide.BOTTOM && sideInclusion == SideInclusion.BOTTOM)
        )

        if (!include) {
            return EXCLUDE
        }

        return INCLUDE
    }
}
