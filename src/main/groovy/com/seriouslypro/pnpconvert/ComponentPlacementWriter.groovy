package com.seriouslypro.pnpconvert

interface ComponentPlacementWriter {

    void close()

    void process(ComponentPlacement componentPlacement, String[] line)
}