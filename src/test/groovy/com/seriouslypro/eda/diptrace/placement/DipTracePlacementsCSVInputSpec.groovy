package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.Coordinate
import com.seriouslypro.pnpconvert.PCBSide
import spock.lang.Specification

class DipTracePlacementsCSVInputSpec extends Specification {

    def 'ensure diptrace to design rotation is applied'() {
        // Diptrace angles values are negative clockwise
        // Internal "Design" angles are positive clockwise

        given:
            String inputFileName = "test"

            String content =
                "RefDes,Pattern,X (mm),Y (mm),Side,Rotate,Value,Name\n" +
                "Q2,SOT23,24,52.25,Top,90,,MMBT4401LT1\n" +
                "Q3,SOT23,41,52.25,Top,270,,MMBT4401LT1\n"

            Reader reader = new StringReader(content)

        and:
            ArrayList<ComponentPlacement> expectedPlacements = [
                new ComponentPlacement(refdes: "Q2", name: "MMBT4401LT1", value: "", pattern:"SOT23", coordinate: new Coordinate(x: 24, y: 52.25), side: PCBSide.TOP, rotation: 270, optionalJob: Optional.empty()),
                new ComponentPlacement(refdes: "Q3", name: "MMBT4401LT1", value: "", pattern:"SOT23", coordinate: new Coordinate(x: 41, y: 52.25), side: PCBSide.TOP, rotation: 90, optionalJob: Optional.empty())
            ]

        and:
            CSVInput csvInput = new DipTracePlacementsCSVInput(inputFileName, reader)
            ArrayList<ComponentPlacement> placements = []

        when:
            csvInput.parseHeader()

            csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->
                placements << componentPlacement
            }

        then:
            placements == expectedPlacements
    }

    def 'one of name or value fields are required'() {
        given:
            String inputFileName = "test"

            String content =
                "RefDes,Pattern,X (mm),Y (mm),Side,Rotate,Value,Name\n" +
                    "Q1,SOT23,1,1,Top,0,,\n" +
                    "Q2,SOT23,2,2,Top,0,,NAME\n" +
                    "Q3,SOT23,3,3,Top,0,VALUE,\n" +
                    "Q4,SOT23,4,4,Top,0,VALUE,NAME\n"

            Reader reader = new StringReader(content)

        and:
            ArrayList<ComponentPlacement> expectedPlacements = [
                new ComponentPlacement(refdes: "Q2", name: "NAME", value: "", pattern:"SOT23", coordinate: new Coordinate(x: 2, y: 2), side: PCBSide.TOP, rotation: 0, optionalJob: Optional.empty()),
                new ComponentPlacement(refdes: "Q3", name: "", value: "VALUE", pattern:"SOT23", coordinate: new Coordinate(x: 3, y: 3), side: PCBSide.TOP, rotation: 0, optionalJob: Optional.empty()),
                new ComponentPlacement(refdes: "Q4", name: "NAME", value: "VALUE", pattern:"SOT23", coordinate: new Coordinate(x: 4, y: 4), side: PCBSide.TOP, rotation: 0, optionalJob: Optional.empty()),
            ]

        and:
            CSVInput csvInput = new DipTracePlacementsCSVInput(inputFileName, reader)
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
