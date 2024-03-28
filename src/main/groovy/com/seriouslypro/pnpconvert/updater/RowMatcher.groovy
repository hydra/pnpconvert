package com.seriouslypro.pnpconvert.updater

import com.seriouslypro.pnpconvert.Feeders
import com.seriouslypro.pnpconvert.MatchOption

class RowMatcher {

    static boolean match(Set<MatchOption> matchOptions, SheetToDPVHeaderMapping sheetToEntryHeaderMapping, List<String> sheetFeederRowValues, List<String> dpvFeederEntryValues) {

        if (matchOptions.empty) {
            throw new IllegalArgumentException("At least one match option is required")
        }

        if (matchOptions.contains(MatchOption.FLAG_ENABLED)) {
            int sheetFlagsIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.FLAGS)

            boolean enabledFlagMatched = !sheetFeederRowValues[sheetFlagsIndex].contains('!')
            if (!enabledFlagMatched) {
                return false
            }
        }

        if (matchOptions.contains(MatchOption.FEEDER_ID)) {
            int dpvIdIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.ID)
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.ID)

            boolean idMatched = dpvFeederEntryValues[dpvIdIndex] == sheetFeederRowValues[sheetColumnIndex]
            if (!idMatched) {
                return false
            }
        }

        // use of the note field is fragile due to the dynamic content, and constrains of DPV note field, e.g. maximum length.
        // part code and manufacturer may not be present, require description to be present in order to detect
        int dpvNoteIndex = sheetToEntryHeaderMapping.dpvIndex(DPVStationTableColumn.NOTE)
        List<String> noteParts = dpvFeederEntryValues[dpvNoteIndex] ? dpvFeederEntryValues[dpvNoteIndex].split(";") : []

        if (matchOptions.contains(MatchOption.PART_CODE)) {
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.PART_CODE)

            if (noteParts.size() < 2) {
                return false
            }
            String partCode = noteParts[0]

            boolean partCodeMatched = partCode == sheetFeederRowValues[sheetColumnIndex]
            if (!partCodeMatched) {
                return false
            }
        }

        if (matchOptions.contains(MatchOption.MANUFACTURER)) {
            int sheetColumnIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.MANUFACTURER)

            if (noteParts.size() < 2) {
                return false
            }
            String partCode = noteParts[1]

            boolean partCodeMatched = partCode == sheetFeederRowValues[sheetColumnIndex]
            if (!partCodeMatched) {
                return false
            }
        }

        // if there are 1,2,3 or 4 parts the description is available, but it's index will either be 1 or 3 depending
        // on the presence of part code and manufacturer
        // the description may become trimmed in the DPV file due to the field's maximum length, so partial matches
        // may match more rows than intended!

        if (matchOptions.contains(MatchOption.DESCRIPTION)) {
            int sheetComponentNameIndex = sheetToEntryHeaderMapping.sheetIndex(Feeders.FeederCSVColumn.DESCRIPTION)

            int descriptionIndex = 0 // CLOVER assignment needed to prevent 'EmptyExpression.INSTANCE is immutable' error
            switch (noteParts.size()) {
                case 1:
                case 2:
                    descriptionIndex = 0
                    break
                case 3:
                case 4:
                    descriptionIndex = 2
                    break
                default:
                    return false
            }
            String description = noteParts[descriptionIndex]

            boolean descriptionMatched = sheetFeederRowValues[sheetComponentNameIndex].startsWith(description)
            if (!descriptionMatched) {
                return false
            }
        }

        true
    }
}
