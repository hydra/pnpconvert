package com.seriouslypro.pnpconvert

import com.seriouslypro.csv.CSVColumn
import com.seriouslypro.csv.CSVHeaderParser
import com.seriouslypro.csv.CSVHeaderParserBase
import com.seriouslypro.csv.CSVInput
import com.seriouslypro.csv.CSVInputContext
import com.seriouslypro.csv.CSVLineParser
import com.seriouslypro.csv.CSVLineParserBase


interface MatchingStrategy {
    boolean matches(Component candidate, ComponentPlacement componentPlacement)
    boolean matches(Component candidate, String name)
    boolean isExactMatch()
}

class DiptraceMatchingStrategy implements MatchingStrategy {

    DipTraceComponentNameBuilder diptraceComponentNameBuilder = new DipTraceComponentNameBuilder()

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        String dipTraceComponentName = diptraceComponentNameBuilder.buildDipTraceComponentName(componentPlacement)
        dipTraceComponentName && candidate.name == dipTraceComponentName
    }

    @Override
    boolean matches(Component candidate, String name) {
        return false
    }

    @Override
    boolean isExactMatch() {
        return true
    }
}

class NameOnlyMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        componentPlacement.name && candidate.name == componentPlacement.name
    }

    @Override
    boolean matches(Component candidate, String name) {
        name == candidate.name
    }

    @Override
    boolean isExactMatch() {
        return true
    }
}

class DiptraceAliasMatchingStrategy implements MatchingStrategy {

    DipTraceComponentNameBuilder diptraceComponentNameBuilder = new DipTraceComponentNameBuilder()

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        String dipTraceComponentName = diptraceComponentNameBuilder.buildDipTraceComponentName(componentPlacement)
        dipTraceComponentName && candidate.aliases.contains(dipTraceComponentName)
    }

    @Override
    boolean matches(Component candidate, String name) {
        return false
    }

    @Override
    boolean isExactMatch() {
        return false
    }
}

class AliasMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        componentPlacement && candidate.aliases.contains(componentPlacement.name)
    }

    @Override
    boolean matches(Component candidate, String name) {
        candidate.aliases.contains(name)
    }

    @Override
    boolean isExactMatch() {
        return false
    }
}

class Components {
    List<Component> components = []
    List<MatchingStrategy> matchingStrategies = [
        new DiptraceMatchingStrategy(),
        new DiptraceAliasMatchingStrategy(),
        new AliasMatchingStrategy(),
        new NameOnlyMatchingStrategy()
    ]

    ComponentFindResult findByPlacement(ComponentPlacement componentPlacement) {
        components.findResult { Component candidate ->
            List<MatchingStrategy> matchedMatchingStrategies = matchingStrategies.findAll { MatchingStrategy strategy ->
                strategy.matches(candidate, componentPlacement)
            }

            if (!matchedMatchingStrategies)  {
                return null
            }

            new ComponentFindResult(component: candidate, matchingStrategies: matchedMatchingStrategies)
        }
    }

    void add(Component component) {
        components << component
    }

    ComponentFindResult findByFeederName(String feederName) {
        components.findResult { Component candidate ->
            List<MatchingStrategy> matchedMatchingStrategies = matchingStrategies.findAll { MatchingStrategy strategy ->
                strategy.matches(candidate, feederName)
            }

            if (!matchedMatchingStrategies)  {
                return null
            }

            new ComponentFindResult(component: candidate, matchingStrategies: matchedMatchingStrategies)
        }
    }

    static enum ComponentCSVColumn implements CSVColumn<ComponentCSVColumn> {
        NAME,
        WIDTH(["WIDTH/X", "X","WIDTH (X)"]),
        LENGTH(["LENGTH/Y", "Y","LENGTH (Y)"]),
        HEIGHT(["HEIGHT/Z", "Z","HEIGHT (Z)"]),
        ALIASES,
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
                    name: rowValues[columnIndex(context, ComponentCSVColumn.NAME)].trim(),
                    width: rowValues[columnIndex(context, ComponentCSVColumn.WIDTH)] as BigDecimal,
                    length: rowValues[columnIndex(context, ComponentCSVColumn.LENGTH)] as BigDecimal,
                    height: rowValues[columnIndex(context, ComponentCSVColumn.HEIGHT)] as BigDecimal,
                    aliases: rowValues[columnIndex(context, ComponentCSVColumn.ALIASES)].split(",").findResults { raw ->
                        String value = raw.trim()
                        boolean emptyValue = !value
                        emptyValue ? null : value
                    },
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
