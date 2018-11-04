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

    FeederMapping findByComponent(String name) {

        return feederMap.findResult { Integer id, Feeder feeder ->
            feeder.hasComponent(name) ? new FeederMapping(id: id, feeder: feeder) : null
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

                Integer id = rowValues[columnIndex(context, FeederCSVColumn.ID)] as Integer

                if (rowValues[columnIndex(context, FeederCSVColumn.X_OFFSET)]) {
                    pickSettings.xOffset = rowValues[columnIndex(context, FeederCSVColumn.X_OFFSET)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.Y_OFFSET)]) {
                    pickSettings.yOffset = rowValues[columnIndex(context, FeederCSVColumn.Y_OFFSET)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.HEAD)]) {
                    pickSettings.head = rowValues[columnIndex(context, FeederCSVColumn.HEAD)] as Integer
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.CHECK_VACUUM)]) {
                    pickSettings.checkVacuum = rowValues[columnIndex(context, FeederCSVColumn.CHECK_VACUUM)].toBoolean()
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.USE_VISION)]) {
                    pickSettings.useVision = rowValues[columnIndex(context, FeederCSVColumn.USE_VISION)].toBoolean()
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.PACKAGE_ANGLE)]) {
                    pickSettings.packageAngle = rowValues[columnIndex(context, FeederCSVColumn.PACKAGE_ANGLE)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.PLACE_SPEED)]) {
                    pickSettings.placeSpeedPercentage = rowValues[columnIndex(context, FeederCSVColumn.PLACE_SPEED)] as Integer
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.PLACE_DELAY)]) {
                    pickSettings.placeDelay = rowValues[columnIndex(context, FeederCSVColumn.PLACE_DELAY)] as Integer
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.TAKE_HEIGHT)]) {
                    pickSettings.takeHeight = rowValues[columnIndex(context, FeederCSVColumn.TAKE_HEIGHT)] as BigDecimal
                }

                String note = ""
                if (hasColumn(FeederCSVColumn.NOTE) && rowValues[columnIndex(context, FeederCSVColumn.NOTE)]) {
                    note = rowValues[columnIndex(context, FeederCSVColumn.NOTE)]
                }

                rowValues[columnIndex(context, FeederCSVColumn.NOTE)]
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
                        note: note,
                        pickSettings: pickSettings,
                        properties: feederProperties
                    ))
                } else {


                    if (hasColumn(FeederCSVColumn.TAPE_SPACING) && rowValues[columnIndex(context, FeederCSVColumn.TAPE_SPACING)]) {
                        pickSettings.tapeSpacing = rowValues[columnIndex(context, FeederCSVColumn.TAPE_SPACING)] as Integer
                    }
                    if (hasColumn(FeederCSVColumn.TAPE_PULL_SPEED) && rowValues[columnIndex(context, FeederCSVColumn.TAPE_PULL_SPEED)]) {
                        pickSettings.pullSpeed = rowValues[columnIndex(context, FeederCSVColumn.TAPE_PULL_SPEED)] as Integer
                    }

                    loadReel(id, new ReelFeeder(
                        enabled: rowValues[columnIndex(context, FeederCSVColumn.ENABLED)].toBoolean(),
                        componentName: rowValues[columnIndex(context, FeederCSVColumn.COMPONENT_NAME)],
                        note: note,
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
