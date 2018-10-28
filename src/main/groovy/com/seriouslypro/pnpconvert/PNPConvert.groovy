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
        builder.o(args:1, argName: 'output', 'output dpv file')
        builder.f(args:1, argName: 'feeders', 'feeders csv file')
        builder.r(args:1, argName: 'rotation', 'rotation degrees (positive is clockwise)')
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
        String outputFileName = "place.dpv"
        String feedersFileName = "feeders.csv"
        BigDecimal rotationDegrees = 0.0G

        if (options.i) {
            inputFileName = options.i
        }

        if (options.o) {
            outputFileName = options.o
        }

        if (options.f) {
            feedersFileName = options.f
        }

        if (options.r) {
            rotationDegrees = options.r as BigDecimal
        }

        if (options.c) {
            Converter converter = new Converter(inputFileName, feedersFileName, outputFileName, rotationDegrees)
            converter.go()
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
