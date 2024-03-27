package com.seriouslypro.pnpconvert

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.pnpconvert.machine.Machine
import org.checkerframework.checker.units.qual.C

class DPVGenerator {
    DPVHeader dpvHeader
    List<ComponentPlacement> placements
    Components components
    Feeders feeders
    BigDecimal offsetZ

    Machine machine

    DPVWriter writer

    List<ComponentPlacement> placementsWithUnknownComponents
    Set<Component> unloadedComponents
    Map<Feeder, Component> feedersMatchedByAlias
    Map<ComponentPlacement, Component> inexactComponentMatches

    Optional<Panel> optionalPanel
    Optional<List<Fiducial>> optionalFiducials
    List<PartMapping> partMappings

    void generate(OutputStream outputStream) {
        placementsWithUnknownComponents = []
        unloadedComponents = []
        feedersMatchedByAlias = [:]
        inexactComponentMatches = [:]

        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = selectMaterials()
        Map<ComponentPlacement, MaterialAssignment> materialAssignments = assignMaterials(materialSelections)

        MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()
        materialAssignments = materialAssignmentSorter.sort(materialAssignments)

        relocatePlacements(materialAssignments)

        dumpMaterialAsignments(materialAssignments)
        dumpSummary(optionalPanel, materialAssignments)

        System.out.println()
        System.out.println('*** ISSUES *** - Components that did not match, need verification or loading')
        System.out.println('')

        System.out.println()
        System.out.println("inexactComponentsMatches:\n" + inexactComponentMatches.collect { ComponentPlacement placement, Component component ->
            "placement: $placement, component: $component"
        }.join('\n'))

        System.out.println()
        System.out.println("feedersMatchedByAlias:\n" + feedersMatchedByAlias.collect { Feeder feeder, Component component ->
            "feederComponent: $feeder.description, component: $component"
        }.join('\n'))

        System.out.println()
        System.out.println("placementsWithUnknownComponents:\n" + placementsWithUnknownComponents.join('\n'))

        System.out.println()
        System.out.println("unloadedComponents:\n" + unloadedComponents.join('\n'))

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

    Map<ComponentPlacement, MaterialSelectionEntry> selectMaterials() {

        Map<ComponentPlacement, MaterialSelectionEntry> materialSelections = [:]

        placements.each { ComponentPlacement placement ->
            ComponentFindResult componentFindResult = components.findByPlacement(placement)

            if (!componentFindResult) {
                placementsWithUnknownComponents << placement
                return
            }

            if (!componentFindResult.isExactMatch()) {
                inexactComponentMatches[placement] = componentFindResult.component
            }

            Component component = componentFindResult.component

            //
            // feeder with component?
            //
            Feeder findResult = feeders.findByComponent(component)
            if (!findResult) {

                //
                // feeder with alias?
                //
                findResult = component.aliases.findResult { alias ->
                    feeders.findByComponent(new Component(name: alias))
                }

                if (!findResult) {
                    //
                    // alias component in feeder?
                    //
                    Component aliasComponent = components.components.find { otherComponent -> otherComponent.aliases.contains(component.name) }

                    if (aliasComponent) {
                        findResult = component.aliases.findResult { alias ->
                            feeders.findByComponent(aliasComponent)
                        }
                        if (findResult) {
                            component = aliasComponent
                        }
                    }

                    if (!findResult) {
                        unloadedComponents << component
                        return
                    }
                }

                feedersMatchedByAlias[findResult] = component

            }
            Feeder feeder = findResult

            MaterialSelectionEntry materialSelection = new MaterialSelectionEntry(
                component: component,
                feeder: feeder,
            )

            materialSelections[placement] = materialSelection
        }

        return materialSelections
    }

    Integer assignFeederID(NumberSequence trayIdSequence, Range trayIds, Set<Integer> usedIDs, Feeder feeder) throws IndexOutOfBoundsException {

        // Assign Feeder ID if required, code assumes feeder is a tray.
        // Trays may have a fixed id.  ID re-use needs to be avoided.

        Integer feederId = feeder.fixedId.orElseGet( {
            boolean found = false
            while (!found) {
                Integer candidateId = trayIdSequence.next()
                if (candidateId > machine.trayIds.to) {
                    throw new IndexOutOfBoundsException('No more tray IDs remaining, reduce the amount of trays required.  e.g. by splitting into multiple jobs.')
                }
                if (!usedIDs.contains(candidateId)) {
                    return candidateId
                }
            }
        })

        usedIDs.add(feederId)

        return feederId
    }

    Map<ComponentPlacement, MaterialAssignment> assignMaterials(Map<ComponentPlacement, MaterialSelectionEntry> materialSelections) {

        Set<Integer> usedIDs = materialSelections.findAll { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.present
        }.findResults { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.get()
        }.sort()

        Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]

        NumberSequence trayIdSequence = new NumberSequence(machine.trayIds.getFrom())

        materialSelections.each { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->

            Feeder feeder = materialSelectionEntry.feeder

            Component component = materialSelectionEntry.component


            MaterialAssignment existingMaterialAssignment = materialAssignments.values().find { MaterialAssignment candidate ->
                candidate.feeder.is(feeder)
            }

            if (existingMaterialAssignment) {
                materialAssignments[placement] = existingMaterialAssignment
            } else {

                Integer feederId

                try {
                    feederId = assignFeederID(trayIdSequence, machine.trayIds, usedIDs, feeder)
                } catch (IndexOutOfBoundsException e ) {
                    dumpSummary(optionalPanel, materialAssignments)
                    throw e
                }

                MaterialAssignment materialAssignment = new MaterialAssignment(
                    component: component,
                    feederId: feederId,
                    feeder: feeder
                )
                materialAssignments[placement] = materialAssignment
            }
        }

        return materialAssignments
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
                            name         : materialAssigment.value.component.name,
                            aliases      : materialAssigment.value.component.aliases,
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


