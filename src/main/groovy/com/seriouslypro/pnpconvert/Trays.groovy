package com.seriouslypro.pnpconvert

class Trays {
    List<Tray> trays = []

    Tray findByName(String name) {
        trays.find { it.name == name }
    }

    static enum TrayCSVColumn {
        NAME,
        FIRST_COMPONENT_X,
        FIRST_COMPONENT_Y,
        LAST_COMPONENT_X,
        LAST_COMPONENT_Y,
        COLUMNS,
        ROWS,
        FIRST_COMPONENT_INDEX
    }

    void loadFromCSV(Reader reader) {

        CSVLineParser<Tray, TrayCSVColumn> trayLineParser = new CSVLineParserBase<Tray, TrayCSVColumn>() {

            @Override
            Tray parse(String[] rowValues) {
                return new Tray(
                    name: rowValues[columnIndex(TrayCSVColumn.NAME)].trim(),
                    firstComponentX: rowValues[columnIndex(TrayCSVColumn.FIRST_COMPONENT_X)] as BigDecimal,
                    firstComponentY: rowValues[columnIndex(TrayCSVColumn.FIRST_COMPONENT_Y)] as BigDecimal,
                    lastComponentX: rowValues[columnIndex(TrayCSVColumn.LAST_COMPONENT_X)] as BigDecimal,
                    lastComponentY: rowValues[columnIndex(TrayCSVColumn.LAST_COMPONENT_Y)] as BigDecimal,
                    columns: rowValues[columnIndex(TrayCSVColumn.COLUMNS)] as Integer,
                    rows: rowValues[columnIndex(TrayCSVColumn.ROWS)] as Integer,
                    firstComponentIndex: rowValues[columnIndex(TrayCSVColumn.FIRST_COMPONENT_INDEX)] as Integer,
                )
            }
        }

        CSVHeaderParser<TrayCSVColumn> trayHeaderParser = new CSVHeaderParserBase<TrayCSVColumn>() {
            @Override
            TrayCSVColumn parseHeader(String headerValue) {
                headerValue.toUpperCase().replaceAll('[^A-Za-z0-9]', "_") as TrayCSVColumn
            }
        }

        CSVInput<Tray, TrayCSVColumn> csvInput = new CSVInput<Tray, TrayCSVColumn>(reader, trayHeaderParser, trayLineParser)
        csvInput.parseHeader()

        csvInput.parseLines { Tray tray, String[] line ->
            trays.add(tray)
        }

        csvInput.close()
    }
}
