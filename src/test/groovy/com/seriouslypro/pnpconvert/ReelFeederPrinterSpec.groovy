package com.seriouslypro.pnpconvert

import spock.lang.Specification

class ReelFeederPrinterSpec extends Specification {

    def "can print reel feeder"() {
        given:
            def feederPrinter = new ReelFeederPrinter()
            def feeder = new ReelFeeder()

        expect:
            feederPrinter.canPrint(feeder)
    }

    def "print with missing id"() {
        given:
            def feederPrinter = new ReelFeederPrinter()
            def feeder = new ReelFeeder()

        when:
            def result = feederPrinter.print(feeder)

        then:
            result == [id: "<None>", note: ""]
    }
}
