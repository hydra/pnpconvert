package com.seriouslypro.pnpconvert

class DiptraceComponentNameBuilder {
    String buildDipTraceComponentName(ComponentPlacement componentPlacement) {
        componentPlacement.value + '/' + componentPlacement.name
    }
}
