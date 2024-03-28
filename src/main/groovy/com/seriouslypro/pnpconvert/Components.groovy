package com.seriouslypro.pnpconvert

import com.seriouslypro.csv.CSVColumn
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVHeaderParserBase
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParser
import com.seriouslypro.csv.CSVLineParserBase

class Components {
    List<Component> components = []

    void add(Component component) {
        components << component
    }

    static enum ComponentCSVColumn implements CSVColumn<ComponentCSVColumn> {
        PART_CODE,
        MANUFACTURER,
        DESCRIPTION,
        WIDTH(["WIDTH/X", "X","WIDTH (X)"]),
        LENGTH(["LENGTH/Y", "Y","LENGTH (Y)"]),
        HEIGHT(["HEIGHT/Z", "Z","HEIGHT (Z)"]),
        PLACEMENT_OFFSET_X(["OFFSET X", "OFFSETX"]),
        PLACEMENT_OFFSET_Y(["OFFSET Y", "OFFSETY"])

        ComponentCSVColumn(List<String> aliases = []) {
            this.aliases = aliases
        }
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<Component, ComponentCSVColumn> componentLineParser = new CSVLineParserBase<Component, ComponentCSVColumn>() {

            @Override
            Component parse(CSVInputContext context, String[] rowValues) {

                BigDecimal placementOffsetX = (hasColumn(ComponentCSVColumn.PLACEMENT_OFFSET_X) && rowValues[columnIndex(context, ComponentCSVColumn.PLACEMENT_OFFSET_X)]) ? rowValues[columnIndex(context, ComponentCSVColumn.PLACEMENT_OFFSET_X)] as BigDecimal : 0
                BigDecimal placementOffsetY = (hasColumn(ComponentCSVColumn.PLACEMENT_OFFSET_Y) && rowValues[columnIndex(context, ComponentCSVColumn.PLACEMENT_OFFSET_Y)]) ? rowValues[columnIndex(context, ComponentCSVColumn.PLACEMENT_OFFSET_Y)] as BigDecimal : 0

                return new Component(
                    partCode: rowValues[columnIndex(context, ComponentCSVColumn.PART_CODE)].trim(),
                    manufacturer: rowValues[columnIndex(context, ComponentCSVColumn.MANUFACTURER)].trim(),
                    description: rowValues[columnIndex(context, ComponentCSVColumn.DESCRIPTION)].trim(),
                    width: rowValues[columnIndex(context, ComponentCSVColumn.WIDTH)] as BigDecimal,
                    length: rowValues[columnIndex(context, ComponentCSVColumn.LENGTH)] as BigDecimal,
                    height: rowValues[columnIndex(context, ComponentCSVColumn.HEIGHT)] as BigDecimal,
                    placementOffsetX: placementOffsetX,
                    placementOffsetY: placementOffsetY,
                )
            }
        }

        CSVHeaderParser<ComponentCSVColumn> componentHeaderParser = new CSVHeaderParserBase<ComponentCSVColumn>() {
            @Override
            ComponentCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                ComponentCSVColumn.fromString(ComponentCSVColumn, headerValue) as ComponentCSVColumn
            }
        }

        CSVInput<Component, ComponentCSVColumn> csvInput = new CSVInput<Component, ComponentCSVColumn>(reference, reader, componentHeaderParser, componentLineParser)
        csvInput.parseHeader()

        csvInput.parseLines { CSVInputContext context, Component component, String[] line ->
            components.add(component)
        }

        csvInput.close()
    }
}
