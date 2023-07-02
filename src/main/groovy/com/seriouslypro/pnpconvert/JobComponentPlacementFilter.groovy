package com.seriouslypro.pnpconvert

class JobComponentPlacementFilter implements ComponentPlacementFilter {

    final boolean INCLUDE = true
    final boolean EXCLUDE = false

    Optional<Integer> optionalJob = Optional.empty()
    @Override
    boolean shouldFilter(ComponentPlacement componentPlacement) {

        boolean include = !optionalJob.isPresent() || (
                (optionalJob.isPresent() && componentPlacement.optionalJob.isPresent()) &&
                optionalJob.get() == componentPlacement.optionalJob.get()
        );

        if (!include) {
            return EXCLUDE
        }

        return INCLUDE
    }
}
