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

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<FeederMapping, FeederCSVColumn> lineParser = new CSVLineParserBase<FeederMapping, FeederCSVColumn>() {

            @Override
            FeederMapping parse(CSVInputContext context, String[] rowValues) {

                PickSettings pickSettings = new PickSettings()

                Integer id = rowValues[headerMappings[FeederCSVColumn.ID].index] as Integer

                pickSettings.xOffset = rowValues[columnIndex(context, FeederCSVColumn.X_OFFSET)] as BigDecimal
                pickSettings.yOffset = rowValues[columnIndex(context, FeederCSVColumn.Y_OFFSET)] as BigDecimal
                pickSettings.head = rowValues[columnIndex(context, FeederCSVColumn.HEAD)] as Integer
                pickSettings.checkVacuum = rowValues[columnIndex(context, FeederCSVColumn.CHECK_VACUUM)].toBoolean()
                pickSettings.useVision = rowValues[columnIndex(context, FeederCSVColumn.USE_VISION)].toBoolean()
                pickSettings.packageAngle = rowValues[columnIndex(context, FeederCSVColumn.PACKAGE_ANGLE)] as BigDecimal
                pickSettings.placeSpeedPercentage = rowValues[columnIndex(context, FeederCSVColumn.PLACE_SPEED)] as Integer
                pickSettings.placeDelay = rowValues[columnIndex(context, FeederCSVColumn.PLACE_DELAY)] as Integer
                pickSettings.takeHeight = rowValues[columnIndex(context, FeederCSVColumn.TAKE_HEIGHT)] as BigDecimal

                FeederProperties feederProperties = machine.feederProperties(id)

                String trayName

                if (hasColumn(FeederCSVColumn.TRAY_NAME)) {
                    trayName = rowValues[headerMappings[FeederCSVColumn.TRAY_NAME].index].trim()
                }

                if (trayName) {
                    Tray tray = trays.findByName(trayName)

                    if (!tray) {
                        throw new IllegalArgumentException("unknown tray. name: '$trayName', reference: $context.reference, line: $context.lineIndex")
                    }

                    loadTray(id, new TrayFeeder(
                        enabled: rowValues[columnIndex(context, FeederCSVColumn.ENABLED)].toBoolean(),
                        tray: tray,
                        componentName: rowValues[columnIndex(context, FeederCSVColumn.COMPONENT_NAME)],
                        note: rowValues[columnIndex(context, FeederCSVColumn.NOTE)],
                        pickSettings: pickSettings,
                        properties: feederProperties
                    ))
                } else {


                    if (hasColumn(FeederCSVColumn.TAPE_SPACING)) {
                        pickSettings.tapeSpacing = rowValues[columnIndex(context, FeederCSVColumn.TAPE_SPACING)] as Integer
                    }
                    if (hasColumn(FeederCSVColumn.TAPE_PULL_SPEED)) {
                        pickSettings.pullSpeed = rowValues[columnIndex(context, FeederCSVColumn.TAPE_PULL_SPEED)] as Integer
                    }

                    loadReel(id, new ReelFeeder(
                        enabled: rowValues[columnIndex(context, FeederCSVColumn.ENABLED)].toBoolean(),
                        componentName: rowValues[columnIndex(context, FeederCSVColumn.COMPONENT_NAME)],
                        note: rowValues[columnIndex(context, FeederCSVColumn.NOTE)],
                        tapeWidth: rowValues[columnIndex(context, FeederCSVColumn.TAPE_WIDTH)] as Integer,
                        pickSettings: pickSettings,
                        properties: feederProperties
                    ))
                }
                return new FeederMapping(id: id, feeder: feederMap[id])
            }
        }

        CSVHeaderParser<FeederCSVColumn> componentHeaderParser = new CSVHeaderParserBase<FeederCSVColumn>() {
            @Override
            FeederCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as FeederCSVColumn
            }
        }

        CSVInput<FeederMapping, FeederCSVColumn> csvInput = new CSVInput<FeederMapping, FeederCSVColumn>(reference, reader, componentHeaderParser, lineParser)
        csvInput.parseHeader()

        csvInput.parseLines { CSVInputContext context, FeederMapping feederMapping, String[] line ->
            feederMap[feederMapping.id] = feederMapping.feeder
        }

        csvInput.close()
    }
}
