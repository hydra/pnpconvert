package com.seriouslypro.pnpconvert

class DipTraceComponentNameBuilder {
    String buildDipTraceComponentName(ComponentPlacement componentPlacement) {

        String result = [componentPlacement.value, componentPlacement.name].findAll { String value ->
            value && !value.isEmpty()
        }.join('/')

        return result
    }
}
