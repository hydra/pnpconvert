package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.diptrace.DipTraceCSVInput

import static FileTools.*

class CSVProcessor {
    ComponentPlacementWriter writer
    ComponentPlacementTransformer transformer

    List<ComponentPlacement> process(String inputFileName) {
        Reader reader = openFileOrUrl(inputFileName)
        CSVInput csvInput = new DipTraceCSVInput(inputFileName, reader)

        csvInput.parseHeader()

        List<ComponentPlacement> placements = []

        csvInput.parseLines { CSVInputContext context, ComponentPlacement componentPlacement, String[] line ->


            ComponentPlacement transformedComponentPlacement = transformer.process(componentPlacement)

            writer.process(transformedComponentPlacement, line)

            placements << transformedComponentPlacement
        }

        csvInput.close()

        writer.close()

        transformer.close()

        return placements
    }

    CSVProcessor withTransformer(ComponentPlacementTransformer transformer) {
        this.transformer = transformer
        return this
    }

    CSVProcessor withWriter(ComponentPlacementWriter writer) {
        this.writer = writer
        return this
    }
}
