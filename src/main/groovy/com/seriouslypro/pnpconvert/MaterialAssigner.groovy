package com.seriouslypro.pnpconvert

import groovy.transform.InheritConstructors

class MaterialsAssigner {

    Map<ComponentPlacement, MaterialAssignment> assignMaterials(TrayFeederIDAssigner feederIDAssigner, Map<ComponentPlacement, MaterialSelectionEntry> materialSelections) {

        Set<Integer> usedIDs = materialSelections.findAll { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.present
        }.findResults { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->
            materialSelectionEntry.feeder.fixedId.get()
        }.sort()

        Map<ComponentPlacement, MaterialAssignment> materialAssignments = [:]

        materialSelections.each { ComponentPlacement placement, MaterialSelectionEntry materialSelectionEntry ->

            Feeder feeder = materialSelectionEntry.feeder

            Component component = materialSelectionEntry.component


            MaterialAssignment existingMaterialAssignment = materialAssignments.values().find { MaterialAssignment candidate ->
                candidate.feeder.is(feeder)
            }

            if (existingMaterialAssignment) {
                materialAssignments[placement] = existingMaterialAssignment
            } else {

                Integer feederId = null

                try {
                    feederId = feederIDAssigner.assignFeederID(usedIDs, feeder)
                } catch (InsufficientTrayIDsException e) {
                    throw new MaterialAssignmentException(materialAssignments, e)
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
}

@InheritConstructors
class MaterialAssignmentException extends Exception {

    Map<ComponentPlacement, MaterialAssignment> assignments

    MaterialAssignmentException(Map<ComponentPlacement, MaterialAssignment> assignments, Exception cause) {
        super(cause)
        this.assignments = assignments
    }
}
