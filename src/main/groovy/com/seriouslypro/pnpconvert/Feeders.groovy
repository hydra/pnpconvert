package com.seriouslypro.pnpconvert

class Feeders {

    public static final String FLAG_IGNORE = "!"
    Trays trays

    ArrayList<Feeder> feederList = []

    List<Exception> csvParseExceptions = []

    Feeder createReelFeeder(int id, int tapeWidth, String componentName, PickSettings pickSettings, String note) {
        new ReelFeeder(
            fixedId: Optional.of(id),
            tapeWidth: tapeWidth,
            componentName: componentName,
            pickSettings: pickSettings,
            note: note,
        )
    }

    Feeder loadReel(ReelFeeder reelFeeder) {
        feederList.add(reelFeeder)

        reelFeeder
    }

    Feeder findByComponent(String name) {
        return feederList.findResult { Feeder feeder ->
            feeder.hasComponent(name) ? feeder : null
        }
    }

    Feeder loadTray(TrayFeeder trayFeeder) {
        feederList.add(trayFeeder)

        trayFeeder
    }

    void loadFeeder(Feeder feeder) {
        feederList.add(feeder)
    }

    Feeder createTrayFeeder(int id, Tray tray, String componentName, PickSettings pickSettings, String note) {
        new TrayFeeder(
            tray: tray,
            componentName: componentName,
            pickSettings: pickSettings,
            note: note,
        )
    }

    static enum FeederCSVColumn {
        ID,
        ENABLED,
        USE_VISION,
        CHECK_VACUUM,
        COMPONENT_NAME,
        NOTE,
        HEAD,
        SEPARATE_MOUNT,
        X_OFFSET,
        Y_OFFSET,
        PLACE_SPEED,
        PLACE_DELAY,

        TAKE_HEIGHT,
        TAKE_DELAY,
        PACKAGE_ANGLE,

        // Tape Specific Columns
        TAPE_WIDTH,
        TAPE_SPACING,
        TAPE_PULL_SPEED,

        // Tray Specific Columns
        TRAY_NAME,

        // Optional Columns
        FLAGS,
    }

    class FeederItem {
        Optional<Feeder> feeder = Optional.empty()
        Set<String> flags = []
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<FeederItem, FeederCSVColumn> lineParser = new CSVLineParserBase<FeederItem, FeederCSVColumn>() {

            @Override
            FeederItem parse(CSVInputContext context, String[] rowValues) {

                PickSettings pickSettings = new PickSettings()

                Optional<Integer> id = Optional.empty()
                if (rowValues[columnIndex(context, FeederCSVColumn.ID)]) {
                    id = Optional.of(rowValues[columnIndex(context, FeederCSVColumn.ID)] as Integer)
                }

                if (rowValues[columnIndex(context, FeederCSVColumn.X_OFFSET)]) {
                    pickSettings.xOffset = rowValues[columnIndex(context, FeederCSVColumn.X_OFFSET)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.Y_OFFSET)]) {
                    pickSettings.yOffset = rowValues[columnIndex(context, FeederCSVColumn.Y_OFFSET)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.HEAD)]) {
                    pickSettings.head = rowValues[columnIndex(context, FeederCSVColumn.HEAD)] as Integer
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.SEPARATE_MOUNT)]) {
                    pickSettings.separateMount = rowValues[columnIndex(context, FeederCSVColumn.SEPARATE_MOUNT)].toBoolean()
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
                    pickSettings.placeDelay = rowValues[columnIndex(context, FeederCSVColumn.PLACE_DELAY)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.TAKE_HEIGHT)]) {
                    pickSettings.takeHeight = rowValues[columnIndex(context, FeederCSVColumn.TAKE_HEIGHT)] as BigDecimal
                }
                if (rowValues[columnIndex(context, FeederCSVColumn.TAKE_DELAY)]) {
                    pickSettings.takeDelay = rowValues[columnIndex(context, FeederCSVColumn.TAKE_DELAY)] as BigDecimal
                }


                String note = ""
                if (hasColumn(FeederCSVColumn.NOTE) && rowValues[columnIndex(context, FeederCSVColumn.NOTE)]) {
                    note = rowValues[columnIndex(context, FeederCSVColumn.NOTE)].trim()
                }

                String trayName

                if (hasColumn(FeederCSVColumn.TRAY_NAME)) {
                    trayName = rowValues[headerMappings[FeederCSVColumn.TRAY_NAME].index].trim()
                }

                String componentName = rowValues[columnIndex(context, FeederCSVColumn.COMPONENT_NAME)].trim()
                boolean enabled = rowValues[columnIndex(context, FeederCSVColumn.ENABLED)].toBoolean()

                Set<String> flags = []
                if (hasColumn(FeederCSVColumn.FLAGS) && rowValues[columnIndex(context, FeederCSVColumn.FLAGS)]) {
                    String flagsString = rowValues[columnIndex(context, FeederCSVColumn.FLAGS)].trim()
                    flags = flagsString.split(",")
                }

                Optional<Feeder> feeder = Optional.empty()

                if (trayName) {
                    Tray tray = trays.findByName(trayName)

                    if (!tray) {
                        throw new IllegalArgumentException("unknown tray. name: '$trayName', reference: $context.reference, line: $context.lineIndex")
                    }

                    feeder = Optional.of(new TrayFeeder(
                        fixedId: id,
                        enabled: enabled,
                        tray: tray,
                        componentName: componentName,
                        note: note,
                        pickSettings: pickSettings
                    ))
                } else {
                    if (hasColumn(FeederCSVColumn.TAPE_SPACING) && rowValues[columnIndex(context, FeederCSVColumn.TAPE_SPACING)]) {
                        pickSettings.tapeSpacing = rowValues[columnIndex(context, FeederCSVColumn.TAPE_SPACING)] as Integer
                    }
                    if (hasColumn(FeederCSVColumn.TAPE_PULL_SPEED) && rowValues[columnIndex(context, FeederCSVColumn.TAPE_PULL_SPEED)]) {
                        pickSettings.pullSpeed = rowValues[columnIndex(context, FeederCSVColumn.TAPE_PULL_SPEED)] as Integer
                    }

                    feeder = Optional.of(new ReelFeeder(
                        fixedId: id,
                        enabled: enabled,
                        componentName: componentName,
                        note: note,
                        tapeWidth: rowValues[columnIndex(context, FeederCSVColumn.TAPE_WIDTH)] as Integer,
                        pickSettings: pickSettings
                    ))
                }
                return new FeederItem(feeder: feeder, flags: flags)
            }
        }

        CSVHeaderParser<FeederCSVColumn> componentHeaderParser = new CSVHeaderParserBase<FeederCSVColumn>() {
            @Override
            FeederCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as FeederCSVColumn
            }
        }

        csvParseExceptions = []

        CSVInput<FeederItem, FeederCSVColumn> csvInput = new CSVInput<FeederItem, FeederCSVColumn>(reference, reader, componentHeaderParser, lineParser)
        csvInput.parseHeader()

        csvInput.parseLines({ CSVInputContext context, FeederItem feederItem, String[] line ->
            if (feederItem.feeder.present) {
                if (!feederItem.flags.contains(FLAG_IGNORE)) {

                    Feeder feeder = feederItem.feeder.get()
                    loadFeeder(feeder)
                }
            }
        }, { CSVInputContext context, String[] line, Exception cause ->
            csvParseExceptions << cause
        })

        csvInput.close()
    }
}
