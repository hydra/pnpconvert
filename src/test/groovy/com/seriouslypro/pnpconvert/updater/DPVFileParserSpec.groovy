package com.seriouslypro.pnpconvert.updater

import spock.lang.Specification

class DPVFileParserSpec extends Specification {

    def 'fail to parse file'() {
        given:
            InputStream inputStream = new ByteArrayInputStream("invalid".getBytes())

        when:
            DPVFile file = new DPVFileParser().parse(inputStream)

        then:
            UnsupportedDPVContent caught = thrown()
            caught.message.contains("Expected first line to start with 'separated'")
    }
}
