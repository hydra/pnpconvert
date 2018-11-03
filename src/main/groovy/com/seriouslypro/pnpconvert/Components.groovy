package com.seriouslypro.pnpconvert


interface MatchingStrategy {
    boolean matches(Component candidate, ComponentPlacement componentPlacement)
}

class DiptraceMatchingStrategy implements MatchingStrategy {

    DiptraceComponentNameBuilder diptraceComponentNameBuilder = new DiptraceComponentNameBuilder()

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

    DiptraceComponentNameBuilder diptraceComponentNameBuilder = new DiptraceComponentNameBuilder()

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

        CSVLineParser<Component> componentLineParser = new CSVLineParser<Component>() {
            @Override
            Component parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {
                return new Component(
                    name: rowValues[headerMappings[ComponentCSVColumn.NAME].index].trim(),
                    width: rowValues[headerMappings[ComponentCSVColumn.WIDTH].index] as BigDecimal,
                    length: rowValues[headerMappings[ComponentCSVColumn.LENGTH].index] as BigDecimal,
                    height: rowValues[headerMappings[ComponentCSVColumn.HEIGHT].index] as BigDecimal,
                    aliases: rowValues[headerMappings[ComponentCSVColumn.ALIASES].index].split(",").collect { it.trim() }
                )
            }
        }
        CSVHeaderParser componentHeaderParser = new CSVHeaderParser() {

            Map<ComponentCSVColumn, CSVHeader> headerMappings = [:]

            @Override
            void parse(String[] headerValues) {
                headerValues.eachWithIndex { String headerValue, Integer index ->
                    ComponentCSVColumn componentCSVColumn = headerValue.toUpperCase() as ComponentCSVColumn
                    CSVHeader csvHeader = new CSVHeader(index: index)
                    headerMappings[componentCSVColumn] = csvHeader
                }
            }

            @Override
            Map<Object, CSVHeader> getHeaderMappings() {
                return headerMappings
            }
        }

        CSVInput<Component> csvInput = new CSVInput<Component>(inputStreamReader, componentHeaderParser, componentLineParser)
        csvInput.parseHeader()

        csvInput.parseLines { Component component, String[] line ->
            components.add(component)
        }

        csvInput.close()
    }
}
