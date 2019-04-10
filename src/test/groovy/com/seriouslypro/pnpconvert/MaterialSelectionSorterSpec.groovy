package com.seriouslypro.pnpconvert

import spock.lang.Specification

class MaterialSelectionSorterSpec extends Specification {

    def 'sort empty map'() {
        given:
            MaterialSelectionSorter materialSelectionSorter = new MaterialSelectionSorter()

        when:
            def result = materialSelectionSorter.sort([:])

        then:
            result == [:]
    }

    def 'sort components by height'() {
        given:
            MaterialSelectionSorter materialSelectionSorter = new MaterialSelectionSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialSelection> materialSelections = [
                (componentPlacement1): new MaterialSelection(component: new Component(height: 3)),
                (componentPlacement2): new MaterialSelection(component: new Component(height: 1)),
                (componentPlacement3): new MaterialSelection(component: new Component(height: 2)),
            ]

        when:
            def result = materialSelectionSorter.sort(materialSelections)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }

    def 'sort components by feeder id'() {
        given:
            MaterialSelectionSorter materialSelectionSorter = new MaterialSelectionSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialSelection> materialSelections = [
                (componentPlacement1): new MaterialSelection(feederId: 3, component: new Component()),
                (componentPlacement2): new MaterialSelection(feederId: 1, component: new Component()),
                (componentPlacement3): new MaterialSelection(feederId: 2, component: new Component()),
            ]

        when:
            def result = materialSelectionSorter.sort(materialSelections)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }

    def 'sort components by area'() {
        given:
            MaterialSelectionSorter materialSelectionSorter = new MaterialSelectionSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )

            Map<ComponentPlacement, MaterialSelection> materialSelections = [
                (componentPlacement1): new MaterialSelection(component: new Component(width: 3, length: 3)),
                (componentPlacement2): new MaterialSelection(component: new Component(width: 1, length: 1)),
                (componentPlacement3): new MaterialSelection(component: new Component(width: 2, length: 2)),
            ]

        when:
            def result = materialSelectionSorter.sort(materialSelections)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement2, componentPlacement3, componentPlacement1
            ]
            keySet == expectedKeySet
    }


    def 'sort components by height then area then feederId'() {
        given:
            MaterialSelectionSorter materialSelectionSorter = new MaterialSelectionSorter()

            ComponentPlacement componentPlacement1 = new ComponentPlacement(refdes: 'A' )
            ComponentPlacement componentPlacement2 = new ComponentPlacement(refdes: 'B' )
            ComponentPlacement componentPlacement3 = new ComponentPlacement(refdes: 'C' )
            ComponentPlacement componentPlacement4 = new ComponentPlacement(refdes: 'D' )
            ComponentPlacement componentPlacement5 = new ComponentPlacement(refdes: 'E' )
            ComponentPlacement componentPlacement6 = new ComponentPlacement(refdes: 'F' )

            Map<ComponentPlacement, MaterialSelection> materialSelections = [
                (componentPlacement1): new MaterialSelection(component: new Component(width: 3, length: 3, height: 1), feederId: 6),
                (componentPlacement2): new MaterialSelection(component: new Component(width: 3, length: 3, height: 2), feederId: 7),
                (componentPlacement3): new MaterialSelection(component: new Component(width: 1, length: 1, height: 1), feederId: 8),
                (componentPlacement4): new MaterialSelection(component: new Component(width: 2, length: 2, height: 2), feederId: 9),
                (componentPlacement5): new MaterialSelection(component: new Component(width: 2, length: 2, height: 2), feederId: 10),
                (componentPlacement6): new MaterialSelection(component: new Component(width: 0.25, length: 4, height: 1), feederId: 11),
            ]

        and:
            assert(materialSelections[componentPlacement1].component.area() == materialSelections[componentPlacement2].component.area())
            assert(materialSelections[componentPlacement3].component.area() == materialSelections[componentPlacement6].component.area())
            assert(materialSelections[componentPlacement4].component.area() == materialSelections[componentPlacement5].component.area())

        and:
            assert(materialSelections[componentPlacement1].component.height < materialSelections[componentPlacement2].component.height)

        and:
            assert(materialSelections[componentPlacement3].feederId < materialSelections[componentPlacement6].feederId)

        when:
            def result = materialSelectionSorter.sort(materialSelections)

        then:
            ArrayList keySet = result.keySet()
            ArrayList expectedKeySet = [
                componentPlacement3, componentPlacement6, componentPlacement1, componentPlacement4, componentPlacement5, componentPlacement2
            ]
            keySet == expectedKeySet
    }
}
