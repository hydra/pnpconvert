package com.seriouslypro.pnpconvert

import spock.lang.Specification

class MaterialAssignmentSorterSpec extends Specification {

    def 'sort empty map'() {
        given:
            MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()

        when:
            def result = materialAssignmentSorter.sort([:])

        then:
            result == [:]
    }

    def 'sort components by height'() {
        given:
            MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [
                (componentPlacement1): new MaterialAssignment(component: new Component(height: 3)),
                (componentPlacement2): new MaterialAssignment(component: new Component(height: 1)),
                (componentPlacement3): new MaterialAssignment(component: new Component(height: 2)),
            ]

        when:
            def result = materialAssignmentSorter.sort(materialAssignments)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }

    def 'sort components by feeder id'() {
        given:
            MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [
                (componentPlacement1): new MaterialAssignment(feederId: 3, component: new Component()),
                (componentPlacement2): new MaterialAssignment(feederId: 1, component: new Component()),
                (componentPlacement3): new MaterialAssignment(feederId: 2, component: new Component()),
            ]

        when:
            def result = materialAssignmentSorter.sort(materialAssignments)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }

    def 'sort components by area'() {
        given:
            MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [
                (componentPlacement1): new MaterialAssignment(component: new Component(width: 3, length: 3)),
                (componentPlacement2): new MaterialAssignment(component: new Component(width: 1, length: 1)),
                (componentPlacement3): new MaterialAssignment(component: new Component(width: 2, length: 2)),
            ]

        when:
            def result = materialAssignmentSorter.sort(materialAssignments)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }


    def 'sort components by height then area then feederId'() {
        given:
            MaterialAssignmentSorter materialAssignmentSorter = new MaterialAssignmentSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )
            ComponentPlacement componentPlacement4 = new ComponentPlacement(refdes: 'D' )
            ComponentPlacement componentPlacement5 = new ComponentPlacement(refdes: 'E' )
            ComponentPlacement componentPlacement6 = new ComponentPlacement(refdes: 'F' )

            Map<ComponentPlacement, MaterialAssignment> materialAssignments = [
                (componentPlacement1): new MaterialAssignment(component: new Component(width: 3, length: 3, height: 1), feederId: 6),
                (componentPlacement2): new MaterialAssignment(component: new Component(width: 3, length: 3, height: 2), feederId: 7),
                (componentPlacement3): new MaterialAssignment(component: new Component(width: 1, length: 1, height: 1), feederId: 8),
                (componentPlacement4): new MaterialAssignment(component: new Component(width: 2, length: 2, height: 2), feederId: 9),
                (componentPlacement5): new MaterialAssignment(component: new Component(width: 2, length: 2, height: 2), feederId: 10),
                (componentPlacement6): new MaterialAssignment(component: new Component(width: 0.25, length: 4, height: 1), feederId: 11),
            ]

        and:
            assert(materialAssignments[componentPlacement1].component.area() == materialAssignments[componentPlacement2].component.area())
            assert(materialAssignments[componentPlacement3].component.area() == materialAssignments[componentPlacement6].component.area())
            assert(materialAssignments[componentPlacement4].component.area() == materialAssignments[componentPlacement5].component.area())

        and:
            assert(materialAssignments[componentPlacement1].component.height < materialAssignments[componentPlacement2].component.height)

        and:
            assert(materialAssignments[componentPlacement3].feederId < materialAssignments[componentPlacement6].feederId)

        when:
            def result = materialAssignmentSorter.sort(materialAssignments)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement3, componentPlacement6, componentPlacement1, componentPlacement4, componentPlacement5, componentPlacement2
            ]
            keySet == expectedKeySet
    }
}
