package com.seriouslypro.pnpconvert

class MaterialAssignmentSorter {
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

    Map<ComponentPlacement, MaterialAssignment> sort(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        List<Comparator> comparators = [
            componentHeightComparator,
            componentAreaComparator,
            feederIdComparator
        ]

        Map<ComponentPlacement, MaterialAssignment> sortedMaterialAssignments = materialAssignments.sort { a, b ->
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

        return sortedMaterialAssignments
    }
}

class FeederIdComparator<MaterialAssignment> implements Comparator<MaterialAssignment> {

    @Override
    int compare(MaterialAssignment o1, MaterialAssignment o2) {
        return o1.feederId <=> o2.feederId // lowest first
    }
}

class ComponentAreaComparator<MaterialAssignment> implements Comparator<MaterialAssignment> {

    @Override
    int compare(MaterialAssignment o1, MaterialAssignment o2) {
        BigDecimal area1 = o1.component.area()
        BigDecimal area2 = o2.component.area()

        return area1 <=> area2 // smallest first
    }
}

class ComponentHeightComparator<MaterialAssignment> implements Comparator<MaterialAssignment> {

    @Override
    int compare(MaterialAssignment o1, MaterialAssignment o2) {
        //System.out.println("CHC - o1: ${o1.component.height}, o2: ${o2.component.height}")
        return o1.component.height.compareTo(o2.component.height) // shortest first
    }
}
