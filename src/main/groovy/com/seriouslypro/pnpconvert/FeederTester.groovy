package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine

import static com.seriouslypro.pnpconvert.FileTools.openFileOrUrl

class FeederTester {
    Machine machine
    String traysFileName
    String feedersFileName
    String componentsFileName
    String outputPrefix

    private static final boolean append = false

    void generateFeederTest() {

        //
        // Load Components
        //

        ComponentsLoader components = loadComponents()

        System.out.println()
        System.out.println("defined components:")
        components.components.each { Component component ->
            System.out.println(component)
        }

        System.out.println()

        //
        // Load Trays
        //

        TraysLoader trays = loadTrays()

        System.out.println()
        System.out.println("defined trays:")
        trays.trays.each { Tray tray ->
            System.out.println(tray)
        }

        //
        // Load Feeders
        //

        FeedersLoader feeders = loadFeeders(trays)

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
            pcbFileName: "NONE"
        )

        DPVWriter dpvWriter = new DPVWriter(outputStream, machine, 0.0, dpvHeader)
        feeders.feederList.each { Feeder feeder ->
            feeder.fixedId.ifPresent { feederId ->
                Component component = components.components.find { candidate ->
                    candidate.partCode == feeder.partCode && candidate.manufacturer == feeder.manufacturer &&
                        feeder.partCode && candidate.partCode &&
                        feeder.manufacturer && candidate.manufacturer
                }

                if (component) {
                    dpvWriter.addMaterial(feederId, feeder, component)
                }
            }
        }

        dpvWriter.write()

        outputStream.close()
    }


    private TraysLoader loadTrays() {
        Reader reader = openFileOrUrl(traysFileName)

        TraysLoader trays = new TraysLoader()
        trays.loadFromCSV(traysFileName, reader)

        trays
    }

    private FeedersLoader loadFeeders(TraysLoader trays) {
        Reader reader = openFileOrUrl(feedersFileName)

        FeedersLoader feeders = new FeedersLoader(
            traysLoader: trays
        )

        feeders.loadFromCSV(feedersFileName, reader)
        feeders
    }

    private ComponentsLoader loadComponents() {
        Reader reader = openFileOrUrl(componentsFileName)

        ComponentsLoader components = new ComponentsLoader()
        components.loadFromCSV(componentsFileName, reader)

        components
    }
}
