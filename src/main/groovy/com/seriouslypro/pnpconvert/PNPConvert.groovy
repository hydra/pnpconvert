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
        builder.i(args:1, argName: 'input', 'input csv file')
        builder.o(args:1, argName: 'output', 'output prefix')
        builder.f(args:1, argName: 'feeders', 'feeders csv file')
        builder.r(args:1, argName: 'rotation', 'rotation degrees (positive is clockwise)')
//        builder.w(args:1, argName: 'width', 'pcb width')
//        builder.h(args:1, argName: 'height', 'pcb height')
        builder.rx(args:1, argName: 'rotationX', 'rotation X origin')
        builder.ry(args:1, argName: 'rotationY', 'rotation Y origin')

//        builder.ox(args:1, argName: 'offsetX', 'X offset, applied after rotation')
//        builder.oy(args:1, argName: 'offsetY', 'Y offset, applied after rotation')
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

        String inputFileName = "place.csv"
        String outputPrefix = "place"
        String feedersFileName = "feeders.csv"
        BoardRotation boardRotation = new BoardRotation()

        if (options.i) {
            inputFileName = options.i
        }

        if (options.o) {
            outputPrefix = options.o
        }

        if (options.f) {
            feedersFileName = options.f
        }

        if (options.r) {
            boardRotation.degrees = options.r as BigDecimal
        }

        /*
        if ((options.w && !options.h) || (!options.w && options.h)) {
            System.out.println('specify width and height')
            builder.usage()
            System.exit(-1);
        }

        if (options.w && options.h) {
            boardRotation.origin.x = (options.w as BigDecimal) / 2
            boardRotation.origin.y = (options.h as BigDecimal) / 2
        }
        */

        if (options.rx ) {
            boardRotation.origin.x = (options.rx as BigDecimal)
        }

        if (options.ry) {
            boardRotation.origin.y = (options.ry as BigDecimal)
        }

        if (options.c) {
            Converter converter = new Converter(inputFileName, feedersFileName, outputPrefix, boardRotation)
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
