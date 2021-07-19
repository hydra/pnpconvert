package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.pnpconvert.ComponentPlacement
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

class DipTracePlacementsCSVInput extends CSVInput<ComponentPlacement, DipTracePlacementsCSVHeaders> {

    DipTracePlacementsCSVInput(String reference, Reader reader) {
        super(reference, reader, new DipTracePlacementsHeaderParser(), new DipTracePlacementsLineParser())
    }
}
