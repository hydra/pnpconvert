package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine

import static FileTools.*

class Converter {

    String inputFileName
    String traysFileName
    String feedersFileName
    String componentsFileName
    String outputPrefix

    Machine machine

    BoardRotation boardRotation = new BoardRotation()
    Coordinate offsetXY = new Coordinate()
    BigDecimal offsetZ = 0
    PCBSideComponentPlacementFilter.SideInclusion sideInclusion = PCBSideComponentPlacementFilter.SideInclusion.ALL

    CSVProcessor csvProcessor
    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials
    List<RefdesReplacement> refdesReplacements
    Set<String> placementReferenceDesignatorsToDisable

    private static final boolean append = false

    void convert() {

        if (sideInclusion != PCBSideComponentPlacementFilter.SideInclusion.ALL) {
            String sidePostfix = '-' + sideInclusion.toString().toUpperCase()
            outputPrefix += sidePostfix
        }

        //
        // CSV processing
        //

        String transformFileName = outputPrefix + "-transformed.csv"

        ComponentPlacementTransformer transformer = new DiptraceComponentPlacementTransformer(outputPrefix, boardRotation, offsetXY)
        ComponentPlacementWriter dipTraceComponentPlacementWriter = new DipTraceComponentPlacementWriter(transformFileName)


        PCBSideComponentPlacementFilter filter = new PCBSideComponentPlacementFilter(sideInclusion: sideInclusion)

        csvProcessor = new CSVProcessor(filter: filter, transformer: transformer, writer: dipTraceComponentPlacementWriter)

        List<ComponentPlacement> placements = csvProcessor.process(inputFileName)

        //
        // Disable components by refdes
        //
        placementReferenceDesignatorsToDisable.each { String upperCaseRefdes ->
            ComponentPlacement matchedPlacement = placements.find { placement ->
                upperCaseRefdes == placement.refdes.toUpperCase()
            }

            if (matchedPlacement) {
                matchedPlacement.enabled = false
            }
        }

        //
        // Replace components by refdes
        //
        if (refdesReplacements) {
            System.out.println()
            System.out.println("replacing components:")
        }
        List<RefdesReplacement> unmatchedRefdesReplacements = []
        refdesReplacements.each { RefdesReplacement refdesReplacement ->
            String upperCaseRefdes = refdesReplacement.refdes.toUpperCase()
            ComponentPlacement matchedPlacement = placements.find { placement ->
                upperCaseRefdes == placement.refdes.toUpperCase()
            }

            if (matchedPlacement) {
                System.out.println("replacing ${matchedPlacement} with ${refdesReplacement}")
                matchedPlacement.name = refdesReplacement.name
                matchedPlacement.value = refdesReplacement.value
            } else {
                unmatchedRefdesReplacements << refdesReplacement
            }
        }

        if (unmatchedRefdesReplacements) {
            System.out.println("Unmatched refdes replacements")
            unmatchedRefdesReplacements.each { refdesReplacement ->
                System.out.println(refdesReplacement)
            }
            System.exit(-1)
        }

        //
        // Load Components
        //

        Components components = loadComponents()

        System.out.println()
        System.out.println("defined components:")
        components.components.each { Component component ->
            System.out.println(component)
        }

        System.out.println()


        //
        // Load Trays
        //

        Trays trays = loadTrays()

        System.out.println()
        System.out.println("defined trays:")
        trays.trays.each { Tray tray ->
            System.out.println(tray)
        }

        //
        // Load Feeders
        //

        Feeders feeders = loadFeeders(trays)

        System.out.println()
        System.out.println("defined feeders:")
        feeders.feederList.each { Feeder feeder ->
            System.out.println("feeder: $feeder")
        }

        //
        // Generate DPV
        //

        String outputDPVFileName = outputPrefix + ".dpv"

        OutputStream outputStream = new FileOutputStream(outputDPVFileName, append)

        DPVHeader dpvHeader = new DPVHeader(
                fileName: outputDPVFileName,
                pcbFileName: inputFileName
        )

        DPVGenerator generator = new DPVGenerator(
                machine: machine,
                dpvHeader: dpvHeader,
                placements: placements,
                components: components,
                feeders: feeders,
                optionalPanel: optionalPanel,
                optionalFiducials: optionalFiducials,
                offsetZ: offsetZ,
        )

        generator.generate(outputStream)

        outputStream.close()
    }

    Trays loadTrays() {
        Reader reader = openFileOrUrl(traysFileName)

        Trays trays = new Trays()
        trays.loadFromCSV(traysFileName, reader)

        trays
    }

    Feeders loadFeeders(Trays trays) {
        Reader reader = openFileOrUrl(feedersFileName)

        Feeders feeders = new Feeders(
            trays: trays
        )

        feeders.loadFromCSV(feedersFileName, reader)
        feeders
    }

    private Components loadComponents() {
        Reader reader = openFileOrUrl(componentsFileName)

        Components components = new Components()
        components.loadFromCSV(componentsFileName, reader)

        components
    }
}
