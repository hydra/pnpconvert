package com.seriouslypro.csv

import com.seriouslypro.eda.diptrace.placement.DipTracePlacementsCSVInput
import com.seriouslypro.pnpconvert.ComponentPlacement
import com.seriouslypro.pnpconvert.ComponentPlacementFilter
import com.seriouslypro.pnpconvert.ComponentPlacementTransformer
import com.seriouslypro.pnpconvert.ComponentPlacementWriter
import com.seriouslypro.pnpconvert.FileTools

class CSVProcessor {

    private final class CSVItem {
        ComponentPlacement componentPlacement
        String[] line
    }

    ComponentPlacementFilter filter
    ComponentPlacementWriter writer
    ComponentPlacementTransformer transformer

    List<ComponentPlacement> process(String inputFileName) {
        Reader reader = FileTools.openFileOrUrl(inputFileName)
        CSVInput csvInput = new DipTracePlacementsCSVInput(inputFileName, reader)

        csvInput.parseHeader()

        List<CSVItem> csvItems = []

        csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->
            CSVItem csvItem = new CSVItem(componentPlacement: componentPlacement, line: line)
            csvItems << csvItem
        }

        List<ComponentPlacement> placements = csvItems.stream()
            .filter { csvItem ->
                filter.shouldFilter(csvItem.componentPlacement)
            }
            .map { csvItem ->
                new CSVItem(componentPlacement: transformer.process(csvItem.componentPlacement), line: csvItem.line)
            }
            .peek { csvItem ->
                writer.process(csvItem.componentPlacement, csvItem.line)
            }
            .collect { csvItem ->
                csvItem.componentPlacement
            }

        csvInput.close()

        writer.close()

        return placements
    }
}
