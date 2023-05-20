package com.seriouslypro.pnpconvert

import spock.lang.Specification

class TrayFeederPrinterSpec extends Specification {

    def "print"() {
        Feeder feeder = new TrayFeeder()
        feeder.tray = new Tray(name: "Name")
        feeder.note = "Note"

        when:
            def result = new TrayFeederPrinter().print(feeder);

        then:
            result == [
                    tray: "Name",
                    note: "Note"
            ]
    }
}
