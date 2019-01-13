package com.seriouslypro.pnpconvert

class Trays {
    List<Tray> trays = []

    Tray findByName(String name) {
        trays.find { it.name == name }
    }

    static enum TrayCSVColumn implements CSVColumn<TrayCSVColumn> {
        NAME,
        FIRST_COMPONENT_X,
        FIRST_COMPONENT_Y,
        LAST_COMPONENT_X,
        LAST_COMPONENT_Y,
        COLUMNS,
        ROWS,
        FIRST_COMPONENT_INDEX
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<Tray, TrayCSVColumn> trayLineParser = new CSVLineParserBase<Tray, TrayCSVColumn>() {

            @Override
            Tray parse(CSVInputContext context, String[] rowValues) {
                return new Tray(
                    name: rowValues[columnIndex(context, TrayCSVColumn.NAME)].trim(),
                    firstComponentX: rowValues[columnIndex(context, TrayCSVColumn.FIRST_COMPONENT_X)] as BigDecimal,
                    firstComponentY: rowValues[columnIndex(context, TrayCSVColumn.FIRST_COMPONENT_Y)] as BigDecimal,
                    lastComponentX: rowValues[columnIndex(context, TrayCSVColumn.LAST_COMPONENT_X)] as BigDecimal,
                    lastComponentY: rowValues[columnIndex(context, TrayCSVColumn.LAST_COMPONENT_Y)] as BigDecimal,
                    columns: rowValues[columnIndex(context, TrayCSVColumn.COLUMNS)] as Integer,
                    rows: rowValues[columnIndex(context, TrayCSVColumn.ROWS)] as Integer,
                    firstComponentIndex: rowValues[columnIndex(context, TrayCSVColumn.FIRST_COMPONENT_INDEX)] as Integer,
                )
            }
        }

        CSVHeaderParser<TrayCSVColumn> trayHeaderParser = new CSVHeaderParserBase<TrayCSVColumn>() {
            @Override
            TrayCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                TrayCSVColumn.fromString(TrayCSVColumn, headerValue) as TrayCSVColumn
            }
        }

        CSVInput<Tray, TrayCSVColumn> csvInput = new CSVInput<Tray, TrayCSVColumn>(reference, reader, trayHeaderParser, trayLineParser)
        csvInput.parseHeader()

        csvInput.parseLines { CSVInputContext context, Tray tray, String[] line ->
            trays.add(tray)
        }

        csvInput.close()
    }
}
