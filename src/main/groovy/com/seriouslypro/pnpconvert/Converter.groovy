package com.seriouslypro.pnpconvert

import com.seriouslypro.csv.CSVProcessor
import com.seriouslypro.eda.part.PartMappingsLoader
import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PartSubstitution
import com.seriouslypro.eda.part.PartSubstitutionsLoader
import com.seriouslypro.pnpconvert.machine.Machine

import static FileTools.*

class Converter {

    String inputFileName
    String traysFileName
    String feedersFileName
    String componentsFileName
    String partMappingsFileName
    String partSubstitutionsFileName
    String outputPrefix

    Machine machine

    Board board
    BoardRotation boardRotation = new BoardRotation()
    BoardMirroring boardMirroring = new BoardMirroring()
    Coordinate offsetXY = new Coordinate()
    BigDecimal offsetZ = 0
    PCBSideComponentPlacementFilter.SideInclusion sideInclusion = PCBSideComponentPlacementFilter.SideInclusion.ALL

    CSVProcessor csvProcessor
    Optional<Integer> optionalJob
    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials
    List<RefdesReplacement> refdesReplacements
    Set<String> placementReferenceDesignatorsToDisable

    boolean showTransforms = false

    private static final boolean append = false

    void convert() {

        if (sideInclusion != PCBSideComponentPlacementFilter.SideInclusion.ALL) {
            String sidePostfix = '-' + sideInclusion.toString().toUpperCase()
            outputPrefix += sidePostfix
        }

        optionalJob.ifPresent {job ->
            String jobPostfix = "-job$job"
            outputPrefix += jobPostfix
        }

        //
        // CSV processing
        //

        String transformFileName = outputPrefix + "-transformed.csv"

        Theme theme = new DarkTheme()
        SVGRenderer svgRenderer = new SVGRenderer(theme.background)

        ComponentPlacementTransformer transformer = new DiptraceComponentPlacementTransformer(svgRenderer, theme, board, boardRotation, boardMirroring, offsetXY, optionalPanel)
        transformer.showTransforms = showTransforms
        ComponentPlacementWriter dipTraceComponentPlacementWriter = new DipTraceComponentPlacementWriter(transformFileName)


        PCBSideComponentPlacementFilter sideInclusionFilter = new PCBSideComponentPlacementFilter(sideInclusion: sideInclusion)
        JobComponentPlacementFilter jobInclusionFilter = new JobComponentPlacementFilter(optionalJob: optionalJob)

        csvProcessor = new CSVProcessor(filters: [sideInclusionFilter, jobInclusionFilter], transformer: transformer, writer: dipTraceComponentPlacementWriter)

        List<ComponentPlacement> placements = csvProcessor.process(inputFileName)

        Coordinate zeroZeroPoint = new Coordinate(x: 0, y: 0)
        svgRenderer.drawOrigin(zeroZeroPoint, theme.origin)

        if (showTransforms) svgRenderer.drawBoard(board, theme.original_board)

        svgRenderer.drawPanel(optionalPanel, theme.panel)
        svgRenderer.drawPancelBoards(optionalPanel, board, theme.panel_board)
        svgRenderer.drawFiducials(optionalFiducials, theme.fiducials)

        String svgFileName = outputPrefix + ".svg"
        svgRenderer.save(svgFileName)

        System.out.println()
        System.out.println("rendered SVG, svgFileName: '$svgFileName'")

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
        // Load Substitutions
        //

        PartSubstitutionsLoader partSubstitutionsLoader = loadPartSubstitutions()

        System.out.println()
        System.out.println("defined part substitutions:")
        partSubstitutionsLoader.partSubstitutions.each { PartSubstitution partSubstitution ->
            System.out.println(partSubstitution.toString())
        }

        //
        // Load Mappings
        //

        PartMappingsLoader partMappingsLoader = loadPartMappings()

        System.out.println()
        System.out.println("defined part mappings:")
        partMappingsLoader.partMappings.each { PartMapping partMapping ->
            System.out.println(partMapping.toString())
        }

        //
        // Load Components
        //

        ComponentsLoader componentsLoader = loadComponents()

        System.out.println()
        System.out.println("defined components:")
        componentsLoader.components.each { Component component ->
            System.out.println(component)
        }

        //
        // Load Trays
        //

        TraysLoader traysLoader = loadTrays()

        System.out.println()
        System.out.println("defined trays:")
        traysLoader.trays.each { Tray tray ->
            System.out.println(tray)
        }

        //
        // Load Feeders
        //

        FeedersLoader feeders = loadFeeders(traysLoader)

        System.out.println()
        System.out.println("defined feeders:")
        feeders.feederList.each { Feeder feeder ->
            System.out.println("feeder: $feeder")
        }

        //
        // Substitute placements
        //
        List<PlacementSubstitution> placementSubstitutions = new ComponentPlacementSubstitutor().process(placements, partSubstitutionsLoader.partSubstitutions)
        System.out.println()
        System.out.println("placement substitutions:")

        placementSubstitutions.each { placementSubstitution ->
            if (placementSubstitution.appliedSubstitution.isPresent() && placementSubstitution.originalPlacement.isPresent()) {
                ComponentPlacement originalPlacement = placementSubstitution.originalPlacement.get()
                def placementSummary = [
                    refdes: originalPlacement.refdes,
                    name: originalPlacement.name,
                    value: originalPlacement.value,
                ]
                System.out.print("${placementSummary} -> ")
                PartSubstitution appliedSubstitution = placementSubstitution.appliedSubstitution.get()
                def updatedPlacementSummary = [
                    'name': placementSubstitution.placement.name,
                    'value': placementSubstitution.placement.value,
                ]
                System.out.print("${updatedPlacementSummary}")
                def patternSummary = [
                    'name pattern': appliedSubstitution.namePattern,
                    'value pattern': appliedSubstitution.valuePattern,
                ]
                System.out.println(" <- ${patternSummary}")
            }
        }

        //
        // Generate a map of placements mapped to components
        //

        List<PlacementMapping> placementMappings = new PlacementMapper().map(placements, componentsLoader.components, partMappingsLoader.partMappings)
        System.out.println()
        System.out.println("placement component mappings:")
        placementMappings.each { PlacementMapping mappedPlacement ->
            def placementSummary = [
                refdes: mappedPlacement.placement.refdes,
                name: mappedPlacement.placement.name,
                value: mappedPlacement.placement.value,
            ]
            System.out.print("${placementSummary} -> ")

            Map<String, String> componentCriteria = [
                'part code'   : mappedPlacement.componentCriteria.partCode,
                'manufacturer': mappedPlacement.componentCriteria.manufacturer,
            ]

            System.out.print("${componentCriteria}")

            mappedPlacement.partMapping.ifPresent { partMapping ->
                def partMappingSummary = [
                    'name pattern': partMapping.namePattern,
                    'value pattern': partMapping.valuePattern,
                ]
                System.out.print(" <- ${partMappingSummary}'")
            }

            System.out.println()
            if (mappedPlacement.errors) {
                mappedPlacement.errors.eachWithIndex { error, i ->
                    String indentation = '' // add unused assignment to prevent 'EmptyExpression.INSTANCE is immutable' error with clover
                    if (i == mappedPlacement.errors.size() - 1) {
                        indentation = '└── '
                    } else {
                        indentation = '├── '
                    }
                    System.out.println(indentation + "error: ${error}")
                }
            }
        }
        System.out.println()

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
            placementMappings: placementMappings,
            feedersLoader: feeders,
            optionalPanel: optionalPanel,
            optionalFiducials: optionalFiducials,
            offsetZ: offsetZ,
        )

        generator.generate(outputStream)

        outputStream.close()
    }

    TraysLoader loadTrays() {
        Reader reader = openFileOrUrl(traysFileName)

        TraysLoader trays = new TraysLoader()
        trays.loadFromCSV(traysFileName, reader)

        trays
    }

    FeedersLoader loadFeeders(TraysLoader trays) {
        Reader reader = openFileOrUrl(feedersFileName)

        FeedersLoader feedersLoader = new FeedersLoader(
            traysLoader: trays
        )

        feedersLoader.loadFromCSV(feedersFileName, reader)
        feedersLoader
    }

    private ComponentsLoader loadComponents() {
        Reader reader = openFileOrUrl(componentsFileName)

        ComponentsLoader componentsLoader = new ComponentsLoader()
        componentsLoader.loadFromCSV(componentsFileName, reader)

        componentsLoader
    }

    private PartMappingsLoader loadPartMappings() {
        PartMappingsLoader partMappingsLoader = new PartMappingsLoader()

        if (partMappingsFileName) {
            Reader reader = openFileOrUrl(partMappingsFileName)
            partMappingsLoader.loadFromCSV(partMappingsFileName, reader)
        }

        partMappingsLoader
    }

    private PartSubstitutionsLoader loadPartSubstitutions() {
        PartSubstitutionsLoader partSubstitutionsLoader = new PartSubstitutionsLoader()

        if (partSubstitutionsFileName) {
            Reader reader = openFileOrUrl(partSubstitutionsFileName)
            partSubstitutionsLoader.loadFromCSV(partSubstitutionsFileName, reader)
        }

        partSubstitutionsLoader
    }
}
