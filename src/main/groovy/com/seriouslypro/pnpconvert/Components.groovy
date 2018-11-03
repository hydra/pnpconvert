package com.seriouslypro.pnpconvert


interface MatchingStrategy {
    boolean matches(Component candidate, ComponentPlacement componentPlacement)
}

class DiptraceMatchingStrategy implements MatchingStrategy {

    DipTraceComponentNameBuilder diptraceComponentNameBuilder = new DipTraceComponentNameBuilder()

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        String dipTraceComponentName = diptraceComponentNameBuilder.buildDipTraceComponentName(componentPlacement)
        candidate.name == dipTraceComponentName
    }

}

class NameOnlyMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        candidate.name == componentPlacement.name
    }
}

class DiptraceAliasMatchingStrategy implements MatchingStrategy {

    DipTraceComponentNameBuilder diptraceComponentNameBuilder = new DipTraceComponentNameBuilder()

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        String dipTraceComponentName = diptraceComponentNameBuilder.buildDipTraceComponentName(componentPlacement)
        candidate.aliases.contains(dipTraceComponentName)
    }
}

class AliasMatchingStrategy implements MatchingStrategy {

    @Override
    boolean matches(Component candidate, ComponentPlacement componentPlacement) {
        candidate.aliases.contains(componentPlacement.name)
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

    Component findByPlacement(ComponentPlacement componentPlacement) {
        components.find { Component candidate ->
            List<MatchingStrategy> matchedMatchingStrategies = matchingStrategies.findAll { MatchingStrategy strategy ->
                strategy.matches(candidate, componentPlacement)
            }
            matchedMatchingStrategies
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

    void loadFromCSV(InputStreamReader inputStreamReader) {

        CSVLineParser<Component, ComponentCSVColumn> componentLineParser = new CSVLineParserBase<Component, ComponentCSVColumn>() {

            @Override
            Component parse(String[] rowValues) {
                return new Component(
                    name: rowValues[columnIndex(ComponentCSVColumn.NAME)].trim(),
                    width: rowValues[columnIndex(ComponentCSVColumn.WIDTH)] as BigDecimal,
                    length: rowValues[columnIndex(ComponentCSVColumn.LENGTH)] as BigDecimal,
                    height: rowValues[columnIndex(ComponentCSVColumn.HEIGHT)] as BigDecimal,
                    aliases: rowValues[columnIndex(ComponentCSVColumn.ALIASES)].split(",").collect { it.trim() }
                )
            }
        }

        CSVHeaderParser<ComponentCSVColumn> componentHeaderParser = new CSVHeaderParserBase<ComponentCSVColumn>() {
            @Override
            ComponentCSVColumn parseHeader(String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as ComponentCSVColumn
            }
        }

        CSVInput<Component, ComponentCSVColumn> csvInput = new CSVInput<Component, ComponentCSVColumn>(inputStreamReader, componentHeaderParser, componentLineParser)
        csvInput.parseHeader()

        csvInput.parseLines { Component component, String[] line ->
            components.add(component)
        }

        csvInput.close()
    }
}
