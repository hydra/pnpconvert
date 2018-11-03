package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.DefaultMachine
import com.seriouslypro.pnpconvert.machine.Machine

class FeederMapping {
    Integer id
    Feeder feeder
}


class Feeders {

    Machine machine = new DefaultMachine()
    List<Tray> trays = defaultTrays // temporary, until loading tray definitions from CSV is implemented.

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

    void loadTray(int id, TrayFeeder trayFeeder) {
        feederMap[id] = trayFeeder
    }

    void loadTray(int id, Tray tray, String componentName, PickSettings pickSettings, String note, FeederProperties feederProperties) {
        loadTray(id, new TrayFeeder(
            tray: tray,
            componentName: componentName,
            pickSettings: pickSettings,
            note: note,
            properties: feederProperties
        ))
    }

    static enum FeederCSVColumn {
        ID,
        ENABLED,
        USE_VISION,
        CHECK_VACUUM,
        COMPONENT_NAME,
        NOTE,
        HEAD,
        X_OFFSET,
        Y_OFFSET,
        PLACE_SPEED,
        PLACE_DELAY,

        TAKE_HEIGHT,
        PACKAGE_ANGLE,

        // Tape Specific Columns
        TAPE_WIDTH,
        TAPE_SPACING,
        TAPE_PULL_SPEED,

        // Tray Specific Columns
        TRAY_NAME,
    }

    void loadFromCSV(InputStreamReader inputStreamReader) {

        CSVLineParser<FeederMapping> lineParser = new CSVLineParser<FeederMapping>() {

            @Override
            FeederMapping parse(Map<Object, CSVHeader> headerMappings, String[] rowValues) {

                PickSettings pickSettings = new PickSettings()

                Integer id = rowValues[headerMappings[FeederCSVColumn.ID].index] as Integer

                pickSettings.xOffset = rowValues[headerMappings[FeederCSVColumn.X_OFFSET].index] as BigDecimal
                pickSettings.yOffset = rowValues[headerMappings[FeederCSVColumn.Y_OFFSET].index] as BigDecimal
                pickSettings.head = rowValues[headerMappings[FeederCSVColumn.HEAD].index] as Integer
                pickSettings.checkVacuum = rowValues[headerMappings[FeederCSVColumn.CHECK_VACUUM].index].toBoolean()
                pickSettings.useVision = rowValues[headerMappings[FeederCSVColumn.USE_VISION].index].toBoolean()
                pickSettings.packageAngle = rowValues[headerMappings[FeederCSVColumn.PACKAGE_ANGLE].index] as BigDecimal
                pickSettings.placeSpeedPercentage = rowValues[headerMappings[FeederCSVColumn.PLACE_SPEED].index] as BigDecimal
                pickSettings.placeDelay = rowValues[headerMappings[FeederCSVColumn.PLACE_DELAY].index] as Integer
                pickSettings.takeHeight = rowValues[headerMappings[FeederCSVColumn.TAKE_HEIGHT].index] as BigDecimal

                FeederProperties feederProperties = machine.feederProperties(id)

                String trayName

                if (headerMappings[FeederCSVColumn.TRAY_NAME]) {
                    trayName = rowValues[headerMappings[FeederCSVColumn.TRAY_NAME].index].trim()
                }

                if (trayName) {
                    Tray tray = trays.find { it.name == trayName }

                    if (!tray) {
                        throw new IllegalArgumentException("unknown tray. name: '$trayName'")
                    }

                    loadTray(id, new TrayFeeder(
                        enabled: rowValues[headerMappings[FeederCSVColumn.ENABLED].index].toBoolean(),
                        tray: tray,
                        componentName: rowValues[headerMappings[FeederCSVColumn.COMPONENT_NAME].index],
                        note: rowValues[headerMappings[FeederCSVColumn.NOTE].index],
                        pickSettings: pickSettings,
                        properties: feederProperties
                    ))
                } else {


                    if (headerMappings[FeederCSVColumn.TAPE_SPACING]) {
                        pickSettings.tapeSpacing = rowValues[headerMappings[FeederCSVColumn.TAPE_SPACING].index] as Integer
                    }
                    if (headerMappings[FeederCSVColumn.TAPE_PULL_SPEED]) {
                        pickSettings.pullSpeed = rowValues[headerMappings[FeederCSVColumn.TAPE_PULL_SPEED].index] as Integer
                    }

                    loadReel(id, new ReelFeeder(
                        enabled: rowValues[headerMappings[FeederCSVColumn.ENABLED].index].toBoolean(),
                        componentName: rowValues[headerMappings[FeederCSVColumn.COMPONENT_NAME].index],
                        note: rowValues[headerMappings[FeederCSVColumn.NOTE].index],
                        tapeWidth: rowValues[headerMappings[FeederCSVColumn.TAPE_WIDTH].index] as Integer,
                        pickSettings: pickSettings,
                        properties: feederProperties
                    ))
                }
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

    private static List<Tray> defaultTrays = [
        new Tray(
            name: "B-1-4-TL",
            firstComponentX: 205.07G, firstComponentY: 61.05G,
            lastComponentX: 277.1G, lastComponentY: 61.11G,
            columns: 4,
            rows: 1,
            firstComponentIndex: 0
        ),
        new Tray(
            name: "B-6-7-TL",
            firstComponentX: 327.5G, firstComponentY: 58.57G,
            lastComponentX: 351.51G, lastComponentY: 58.57G,
            columns: 2,
            rows: 1,
            firstComponentIndex: 0
        )
    ]
}
