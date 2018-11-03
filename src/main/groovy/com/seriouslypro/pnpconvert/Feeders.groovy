package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.DefaultMachine
import com.seriouslypro.pnpconvert.machine.Machine

class FeederMapping {
    Integer id
    Feeder feeder
}


class Feeders {

    Machine machine = new DefaultMachine()
    Trays trays

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

        CSVLineParser<FeederMapping, FeederCSVColumn> lineParser = new CSVLineParserBase<FeederMapping, FeederCSVColumn>() {

            @Override
            FeederMapping parse(String[] rowValues) {

                PickSettings pickSettings = new PickSettings()

                Integer id = rowValues[headerMappings[FeederCSVColumn.ID].index] as Integer

                pickSettings.xOffset = rowValues[columnIndex(FeederCSVColumn.X_OFFSET)] as BigDecimal
                pickSettings.yOffset = rowValues[columnIndex(FeederCSVColumn.Y_OFFSET)] as BigDecimal
                pickSettings.head = rowValues[columnIndex(FeederCSVColumn.HEAD)] as Integer
                pickSettings.checkVacuum = rowValues[columnIndex(FeederCSVColumn.CHECK_VACUUM)].toBoolean()
                pickSettings.useVision = rowValues[columnIndex(FeederCSVColumn.USE_VISION)].toBoolean()
                pickSettings.packageAngle = rowValues[columnIndex(FeederCSVColumn.PACKAGE_ANGLE)] as BigDecimal
                pickSettings.placeSpeedPercentage = rowValues[columnIndex(FeederCSVColumn.PLACE_SPEED)] as Integer
                pickSettings.placeDelay = rowValues[columnIndex(FeederCSVColumn.PLACE_DELAY)] as Integer
                pickSettings.takeHeight = rowValues[columnIndex(FeederCSVColumn.TAKE_HEIGHT)] as BigDecimal

                FeederProperties feederProperties = machine.feederProperties(id)

                String trayName

                if (hasColumn(FeederCSVColumn.TRAY_NAME)) {
                    trayName = rowValues[headerMappings[FeederCSVColumn.TRAY_NAME].index].trim()
                }

                if (trayName) {
                    Tray tray = trays.findByName(trayName)

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


                    if (hasColumn(FeederCSVColumn.TAPE_SPACING)) {
                        pickSettings.tapeSpacing = rowValues[headerMappings[FeederCSVColumn.TAPE_SPACING].index] as Integer
                    }
                    if (hasColumn(FeederCSVColumn.TAPE_PULL_SPEED)) {
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

        CSVHeaderParser<FeederCSVColumn> componentHeaderParser = new CSVHeaderParserBase<FeederCSVColumn>() {
            @Override
            FeederCSVColumn parseHeader(String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as FeederCSVColumn
            }
        }

        CSVInput<FeederMapping, FeederCSVColumn> csvInput = new CSVInput<FeederMapping, FeederCSVColumn>(inputStreamReader, componentHeaderParser, lineParser)
        csvInput.parseHeader()

        csvInput.parseLines { FeederMapping feederMapping, String[] line ->
            feederMap[feederMapping.id] = feederMapping.feeder
        }

        csvInput.close()
    }
}
