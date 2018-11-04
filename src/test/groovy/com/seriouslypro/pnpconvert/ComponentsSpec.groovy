package com.seriouslypro.pnpconvert

import spock.lang.Ignore
import spock.lang.Specification

class ComponentsSpec extends Specification {

    private static final String TEST_COMPONENT_NAME = "10K 0402 1%"
    private static final String TEST_COMPONENT_ALIAS = "10K0 1% 0402"
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
            List<Component> expectedComponentList = [
                new Component(name: "10K 0402 1%/RES_0402", width: 0.5, length: 1.0, height: 0.5, aliases: ["10K 1% 0402/RES_0402",TEST_COMPONENT_ALIAS])
            ]

        when:
            components.loadFromCSV(TEST_COMPONENTS_RESOURCE, inputStreamReader)

        then:
            components.components == expectedComponentList
    }

    def 'find by placement with no components'() {
        given:
            ComponentPlacement componentPlacement = new ComponentPlacement()

        when:
            Component result = components.findByPlacement(componentPlacement)

        then:
            !result
    }

    def 'find by placement'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME)
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_NAME)

        when:
            Component result = components.findByPlacement(componentPlacement)

        then:
            result
    }

    def 'find by placement - matched against alias'() {
        given:
            Component component = new Component(name: TEST_COMPONENT_NAME,aliases: [TEST_COMPONENT_ALIAS])
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: TEST_COMPONENT_ALIAS)

        when:
            Component result = components.findByPlacement(componentPlacement)

        then:
            result
    }

    @Ignore
    def 'add component that matches existing component'() {
        //e.g. component with same name or alias.
        expect:
            false
    }

    @Ignore
    def 'components file with missing required heading'() {
        expect:
            false
    }
}
