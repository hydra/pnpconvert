package com.seriouslypro.pnpconvert

class ReelFeederPrinter implements FeederPrinter {
    @Override
    boolean canPrint(Feeder feeder) {
        feeder instanceof ReelFeeder
    }

    @Override
    Map<String, String> print(Feeder feeder) {
        ReelFeeder reelFeeder = feeder as ReelFeeder

        String id = reelFeeder.fixedId.isPresent() ? reelFeeder.fixedId.get() as String : "<None>";
        [
            id: id,
            note: reelFeeder.note
        ]
    }
}
