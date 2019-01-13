package com.seriouslypro.pnpconvert

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false, includeSuperFields = true)
@EqualsAndHashCode(callSuper = true)
class TrayFeeder extends Feeder {
    Tray tray
}
