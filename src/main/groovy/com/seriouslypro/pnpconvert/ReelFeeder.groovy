package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, includeSuperProperties = true)
@EqualsAndHashCode(callSuper = true)
class ReelFeeder extends Feeder {
    int tapeWidth
}
