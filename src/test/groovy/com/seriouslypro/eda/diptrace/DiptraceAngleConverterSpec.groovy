package com.seriouslypro.eda.diptrace


import spock.lang.Specification
import spock.lang.Unroll

class DiptraceAngleConverterSpec extends Specification {

    @Unroll
    def 'eda to design - #edaAngle'(BigDecimal edaAngle, BigDecimal expectedDesignAngle) {
        expect:
            new DiptraceAngleConverter().edaToDesign(edaAngle).equals(expectedDesignAngle)

        where:
            edaAngle | expectedDesignAngle
            0        | 0
            90       | 270
            180      | 180
            270      | 90
            359.99   | 0.01
    }

    @Unroll
    def 'design to eda - #designAngle'(BigDecimal designAngle, BigDecimal expectedEDAAngle) {
        expect:
            new DiptraceAngleConverter().designToEDA(designAngle).equals(expectedEDAAngle)

        where:
            designAngle | expectedEDAAngle
            0           | 0
            90          | 270
            180         | 180
            270         | 90
            359.99      | 0.01
    }
}
