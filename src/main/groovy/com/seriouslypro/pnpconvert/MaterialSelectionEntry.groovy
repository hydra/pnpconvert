package com.seriouslypro.pnpconvert

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MaterialSelectionEntry {
    Component component
    Feeder feeder
}