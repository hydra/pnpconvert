package com.seriouslypro.pnpconvert


interface MatchingStrategy {
    boolean matches(Component candidate, ComponentPlacement componentPlacement)
    boolean isExactMatch()
}

class DiptraceMatchingStrategy implements MatchingStrategy {

    DipTraceComponentNameBuilder diptraceComponentNameBuilder = new DipTraceComponentNameBuilder()

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        String dipTraceComponentName = diptraceComponentNameBuilder.buildDipTraceComponentName(componentPlacement)
        candidate.name == dipTraceComponentName
    }

    @Override
    boolean isExactMatch() {
        return true
    }
}

class NameOnlyMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        candidate.name == componentPlacement.name
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
        candidate.aliases.contains(dipTraceComponentName)
    }

    @Override
    boolean isExactMatch() {
        return false
    }
}

class AliasMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        candidate.aliases.contains(componentPlacement.name)
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

    static enum ComponentCSVColumn {
        NAME,
        WIDTH,
        LENGTH,
        HEIGHT,
        ALIASES
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<Component, ComponentCSVColumn> componentLineParser = new CSVLineParserBase<Component, ComponentCSVColumn>() {

            @Override
            Component parse(CSVInputContext context, String[] rowValues) {
                return new Component(
                    name: rowValues[columnIndex(context, ComponentCSVColumn.NAME)].trim(),
                    width: rowValues[columnIndex(context, ComponentCSVColumn.WIDTH)] as BigDecimal,
                    length: rowValues[columnIndex(context, ComponentCSVColumn.LENGTH)] as BigDecimal,
                    height: rowValues[columnIndex(context, ComponentCSVColumn.HEIGHT)] as BigDecimal,
                    aliases: rowValues[columnIndex(context, ComponentCSVColumn.ALIASES)].split(",").collect { it.trim() }
                )
            }
        }

        CSVHeaderParser<ComponentCSVColumn> componentHeaderParser = new CSVHeaderParserBase<ComponentCSVColumn>() {
            @Override
            ComponentCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as ComponentCSVColumn
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
