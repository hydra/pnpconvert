package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Feeder {
    Optional<Integer> fixedId = Optional.empty()
    boolean enabled = true
    String note = ""
    String componentName
    PickSettings pickSettings
    FeederProperties properties

    boolean hasComponent(String componentName) {
        return this.componentName == componentName
    }
}