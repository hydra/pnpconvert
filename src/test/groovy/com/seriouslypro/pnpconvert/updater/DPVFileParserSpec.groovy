package com.seriouslypro.pnpconvert.updater

import spock.lang.Specification

import java.nio.charset.StandardCharsets

class DPVFileParserSpec extends Specification {

    def 'fail to parse file'() {
        given:
            String content = "invalid"
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name()))

        when:
            new DPVFileParser().parse(inputStream)

        then:
            UnsupportedDPVContent caught = thrown()
            caught.message.contains("Expected first line to start with 'separated'")
    }

    def 'parse file with table that has trailing delimiter'() {
        given:
            String content = "separated\n" +
                "FILE,feedertest.dpv\n" +
                "PCBFILE,NONE\n" +
                "DATE,2021/03/10\n" +
                "TIME,22:01:13\n" +
                "PANELYPE,0\n" +
                "\n" +
                "Table,No.,ID,offsetX,offsetY,Note\n" +
                "\n" +
                "CalibPoint,0,1,0,0,\n" +
                "CalibPoint,1,2,0,0,"

            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8.name()))

        when:
            new DPVFileParser().parse(inputStream)

        then:
            noExceptionThrown()
    }

}
