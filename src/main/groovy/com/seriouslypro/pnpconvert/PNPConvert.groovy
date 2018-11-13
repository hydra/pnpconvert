package com.seriouslypro.pnpconvert

import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

class PNPConvert {

    public static void main(String [] args) {
        processArgs(args)
    }

    static def processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'pnpconvert')
        builder.v('version')
        builder.s(args:1, argName: 'source directory', 'scan and import csv files')
        builder.i(args:1, argName: 'input', 'input csv file/url')
        builder.o(args:1, argName: 'output', 'output prefix')
        builder.t(args:1, argName: 'trays', 'trays csv file/url')
        builder.f(args:1, argName: 'feeders', 'feeders csv file/url')
        builder.co(args:1, argName: 'components', 'components csv file/url')
        builder.r(args:1, argName: 'rotation', 'rotation degrees (positive is clockwise)')

        builder.rx(args:1, argName: 'rotationX', 'rotation X origin')
        builder.ry(args:1, argName: 'rotationY', 'rotation Y origin')

        builder.ox(args:1, argName: 'offsetX', 'X offset, applied after rotation')
        builder.oy(args:1, argName: 'offsetY', 'Y offset, applied after rotation')

        builder.cfg(args: 1, argName: 'config', 'configuration file (in "key=value" format)')

        builder.c('convert')

        OptionAccessor options = builder.parse(args)
        options.arguments()

        if (!options) {
            builder.usage()
            System.exit(-1);
        }

        if (options.v) {
            about();
            InputStream stream = this.getClass().getResourceAsStream('/version.properties')

            Properties versionProperties = new Properties();
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
        Coordinate offset = new Coordinate()

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
            offset.x = (options.ox as BigDecimal)
        }

        if (options.oy) {
            offset.y = (options.oy as BigDecimal)
        }

        if (options.c) {
            Converter converter = new Converter(
                inputFileName: inputFileName,
                traysFileName: traysFileName,
                feedersFileName: feedersFileName,
                componentsFileName: componentsFileName,
                outputPrefix: outputPrefix,
                boardRotation: boardRotation,
                offset: offset
            )
            converter.convert()
            System.exit(0);
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        System.exit(-1);
    }


    private static void about() {
        System.out.println('PNPConvert (C) 2018 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
