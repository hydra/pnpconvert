package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.CHMT48VB
import com.seriouslypro.pnpconvert.machine.Machine
import groovy.cli.picocli.CliBuilder
import groovy.cli.picocli.OptionAccessor

import java.text.DecimalFormat

class PNPConvert {

    public static void main(String [] args) {
        processArgs(args)
    }

    static def processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'pnpconvert')
        builder.v('version')
        builder.i(args:1, argName: 'input', 'input csv file/url')
        builder.o(args:1, argName: 'output', 'output prefix')
        builder.t(args:1, argName: 'trays', 'trays csv file/url')
        builder.f(args:1, argName: 'feeders', 'feeders csv file/url')
        builder.co(args:1, argName: 'components', 'components csv file/url')
        builder.ps(args:1, argName: 'partSubstitutions', 'part substitutions csv file/url')
        builder.pm(args:1, argName: 'partMappings', 'part mappings csv file/url')
        builder.r(args:1, argName: 'rotation', 'rotation degrees (positive is clockwise)')
        builder.s(args:1, argName: 'side', 'pcb side (top|bottom|all), default is all')
        builder.m(args:1, argName: 'mirroring', 'mirroring mode (horizontal/vertical/both/none), default is none')

        builder.ox(args:1, argName: 'offsetX', 'X offset, applied after all other transformations')
        builder.oy(args:1, argName: 'offsetY', 'Y offset, applied after all other transformations')
        builder.oz(args:1, argName: 'offset', 'Z offset, applied to all component heights - increase for thicker PCBs')

        builder.bw(args:1, argName: 'boardWidth','Board width (not panel width)')
        builder.bh(args:1, argName: 'boardHeight','Board height (not panel height)')
        builder.bd(args:1, argName: 'boardDepth','Board thickness')
        builder.box(args:1, argName: 'boardOriginX', 'EDA origin X (usually 0.0)')
        builder.boy(args:1, argName: 'boardOriginY', 'EDA origin Y (usually 0.0)')
        builder.beox(args:1, argName: 'boardExportOffsetX', 'EDA export X offset (e.g. 10.0)')
        builder.beoy(args:1, argName: 'boardExportOffsetY', 'EDA export Y offset (e.g. 10.0)')
        builder.bblox(args:1, argName: 'boardBottomLeftOffsetX', 'origin X - left extent coord (e.g. 0.0 - 5.0 = -5.0)')
        builder.bbloy(args:1, argName: 'boardBottomLeftOffsetY', 'origin Y - bottom extent coord (e.g. 0.0 - 5.0 = -5.0)')

        builder.prwt(args:1, argName: 'panelRailBottomWidth','Top/Rear rail width')
        builder.prwb(args:1, argName: 'panelRailBottomWidth','Bottom/Front rail width')
        builder.prwl(args:1, argName: 'panelRailBottomWidth','Left rail width')
        builder.prwr(args:1, argName: 'panelRailBottomWidth','Right rail width')
        builder.poy(args:1, argName: 'panelNumberY','Number of PCBs on the Y axis')
        builder.pnx(args:1, argName: 'panelNumberX','Number of PCBs on the X axis')
        builder.pny(args:1, argName: 'panelNumberY','Number of PCBs on the Y axis')
        builder.pix(args:1, argName: 'panelIntervalX','Interval spacing on the X axis')
        builder.piy(args:1, argName: 'panelIntervalY','Interval spacing on the Y axis')

        builder.vcf(args:1, argName: 'visionCalibrationFactor','Visual calibration factor')

        builder.j(args:1, argName: 'job', 'job number')

        builder.cfg(args:1, argName: 'config', 'configuration file (in "key=value" format)')

        builder.dr(args:1, argName: 'disableRefdes', 'Disable components by refdes (comma separated list)')
        builder.rr(args:'+', argName: 'replaceRefdes', 'Replace components by refdes ("refdes,value,name"[ ...])')

        builder.fm(args:'+', argName: 'fiducialMarkers','Fiducial marker list (note,x,y[ ...])')
        builder.fp(argName: 'addPlacementsForFiducialsEnabled', 'Add dummy placements for each fiducial')

        builder.st('Show transforms in SVG')

        builder.ft('Generate DPV containing all feeders')
        builder.c('convert')

        OptionAccessor options = builder.parse(args)

        if (!options || options.getParseResult().originalArgs().size() == 0) {
            about()
            builder.usage()
            System.exit(-1)
        }

        if (options.v) {
            about();
            InputStream stream = this.getClass().getResourceAsStream('/version.properties')

            Properties versionProperties = new Properties()
            versionProperties.load(stream as InputStream)
            String version = 'v' + versionProperties.get('version')

            System.out.println(version)
            System.exit(0);
        }

        Properties config = new Properties()
        if (options.cfg) {
            String configFileName = options.cfg
            InputStream inputStream = new FileInputStream(configFileName)
            config.load(inputStream)
        }

        String inputFileName = config.getOrDefault("input", "place.csv")
        String outputPrefix = config.getOrDefault("output", "place")
        String traysFileName = config.getOrDefault("trays","trays.csv")
        String feedersFileName = config.getOrDefault("feeders","feeders.csv")
        String componentsFileName = config.getOrDefault("components","components.csv")
        String partMappingsFileName = config.getOrDefault("partMappings","part-mappings.csv")
        String partSubstitutionsFileName = config.getOrDefault("partSubstitutions","part-substitutions.csv")
        BigDecimal visualCalibrationFactor = config.getOrDefault("visualCalibrationFactor","0.042383") as BigDecimal

        boolean addPlacementsForFiducialsEnabled = false

        Board board = new Board()
        BoardRotation boardRotation = new BoardRotation()
        BoardMirroring boardMirroring = new BoardMirroring()
        Coordinate offsetXY = new Coordinate()
        BigDecimal offsetZ = 0
        Optional<Integer> optionalJob = Optional.empty()
        PCBSideComponentPlacementFilter.SideInclusion sideInclusion = PCBSideComponentPlacementFilter.SideInclusion.ALL
        Optional<Panel> optionalPanel = Optional.empty()
        Optional<List<Fiducial>> optionalFiducials = Optional.empty()
        List<RefdesReplacement> refdesReplacements = []
        Machine machine = new CHMT48VB()

        Set<String> placementReferenceDesignatorsToDisable = []

        if (options.i) {
            inputFileName = options.i
        }

        if (options.o) {
            outputPrefix = options.o
        }

        if (options.t) {
            traysFileName = options.t
        }

        if (options.f) {
            feedersFileName = options.f
        }

        if (options.pm) {
            partMappingsFileName = options.pm
        }

        if (options.ps) {
            partSubstitutionsFileName = options.ps
        }

        if (options.j) {
            optionalJob = Optional.of(options.j as Integer)
        }

        if (options.co) {
            componentsFileName = options.co
        }

        if (options.r) {
            boardRotation.degrees = options.r as BigDecimal
        }

        if (options.bw ) {
            board.height = (options.bh as BigDecimal)
        }

        if (options.bh) {
            board.width = (options.bw as BigDecimal)
        }

        if (options.bd) {
            board.depth = (options.bd as BigDecimal)
        }

        if (options.box ) {
            board.origin.x = (options.box as BigDecimal)
        }

        if (options.boy) {
            board.origin.y = (options.boy as BigDecimal)
        }

        if (options.beox ) {
            board.exportOffset.x = (options.beox as BigDecimal)
        }

        if (options.beoy) {
            board.exportOffset.y = (options.beoy as BigDecimal)
        }

        if (options.beox ) {
            board.exportOffset.x = (options.beox as BigDecimal)
        }

        if (options.beoy) {
            board.exportOffset.y = (options.beoy as BigDecimal)
        }

        if (options.bblox ) {
            board.bottomLeftOffset.x = (options.bblox as BigDecimal)
        }

        if (options.bbloy) {
            board.bottomLeftOffset.y = (options.bbloy as BigDecimal)
        }

        if (options.m) {
            boardMirroring.mode = parseMirroring(options.m)
        }

        if (options.ox ) {
            offsetXY.x = (options.ox as BigDecimal)
        }

        if (options.oy) {
            offsetXY.y = (options.oy as BigDecimal)
        }

        if (options.oz ) {
            offsetZ = (options.oz as BigDecimal)
        }

        if (options.dr) {
            String allDRValues = options.parseResult.matchedOption("dr").typedValues().flatten().join(",")
            placementReferenceDesignatorsToDisable = allDRValues.split(',').collect { it.trim().toUpperCase() }.unique()
        }

        boolean haveAnyPanelOption = (options.pix || options.piy || options.pnx || options.pny ||
            options.prwt || options.prwb || options.prwl || options.prwr)
        if (haveAnyPanelOption) {
            boolean haveRequiredPanelOptions = options.pix && options.piy && options.pnx && options.pny &&
                options.prwt && options.prwb && options.prwl && options.prwr
            if (!haveRequiredPanelOptions) {
                builder.usage()
                System.exit(-1)
            }

            Panel panel = new Panel(
                intervalX: options.pix as BigDecimal,
                intervalY: options.piy as BigDecimal,
                numberX: options.pnx as int,
                numberY: options.pny as int,
                railWidthT: options.prwt as BigDecimal,
                railWidthB: options.prwb as BigDecimal,
                railWidthL: options.prwl as BigDecimal,
                railWidthR: options.prwr as BigDecimal,
            )

            panel.applyBoard(board)

            optionalPanel = Optional.of(panel)
        }

        if (options.fm) {
            String[] fiducialMarkerValues = options.parseResult.matchedOption("fm").typedValues().flatten()
            optionalFiducials = parseFiducials(fiducialMarkerValues)
        }

        if (options.fp) {
            addPlacementsForFiducialsEnabled = true
        }

        if (options.rr) {
            String[] refdesReplacementValues = options.parseResult.matchedOption("rr").typedValues().flatten()
            refdesReplacements = parseRefdesReplacements(refdesReplacementValues)
        }


        if (options.s) {
            sideInclusion = parseSideInclusion(options.s)
        }

        if (options.vcf) {
            visualCalibrationFactor = options.s as BigDecimal
        }

        boolean showTransforms = (options.st)

        if (options.c) {
            Converter converter = new Converter(
                machine: machine,
                inputFileName: inputFileName,
                traysFileName: traysFileName,
                feedersFileName: feedersFileName,
                componentsFileName: componentsFileName,
                partMappingsFileName: partMappingsFileName,
                partSubstitutionsFileName: partSubstitutionsFileName,
                outputPrefix: outputPrefix,
                board: board,
                boardRotation: boardRotation,
                boardMirroring: boardMirroring,
                offsetXY: offsetXY,
                offsetZ: offsetZ,
                sideInclusion: sideInclusion,
                optionalPanel: optionalPanel,
                optionalFiducials: optionalFiducials,
                refdesReplacements: refdesReplacements,
                placementReferenceDesignatorsToDisable: placementReferenceDesignatorsToDisable,
                optionalJob: optionalJob,
                showTransforms: showTransforms,
                addPlacementsForFiducialsEnabled: addPlacementsForFiducialsEnabled,
                visualCalibrationFactor: visualCalibrationFactor,
            )
            converter.convert()
            System.exit(0);
        }

        if (options.ft) {
            FeederTester feederTester = new FeederTester(
                machine: machine,
                traysFileName: traysFileName,
                feedersFileName: feedersFileName,
                componentsFileName: componentsFileName,
                outputPrefix: outputPrefix,
                visualCalibrationFactor: visualCalibrationFactor,
            )
            feederTester.generateFeederTest()
            System.exit(0);
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        System.exit(-1);
    }

    static List<RefdesReplacement> parseRefdesReplacements(String[] refdesReplacementsValues) {
        List<RefdesReplacement> refdesReplacements = refdesReplacementsValues.findResults { refdesReplacementValue ->
            String[] refdesReplacementValues = refdesReplacementValue.split(',', 3)
            if (refdesReplacementValues.size() != 3) {
                return null
            }

            return new RefdesReplacement(refdes: refdesReplacementValues[0], name: refdesReplacementValues[1], value: refdesReplacementValues[2])
        }

        return refdesReplacements
    }

    static Optional<List<Fiducial>> parseFiducials(String[] fiducialsValues) {
        List<Fiducial> fiducials = fiducialsValues.findResults { fiducialsValue ->
            String[] fiducialValues = fiducialsValue.split(',')
            if (fiducialValues.size() != 3) {
                throw new IllegalArgumentException("Invalid fiducial specification; Use '<name>,<x.xx>,<y.yy>'")
            }

            DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

            return new Fiducial(note: fiducialValues[0], coordinate: new Coordinate(x: twoDigitDecimalFormat.parse(fiducialValues[1]), y: twoDigitDecimalFormat.parse(fiducialValues[2])))
        }

        final String FIDUCIAL_NOTE = 'For 2 point calibration 2 are required (RL, FR), for 3 point calibration 3 are required (RL, FR, FL).'
        if (fiducials.size() < 2) {
            throw new IllegalArgumentException("Insufficient fiducial markers; ${FIDUCIAL_NOTE}")
        } else if (fiducials.size() > 3) {
            throw new IllegalArgumentException("Too many fiducial markers; ${FIDUCIAL_NOTE}")
        }

        return Optional.of(fiducials)
    }

    static Mirroring.Mode parseMirroring(String arg) {
        String uppercaseArg = arg.toUpperCase()
        try {
            Mirroring.Mode mode = Mirroring.Mode.valueOf(uppercaseArg)
            return mode
        } catch (Exception e) {
            String[] candidates = Mirroring.Mode.values()
            throw new IllegalArgumentException("Unknown mode: $arg, expected $candidates", e)
        }
    }

    static PCBSideComponentPlacementFilter.SideInclusion parseSideInclusion(String arg) {
        String uppercaseArg = arg.toUpperCase()
        try {
            PCBSideComponentPlacementFilter.SideInclusion sideInclusion = PCBSideComponentPlacementFilter.SideInclusion.valueOf(uppercaseArg)
            return sideInclusion
        } catch (Exception e) {
            String[] candidates = PCBSideComponentPlacementFilter.SideInclusion.values()
            throw new IllegalArgumentException("Unknown side: $arg, expected $candidates", e)
        }
    }

    private static void about() {
        System.out.println('PNPConvert (C) 2018-2024 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
