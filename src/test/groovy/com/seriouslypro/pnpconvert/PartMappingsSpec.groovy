package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PartMappings
import spock.lang.Specification

class PartMappingsSpec extends Specification {

    private static final String TEST_PART_CODE = "CRG0402F10K"
    private static final String TEST_MANUFACTURER = "TE CONNECTIVITY"
    private static final String TEST_NAME_PATTERN = "/RES_0402.*/"
    private static final String TEST_VALUE_PATTERN = "/10K 0402( (.*)%)?( (.*)mW)?/"

    public static final String TEST_PART_MAPPINGS_RESOURCE = "/part-mappings1.csv"

    PartMappings partMappings

    void setup() {
        partMappings = new PartMappings()
    }

    def 'load'() {
        given:
            InputStream inputStream = this.getClass().getResourceAsStream(TEST_PART_MAPPINGS_RESOURCE)
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream)

        and:
            List<PartMapping> expectedPartMappings = [
                new PartMapping(partCode: TEST_PART_CODE, manufacturer: TEST_MANUFACTURER, namePattern: TEST_NAME_PATTERN, valuePattern: TEST_VALUE_PATTERN)
            ]

        when:
            partMappings.loadFromCSV(TEST_PART_MAPPINGS_RESOURCE, inputStreamReader)

        then:
            partMappings.partMappings == expectedPartMappings
    }
}
