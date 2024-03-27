package com.seriouslypro.pnpconvert

import spock.lang.Ignore
import spock.lang.Specification

class ComponentsSpec extends Specification {

    private static final String TEST_PART_CODE = "CRG0402F10K"
    private static final String TEST_MANUFACTURER = "TE CONNECTIVITY"
    private static final String TEST_COMPONENT_NAME = "10K 0402 1%"
    private static final String TEST_COMPONENT_ALIAS = "10K0 1% 0402"
    private static final String TEST_WHITESPACE_ONLY_ALIAS = "  "
    private static final String TEST_EMPTY_ALIAS = ""
    public static final String TEST_COMPONENTS_RESOURCE = "/components1.csv"

    Components components

    void setup() {
        components = new Components()
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_COMPONENTS_RESOURCE)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            List<Component> expectedComponents = [
                new Component(partCode: TEST_PART_CODE, manufacturer: TEST_MANUFACTURER, name: "10K 0402 1%/RES_0402", width: 0.5, length: 1.0, height: 0.5, placementOffsetX: 0.02, placementOffsetY: 0.4, aliases: ["10K 1% 0402/RES_0402",TEST_COMPONENT_ALIAS])
            ]

        when:
            components.loadFromCSV(TEST_COMPONENTS_RESOURCE, inputStreamReader)

        then:
            components.components == expectedComponents
    }

    def 'ignore empty aliases'() {
        given:
            String content = '"Part Code","Manufacturer","Name","Width","Length","Height","Aliases"\n' +
                '"PC","M","NAME",1,2,3,"' + [TEST_WHITESPACE_ONLY_ALIAS, TEST_EMPTY_ALIAS].join(',')+ '"\n'
            Reader inputStreamReader = new StringReader(content)

        when:
            components.loadFromCSV("TEST", inputStreamReader)

        then:
            components.components.first().aliases == []
    }

    def 'find by placement with no components'() {
        given:
            ComponentPlacement componentPlacement = new ComponentPlacement()

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            !result
    }

    def 'find by placement - matched against name only (no aliases)'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME)
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_NAME)

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            result
            result.component == component
            result.matchingStrategies.size() == 2
            result.matchingStrategies.find { it instanceof DiptraceMatchingStrategy }
            result.matchingStrategies.find { it instanceof NameOnlyMatchingStrategy }
    }

    def 'find by placement - matched against name only (with non-matching aliases)'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME, aliases: ["NON_MATCHING"])
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_NAME)

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            result
            result.component == component
            result.matchingStrategies.size() == 2
            result.matchingStrategies.find { it instanceof DiptraceMatchingStrategy }
            result.matchingStrategies.find { it instanceof NameOnlyMatchingStrategy }
    }

    def 'find by placement - matched against name or alias'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME, aliases: [TEST_COMPONENT_ALIAS])
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_ALIAS)

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            result.component == component
            result.matchingStrategies.size() == 2
            result.matchingStrategies.find { it instanceof DiptraceAliasMatchingStrategy }
            result.matchingStrategies.find { it instanceof AliasMatchingStrategy }
    }

    def 'find by placement - matched against alias'() {
        given:
            Component component = new Component(name: "NAME", aliases: [TEST_COMPONENT_ALIAS])
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_ALIAS)

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            result.component == component
            result.matchingStrategies.size() == 2
            result.matchingStrategies.find { it instanceof DiptraceAliasMatchingStrategy }
            result.matchingStrategies.find { it instanceof AliasMatchingStrategy }
    }

    def 'find by placement - matched against part code and manufacturer'() {
        given:
            Component component = new Component(manufacturer: TEST_MANUFACTURER, partCode: TEST_PART_CODE)
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(manufacturer: TEST_MANUFACTURER, partCode: TEST_PART_CODE)

        when:
            ComponentFindResult result = components.findByPlacement(componentPlacement)

        then:
            result.component == component
            result.matchingStrategies.size() == 1
            result.matchingStrategies.find { it instanceof PartCodeAndManufacturerMatchingStrategy }
    }

    @Ignore
    def 'components file with missing required heading'() {
        expect:
            false
    }

    def 'find by feeder name with no components'() {
        when:
            ComponentFindResult result = components.findByFeederName("feeder name")

        then:
            !result
    }

    def 'find by feeder name'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME)
            components.add(component)

        when:
            ComponentFindResult result = components.findByFeederName(TEST_COMPONENT_NAME)

        then:
            result
            result.component == component
            result.matchingStrategies.size() == 1
            result.matchingStrategies.find { it instanceof NameOnlyMatchingStrategy }
    }

}
