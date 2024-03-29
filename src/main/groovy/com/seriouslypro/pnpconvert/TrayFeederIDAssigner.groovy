package com.seriouslypro.pnpconvert

import groovy.transform.InheritConstructors

class TrayFeederIDAssigner {

    NumberSequence trayIdSequence
    Range trayIds

    TrayFeederIDAssigner(Range trayIds) {
        trayIdSequence = new NumberSequence(trayIds.getFrom())
        this.trayIds = trayIds
    }

    Integer assignFeederID(Set<Integer> usedIDs, Feeder feeder) throws InsufficientTrayIDsException {

        // Assign Feeder ID if required, code assumes feeder is a tray.
        // Trays may have a fixed id.  ID re-use needs to be avoided.

        Integer feederId = feeder.fixedId.orElseGet( {
            boolean found = false
            while (!found) {
                Integer candidateId = trayIdSequence.next()
                if (candidateId > trayIds.to) {
                    throw new InsufficientTrayIDsException('No more tray IDs remaining, reduce the amount of trays required.  e.g. by splitting into multiple jobs.')
                }
                if (!usedIDs.contains(candidateId)) {
                    return candidateId
                }
            }
        })

        usedIDs.add(feederId)

        return feederId
    }
}



@InheritConstructors
class InsufficientTrayIDsException extends Exception {}
