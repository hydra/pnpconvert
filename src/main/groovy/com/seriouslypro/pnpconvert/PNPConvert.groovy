package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.CHMT48VB
import com.seriouslypro.pnpconvert.machine.Machine
import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

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
        builder.r(args:1, argName: 'rotation', 'rotation degrees (positive is clockwise)')
        builder.s(args:1, argName: 'side', 'pcb side (top|bottom|all), default is all')

        builder.rx(args:1, argName: 'rotationX', 'rotation X origin')
        builder.ry(args:1, argName: 'rotationY', 'rotation Y origin')

        builder.ox(args:1, argName: 'offsetX', 'X offset, applied after rotation')
        builder.oy(args:1, argName: 'offsetY', 'Y offset, applied after rotation')
        builder.oz(args:1, argName: 'offset', 'Z offset, applied to all component heights - increase for thicker PCBs')

        builder.pnx(args:1, argName: 'panelNumberX','Number of PCBs on the X axis')
        builder.pny(args:1, argName: 'panelNumberY','Number of PCBs on the Y axis')
        builder.pix(args:1, argName: 'panelIntervalX','Interval spacing on the X axis')
        builder.piy(args:1, argName: 'panelIntervalY','Interval spacing on the Y axis')

        builder.cfg(args:1, argName: 'config', 'configuration file (in "key=value" format)')

        builder.dr(args:1, argName: 'disableRefdes', 'Disable components by refdes (comma separated list)')
        builder.rr(args:'+', argName: 'replaceRefdes', 'Replace components by refdes ("refdes,value,name"[ ...])')

        builder.fm(args:'+', argName: 'fiducialMarkers','Fiducial marker list (note,x,y[ ...])')


        builder.c('convert')

        OptionAccessor options = builder.parse(args)

        if (!options || options.getCommandLine().options.size() == 0) {
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

        BoardRotation boardRotation = new BoardRotation()
        Coordinate offsetXY = new Coordinate()
        BigDecimal offsetZ = 0
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

        if (options.co) {
            componentsFileName = options.co
        }

        if (options.r) {
            boardRotation.degrees = options.r as BigDecimal
        }

        if (options.rx ) {
            boardRotation.origin.x = (options.rx as BigDecimal)
        }

        if (options.ry) {
            boardRotation.origin.y = (options.ry as BigDecimal)
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
            String allDRValues = options.getCommandLine().getOptionValues("dr").join(",")
            placementReferenceDesignatorsToDisable = allDRValues.split(',').collect { it.trim().toUpperCase() }.unique()
        }

        boolean havePanelOption = (options.pix || options.piy || options.pnx || options.pny)
        if (havePanelOption) {
            boolean haveRequiredPanelOptions = options.pix && options.piy && options.pnx && options.pny
            if (!haveRequiredPanelOptions) {
                builder.usage()
                System.exit(-1)
            }

            optionalPanel = Optional.of(new Panel(
                intervalX: options.pix as BigDecimal,
                intervalY: options.piy as BigDecimal,
                numberX: options.pnx as int,
                numberY: options.pny as int,
            ))
        }

        if (options.fm) {
            String[] fiducialMarkerValues = options.getCommandLine().getOptionValues("fm");
            if (fiducialMarkerValues.size() == 2) {
                optionalFiducials = parseFiducials(fiducialMarkerValues)
            }
        }

        if (options.rr) {
            String[] refdesReplacementValues = options.getCommandLine().getOptionValues("rr");
            refdesReplacements = parseRefdesReplacements(refdesReplacementValues)
        }


        if (options.s) {
            sideInclusion = parseSideInclusion(options.s)
        }

        if (options.c) {
            Converter converter = new Converter(
                machine: machine,
                inputFileName: inputFileName,
                traysFileName: traysFileName,
                feedersFileName: feedersFileName,
                componentsFileName: componentsFileName,
                outputPrefix: outputPrefix,
                boardRotation: boardRotation,
                offsetXY: offsetXY,
                offsetZ: offsetZ,
                sideInclusion: sideInclusion,
                optionalPanel: optionalPanel,
                optionalFiducials: optionalFiducials,
                refdesReplacements: refdesReplacements,
                placementReferenceDesignatorsToDisable: placementReferenceDesignatorsToDisable
            )
            converter.convert()
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
                return null
            }

            DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

            return new Fiducial(note: fiducialValues[0], coordinate: new Coordinate(x: twoDigitDecimalFormat.parse(fiducialValues[1]), y: twoDigitDecimalFormat.parse(fiducialValues[2])))
        }

        return Optional.of(fiducials)
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
        System.out.println('PNPConvert (C) 2018 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
