package com.seriouslypro.eda.part

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PartSubstitution {
    // EDA (BOM/PnP)
    String namePattern
    String valuePattern

    // Substitution information
    String name
    String value
}
