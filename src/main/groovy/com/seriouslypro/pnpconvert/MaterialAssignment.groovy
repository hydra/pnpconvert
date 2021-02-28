package com.seriouslypro.pnpconvert

import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
class MaterialAssignment {
    Component component
    Integer feederId
    Feeder feeder
}