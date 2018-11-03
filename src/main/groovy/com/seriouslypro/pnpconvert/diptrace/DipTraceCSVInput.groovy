package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVInput
import com.seriouslypro.pnpconvert.ComponentPlacement

class DipTraceCSVInput extends CSVInput<ComponentPlacement, DipTraceCSVHeaders> {

    DipTraceCSVInput(Reader reader) {
        super(reader, new DipTraceHeaderParser(), new DipTraceLineParser())
    }
}
