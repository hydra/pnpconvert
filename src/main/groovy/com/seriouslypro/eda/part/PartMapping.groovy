package com.seriouslypro.eda.part

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames = true, includePackage = false)
@EqualsAndHashCode
class PartMapping {
    // EDA
    String namePattern
    String valuePattern

    // Ordering information
    String partCode
    String manufacturer
}
