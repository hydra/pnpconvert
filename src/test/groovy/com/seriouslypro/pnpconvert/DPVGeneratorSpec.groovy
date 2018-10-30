package com.seriouslypro.pnpconvert

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream
import spock.lang.Specification

class DPVGeneratorSpec extends Specification {

    def 'instance'() {
        expect:
            new DPVGenerator()
    }

    def 'generate'() {
        given:
            DPVHeader dpvHeader = new DPVHeader(
                fileName: "TEST-FILE",
                pcbFileName: "TEST-PCB-FILE"
            )

            List<ComponentPlacement> componentPlacements = []
            Components components = new Components()
            Feeders feeders = new Feeders()

            OutputStream outputStream = new ByteOutputStream()

            DPVGenerator generator = new DPVGenerator(
                dpvHeader: dpvHeader,
                placements: componentPlacements,
                components: components,
                feeders: feeders
            )

        when:
            generator.generate(outputStream)

        then:
            String content = outputStream.toString()
            content.startsWith("separated")

        and:
            content.contains("Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed")

        and:
            content.contains("Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay")
    }
}
