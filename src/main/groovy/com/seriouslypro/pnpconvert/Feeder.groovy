package com.seriouslypro.pnpconvert

class Feeder {
    boolean enabled = true
    String note = ""
    String componentName
    PickSettings pickSettings
    FeederProperties properties

    boolean hasComponent(String componentName) {
        return this.componentName == componentName
    }
}