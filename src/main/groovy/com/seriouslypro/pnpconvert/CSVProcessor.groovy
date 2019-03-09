package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVInput

import static FileTools.*

class CSVItem {
    ComponentPlacement componentPlacement
    String[] line
}

class CSVProcessor {
    ComponentPlacementWriter writer
    ComponentPlacementTransformer transformer

    List<ComponentPlacement> process(String inputFileName) {
        Reader reader = openFileOrUrl(inputFileName)
        CSVInput csvInput = new DipTraceCSVInput(inputFileName, reader)

        csvInput.parseHeader()

        List<CSVItem> csvItems = []

        csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->
            CSVItem csvItem = new CSVItem(componentPlacement: componentPlacement, line: line)
            csvItems << csvItem
        }

        List<ComponentPlacement> placements = csvItems.stream()
            .map { csvItem ->
                new CSVItem(componentPlacement: transformer.process(csvItem.componentPlacement), line: csvItem.line)
            }
            .peek { csvItem ->
                writer.process(csvItem.componentPlacement)
            }
            .collect { csvItem ->
                csvItem.componentPlacement
            }

        csvInput.close()

        writer.close()

        transformer.close()

        return placements
    }
}
