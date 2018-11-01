package com.seriouslypro.pnpconvert

class FeederMapping {
    Integer id
    Feeder feeder
}

class Feeders {

    Map<Integer, Feeder> feederMap = [:]

    void loadReel(int id, int tapeWidth, String componentName, PickSettings pickSettings, String note, FeederProperties feederProperties) {
        loadReel(id, new ReelFeeder(
            tapeWidth: tapeWidth,
            componentName: componentName,
            pickSettings: pickSettings,
            note: note,
            properties: feederProperties
        ))
    }

    void loadReel(int id, ReelFeeder reelFeeder) {
        feederMap[id] = reelFeeder
    }

    FeederMapping findByComponent(Component component) {

        return feederMap.findResult { Integer id, Feeder feeder ->
            feeder.hasComponent(component.name) ? new FeederMapping(id: id, feeder: feeder) : null
        }
    }

    static enum FeederCSVColumn {
        ID,
        ENABLED,
        USE_VISION,
        CHECK_VACUUM,
        COMPONENT_NAME,
        NOTE,
        HEAD,
        XOFFSET,
        YOFFSET,
        PLACE_SPEED,
        PLACE_DELAY,

        TAKE_HEIGHT,
        PACKAGE_ANGLE,

        TAPE_WIDTH,
        TAPE_SPACING,
        TAPE_PULL_SPEED,
    }

    void loadFromCSV(InputStreamReader inputStreamReader) {

        CSVLineParser<FeederMapping> lineParser = new CSVLineParser<FeederMapping>() {
            @Override
            FeederMapping parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {

                PickSettings pickSettings = new PickSettings()

                if (headerMappings[FeederCSVColumn.TAPE_SPACING]) {
                    pickSettings.tapeSpacing = rowValues[headerMappings[FeederCSVColumn.TAPE_SPACING].index] as Integer
                }

                FeederProperties feederProperties = new FeederProperties()

                Integer id = rowValues[headerMappings[FeederCSVColumn.ID].index] as Integer

                loadReel(id, new ReelFeeder(
                    componentName: rowValues[headerMappings[FeederCSVColumn.COMPONENT_NAME].index],
                    note: rowValues[headerMappings[FeederCSVColumn.NOTE].index],
                    tapeWidth: rowValues[headerMappings[FeederCSVColumn.TAPE_WIDTH].index] as Integer,
                    pickSettings: pickSettings,
                    properties: feederProperties
                ))

                return new FeederMapping(id: id, feeder: feederMap[id])
            }
        }
        CSVHeaderParser componentHeaderParser = new CSVHeaderParser() {

            Map<FeederCSVColumn, CSVHeader> headerMappings = [:]

            @Override
            void parse(String[] headerValues) {
                headerValues.eachWithIndex { String headerValue, Integer index ->
                    FeederCSVColumn componentCSVColumn = headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as FeederCSVColumn
                    CSVHeader csvHeader = new CSVHeader(index: index)
                    headerMappings[componentCSVColumn] = csvHeader
                }
            }

            @Override
            Map<Object, CSVHeader> getHeaderMappings() {
                return headerMappings
            }
        }

        CSVInput<FeederMapping> csvInput = new CSVInput<FeederMapping>(inputStreamReader, componentHeaderParser, lineParser)
        csvInput.parseHeader()

        csvInput.parseLines { FeederMapping feederMapping, String[] line ->
            feederMap[feederMapping.id] = feederMapping.feeder
        }

        csvInput.close()
    }
}
