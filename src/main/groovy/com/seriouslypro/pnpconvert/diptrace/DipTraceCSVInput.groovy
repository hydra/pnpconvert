package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.pnpconvert.ComponentPlacement

class DipTraceCSVInput extends CSVInput<ComponentPlacement, DipTraceCSVHeaders> {

    DipTraceCSVInput(String reference, Reader reader) {
        super(reference, reader, new DipTraceHeaderParser(), new DipTraceLineParser())
    }
}
