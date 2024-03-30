package com.seriouslypro.pnpconvert

import spock.lang.Specification
import static com.github.stefanbirkner.systemlambda.SystemLambda.*;

class PNPConvertSpec extends Specification {

    def "no args"() {
        given:
            List<String> args = []

        when:
            def exitCode = catchSystemExit {
                PNPConvert.main(args as String[])
            }

        then:
            exitCode == -1
    }

    def "insufficient fiducials"() {
        given:
            String requiredArgs = '-c'
            List<String> args = [requiredArgs]

            args << "-fm"
            args << "RL,0.0,0.0"

        when:
            PNPConvert.main(args as String[])

        then:
            Exception thrown = thrown()
            thrown instanceof IllegalArgumentException
            thrown.message.startsWith('Insufficient fiducial markers')
    }

    def "too many fiducials"() {
        given:
            String requiredArgs = '-c'
            List<String> args = [requiredArgs]

            args << "-fm"
            args << "RL,0.0,0.0"
            args << "FR,0.0,0.0"
            args << "FL,0.0,0.0"
            args << "RR,0.0,0.0"

        when:
            PNPConvert.main(args as String[])

        then:
            Exception thrown = thrown()
            thrown instanceof IllegalArgumentException
            thrown.message.startsWith('Too many fiducial markers')
    }
}
