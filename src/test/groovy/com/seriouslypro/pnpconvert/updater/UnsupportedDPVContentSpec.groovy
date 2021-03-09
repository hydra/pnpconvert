package com.seriouslypro.pnpconvert.updater

import spock.lang.Specification

class UnsupportedDPVContentSpec extends Specification {
    def 'error on first character'() {
        when:
            Exception exception = new UnsupportedDPVContent('message', 'line', 0, 0)

        then:
        exception.message == "Unsupported DPV file content, line: 0, offset 0" + "\n" +
            "line" + "\n" +
            "^  message"
    }

    def 'error at specific character of specific line'() {
        when:
        Exception exception = new UnsupportedDPVContent('message', '1234567890', 1, 5)

        then:
        exception.message == "Unsupported DPV file content, line: 1, offset 5" + "\n" +
            "1234567890"  + "\n" +
            "     ^  message"
    }
}
