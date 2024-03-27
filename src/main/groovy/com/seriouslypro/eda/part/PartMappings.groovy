package com.seriouslypro.eda.part

import com.seriouslypro.csv.*
import com.seriouslypro.pnpconvert.FileTools

class PartMappings {

    List<PartMapping> partMappings = []

    static enum EDAPartMappingCSVColumn implements CSVColumn<EDAPartMappingCSVColumn> {
        NAME_PATTERN,
        VALUE_PATTERN,
        PART_CODE,
        MANUFACTURER

        EDAPartMappingCSVColumn(List<String> aliases = []) {
            this.aliases = aliases
        }
    }

    void loadFromCSV(String fileName) {
        Reader reader = FileTools.openFileOrUrl(fileName)
        loadFromCSV(fileName, reader)
    }

    void loadFromCSV(String reference, Reader reader) {

        CSVLineParser<PartMapping, EDAPartMappingCSVColumn> edaPartMappingParser = new CSVLineParserBase<PartMapping, EDAPartMappingCSVColumn>() {

            @Override
            PartMapping parse(CSVInputContext context, String[] rowValues) {

                return new PartMapping(
                    namePattern: rowValues[columnIndex(context, EDAPartMappingCSVColumn.NAME_PATTERN)].trim(),
                    valuePattern: rowValues[columnIndex(context, EDAPartMappingCSVColumn.VALUE_PATTERN)].trim(),
                    partCode: rowValues[columnIndex(context, EDAPartMappingCSVColumn.PART_CODE)].trim(),
                    manufacturer: rowValues[columnIndex(context, EDAPartMappingCSVColumn.MANUFACTURER)].trim(),
                )
            }
        }

        CSVHeaderParser<EDAPartMappingCSVColumn> edaPartMappingHeaderParser = new CSVHeaderParserBase<EDAPartMappingCSVColumn>() {
            @Override
            EDAPartMappingCSVColumn parseHeader(CSVInputContext context, String headerValue) {
                EDAPartMappingCSVColumn.fromString(EDAPartMappingCSVColumn, headerValue) as EDAPartMappingCSVColumn
            }
        }

        CSVInput<PartMapping, EDAPartMappingCSVColumn> csvInput = new CSVInput<PartMapping, EDAPartMappingCSVColumn>(reference, reader, edaPartMappingHeaderParser, edaPartMappingParser)
        csvInput.parseHeader()

        csvInput.parseLines { CSVInputContext context, PartMapping partMapping, String[] line ->
            partMappings.add(partMapping)
        }

        csvInput.close()
    }
}
