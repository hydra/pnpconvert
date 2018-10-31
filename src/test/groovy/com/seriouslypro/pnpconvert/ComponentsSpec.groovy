package com.seriouslypro.pnpconvert

import spock.lang.Ignore
import spock.lang.Specification

class ComponentsSpec extends Specification {

    Components components

    void setup() {
        components = new Components()
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream("/components1.csv")
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            List<Component> expectedComponentList = [
                new Component(name: "10K 0402 1%")
            ]

        when:
            components.loadFromCSV(inputStreamReader)

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
            Component component = new Component(name: "10K 0402 1%")
            components.add(component)

            ComponentPlacement componentPlacement = new ComponentPlacement(name: "10K 0402 1%")

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
