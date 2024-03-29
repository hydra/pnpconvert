package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@EqualsAndHashCode
class Feeder {
    Optional<Integer> fixedId = Optional.empty()
    boolean enabled = true
    String note = ""
    String manufacturer
    String partCode
    String description
    PickSettings pickSettings
    FeederProperties properties
}