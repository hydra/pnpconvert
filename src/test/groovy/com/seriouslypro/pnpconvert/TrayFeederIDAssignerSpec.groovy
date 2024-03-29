package com.seriouslypro.pnpconvert

import spock.lang.Ignore
import spock.lang.Specification

class TrayFeederIDAssignerSpec extends Specification {

    @Ignore
    def 'throw exception if no more tray ids are available when assigning IDs to trays'() {
        expect:
            false
    }
}
