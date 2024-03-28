package com.seriouslypro.pnpconvert

import spock.lang.Specification

class ComponentsLoaderSpec extends Specification {

    public static final String TEST_COMPONENTS_RESOURCE = "/components1.csv"
    public static final String TEST_LEGACY_COMPONENTS_RESOURCE = "/components-legacy.csv"

    ComponentsLoader components

    void setup() {
        components = new ComponentsLoader()
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_COMPONENTS_RESOURCE)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            List<Component> expectedComponents = [
                new Component(partCode: "CRG0402F10K", manufacturer: "TE CONNECTIVITY", description: "10K 0402 1%/RES_0402", width: 0.5, length: 1.0, height: 0.5, placementOffsetX: 0.02, placementOffsetY: 0.4)
            ]

        when:
            components.loadFromCSV(TEST_COMPONENTS_RESOURCE, inputStreamReader)

        then:
            components.components == expectedComponents
    }

    def 'components file with missing required heading'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_LEGACY_COMPONENTS_RESOURCE)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        when:
            components.loadFromCSV(TEST_COMPONENTS_RESOURCE, inputStreamReader)

        then:
            components.components == []
    }
}
