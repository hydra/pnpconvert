package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine

class DPVGenerator {
    DPVHeader dpvHeader
    BigDecimal offsetZ

    Machine machine
    MaterialsAssigner materialsAssigner = new MaterialsAssigner()

    DPVWriter writer

    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials

    void generate(OutputStream outputStream, Map<ComponentPlacement, MaterialSelectionEntry> materialSelections) {

        TrayFeederIDAssigner trayFeederIDAssigner = new TrayFeederIDAssigner(machine.trayIds)

        Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]
        try {
            materialAssignments = materialsAssigner.assignMaterials(trayFeederIDAssigner, materialSelections)
        } catch (MaterialAssignmentException e) {
            dumpSummary(optionalPanel, e.assignments)

            System.out.println("*** EXCEPTION - ${e}")
            return
        }

        MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()
        materialAssignments = materialAssignmentSorter.sort(materialAssignments)

        relocatePlacements(materialAssignments)

        dumpMaterialAsignments(materialAssignments)
        dumpSummary(optionalPanel, materialAssignments)

        writer = new DPVWriter(outputStream, machine, offsetZ, dpvHeader)
        writer.setPanel(optionalPanel)
        writer.setFiducials(optionalFiducials)
        writer.assignMaterials(materialAssignments)
        writer.write()
    }

    void relocatePlacements(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        materialAssignments.each { ComponentPlacement cp, MaterialAssignment ma ->
            cp.coordinate = cp.coordinate.relocate(cp.rotation, ma.component.placementOffsetX, ma.component.placementOffsetY)
        }
    }

    static def dumpMaterialAsignments(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        System.out.println('')
        System.out.println('*** MATERIAL ASSIGNMENTS *** - Components from the design that matched the components and feeders/trays')
        System.out.println('')

        System.out.println()
        System.out.println("materialAssignments:\n" + materialAssignments.collect { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            "placement: $placement, materialAssignment: $materialAssignment"
        }.join('\n'))
    }

    static def dumpSummary(Optional<Panel> optionalPanel, Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        List<Integer> usedFeederIds = materialAssignments.collect { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            materialAssignment.feederId
        }.unique().sort()

        System.out.println()
        System.out.println("usedFeeders:\n" + usedFeederIds.join(','))

        ArrayList<FeederPrinter> feederPrinters = [
                new TrayFeederPrinter(),
                new ReelFeederPrinter()
        ]

        int countOfUnitsInPanel = optionalPanel.map { panel -> panel.numberX * panel.numberY }.orElseGet { 1 }

        List<Map<String, String>> summaryItems = usedFeederIds.findResults { Integer feederId ->
            Map.Entry<ComponentPlacement, MaterialAssignment> materialAssigment = materialAssignments.find { ComponentPlacement placement, MaterialAssignment materialAssignment ->
                materialAssignment.feederId == feederId
            }
            Feeder feeder = materialAssigment.value.feeder
            FeederPrinter feederPrinter = feederPrinters.find { it.canPrint(feeder) }

            Map<ComponentPlacement, MaterialAssignment> materialAssigmentsUsingSameFeeder = materialAssignments.findAll { ComponentPlacement placement, MaterialAssignment materialAssignment ->
                materialAssignment.feederId == feederId
            }

            List<String> refdesList = materialAssigmentsUsingSameFeeder.keySet().findResults { ComponentPlacement placement -> placement.enabled ? placement.refdes : null }
            int countOfComponentsUsed = refdesList.size()
            int totalComponentsUserPerPanel = countOfComponentsUsed * countOfUnitsInPanel
            [
                    feederId          : materialAssigment.value.feederId.toString(),
                    componentsPerUnit : countOfComponentsUsed,
                    componentsPerPanel: totalComponentsUserPerPanel,
                    refdes            : refdesList,
                    feeder            : feederPrinter.print(feeder),
                    component         : [
                            partCode     : materialAssigment.value.component.partCode,
                            manufacturer : materialAssigment.value.component.manufacturer,
                            name         : materialAssigment.value.component.description,
                    ],
            ]
        }

        if (!summaryItems.isEmpty()) {
            System.out.println()
            System.out.println("feederSummary:")
            System.out.println(summaryItems.first().keySet().join(','))
            System.out.println(summaryItems.collect { it.values().join(',') }.join('\n'))
        }
    }

}


