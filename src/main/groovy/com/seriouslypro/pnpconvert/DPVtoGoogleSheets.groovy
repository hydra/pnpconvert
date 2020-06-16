package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.CHMT48VB
import com.seriouslypro.pnpconvert.machine.Machine
import com.seriouslypro.pnpconvert.updater.DPVtoGoogleSheetsUpdater
import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

import java.text.DecimalFormat

/*
References:

Google APIs
https://developers.google.com/api-client-library/java

Google Sheets API
https://github.com/googleapis/google-api-java-client-services/tree/master/clients/google-api-services-sheets/v4

Google Sheets API Javadoc
https://googleapis.dev/java/google-api-services-sheets/latest/index.html

Quick start:
https://developers.google.com/sheets/api/quickstart/java

 */
class DPVtoGoogleSheets {

    public static void main(String [] args) {
        processArgs(args)
    }

    static def processArgs(String[] args) {
        CliBuilder builder = new CliBuilder(usage: 'dpvtogooglesheets')
        builder.v('version')
        builder.i(args:1, argName: 'input', 'input dpv file/url')
        builder.s(args:1, argName: 'sheet', 'sheet id')
        builder.c(args:1, argName: 'credentials', 'credentials json file/url')
        builder.mo(args:'+', argName: 'match-options', 'match options')

        builder.cfg(args:1, argName: 'config', 'configuration file (in "key=value" format)')

        builder.u('update')

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

        String inputFileName = config.getOrDefault("input", "input.dpv")
        String credentialsFileName = config.getOrDefault("credentials","credentials.json")
        String sheetId = config.getOrDefault("sheetId","")

        Set<MatchOption> matchOptions = [MatchOption.FEEDER_ID, MatchOption.COMPONENT_NAME]

        if (options.i) {
            inputFileName = options.i
        }

        if (options.s) {
            sheetId = options.s
        }

        if (options.c) {
            credentialsFileName = options.c
        }


        String[] matchOptionValues = options.getCommandLine().getOptionValues("mo");
        if (matchOptionValues && matchOptionValues.size() > 0) {
            matchOptions = parseMatchOptions(matchOptionValues)
        }

        if (options.u) {
            boolean haveRequiredOptions = !sheetId.empty

            if (haveRequiredOptions) {
                DPVtoGoogleSheetsUpdater updater = new DPVtoGoogleSheetsUpdater(
                    inputFileName: inputFileName,
                    sheetId: sheetId,
                    credentialsFileName: credentialsFileName,
                    matchOptions: matchOptions,
                )
                updater.update()
                System.exit(0);
            }
        }

        about()

        System.out.println('invalid parameter combinations')
        builder.usage()
        System.exit(-1);
    }

    static Set<MatchOption> parseMatchOptions(String[] strings) {
        Set<MatchOption> matchOptions = strings.collect { String arg ->
            String uppercaseArg = arg.toUpperCase()

            try {
                MatchOption matchOption = MatchOption.valueOf(uppercaseArg)
                return matchOption
            } catch (Exception e) {
                String[] candidates = MatchOption.values()
                throw new IllegalArgumentException("Unknown match option: $arg, expected any of $candidates", e)
            }
        }
        matchOptions
    }

    private static void about() {
        System.out.println('DPVtoGoogleSheets (C) 2020 Dominic Clifton')
        System.out.println('Written by Dominic Clifton')
    }
}
