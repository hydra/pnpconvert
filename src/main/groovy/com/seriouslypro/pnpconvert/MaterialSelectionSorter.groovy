package com.seriouslypro.pnpconvert

class MaterialSelectionSorter {
    FeederIdComparator feederIdComparator = new FeederIdComparator()
    ComponentAreaComparator componentAreaComparator = new ComponentAreaComparator()
    ComponentHeightComparator componentHeightComparator = new ComponentHeightComparator()

    boolean traceEnabled = false

    void trace(String message)
    {
        if (!traceEnabled) {
            return
        }

        System.println(message)
    }

    Map<ComponentPlacement, MaterialSelection> sort(Map<ComponentPlacement, MaterialSelection> materialSelections) {

        List<Comparator> comparators = [
            componentHeightComparator,
            componentAreaComparator,
            feederIdComparator
        ]

        Map<ComponentPlacement, MaterialSelection> sortedMaterialSelections = materialSelections.sort {a,  b ->
            trace("sorter A-Refdes: ${a.key.refdes}, B-Refdes: ${b.key.refdes}")
            int result = comparators.inject(0) { int runningResult, Comparator comparator ->
                trace("RR: $runningResult")
                if (runningResult == 0) {
                    trace("using comparator $comparator")
                    int comparatorResult = comparator.compare(a.value, b.value)
                    trace("comparator result: $comparatorResult")
                    return comparatorResult
                }
                trace("returning runningResult: $runningResult")
                return runningResult
            }
            return result
        }

        return sortedMaterialSelections
    }
}

class FeederIdComparator<MaterialSelection> implements Comparator<MaterialSelection> {

    @Override
    int compare(MaterialSelection o1, MaterialSelection o2) {
        return o1.feederId <=> o2.feederId // lowest first
    }
}

class ComponentAreaComparator<MaterialSelection> implements Comparator<MaterialSelection> {

    @Override
    int compare(MaterialSelection o1, MaterialSelection o2) {
        BigDecimal area1 = o1.component.area()
        BigDecimal area2 = o2.component.area()

        return area1 <=> area2 // smallest first
    }
}

class ComponentHeightComparator<MaterialSelection> implements Comparator<MaterialSelection> {

    @Override
    int compare(MaterialSelection o1, MaterialSelection o2) {
        //System.out.println("CHC - o1: ${o1.component.height}, o2: ${o2.component.height}")
        return o1.component.height.compareTo(o2.component.height) // shortest first
    }
}
