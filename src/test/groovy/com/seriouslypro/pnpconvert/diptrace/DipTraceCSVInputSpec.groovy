package com.seriouslypro.pnpconvert.diptrace

import com.seriouslypro.pnpconvert.CSVInput
import com.seriouslypro.pnpconvert.CSVInputContext
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide
import spock.lang.Specification

class DipTraceCSVInputSpec extends Specification {

    def 'ensure diptrace to design rotation is applied'() {
        // Diptrace angles values are negative clockwise
        // Internal "Design" angles are positive clockwise

        given:
            String inputFileName = "test"

            String content =
                "RefDes,Pattern,X (mm),Y (mm),Side,Rotate,Value,Name\n" +
                "Q2,SOT23,24,52.25,Top,0,,MMBT4401LT1\n" +
                "Q3,SOT23,41,52.25,Top,180,,MMBT4401LT1\n"

            Reader reader = new StringReader(content)

        and:
            ArrayList<ComponentPlacement> expectedPlacements = [
                new ComponentPlacement(refdes: "Q2", name: "MMBT4401LT1", value: "", pattern:"SOT23", coordinate: new Coordinate(x: 24, y: 52.25), side: PCBSide.TOP, rotation: 0),
                new ComponentPlacement(refdes: "Q3", name: "MMBT4401LT1", value: "", pattern:"SOT23", coordinate: new Coordinate(x: 41, y: 52.25), side: PCBSide.TOP, rotation: 180)
            ]

        and:
            CSVInput csvInput = new DipTraceCSVInput(inputFileName, reader)
            ArrayList<ComponentPlacement> placements = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->
                placements << componentPlacement
            }

        then:
            placements == expectedPlacements
    }
}
