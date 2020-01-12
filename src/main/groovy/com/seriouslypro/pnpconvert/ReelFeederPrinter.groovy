package com.seriouslypro.pnpconvert

class ReelFeederPrinter implements FeederPrinter {
    @Override
    boolean canPrint(Feeder feeder) {
        feeder instanceof ReelFeeder
    }

    @Override
    Map<String, String> print(Feeder feeder) {
        ReelFeeder reelFeeder = feeder as ReelFeeder

        [
            id: reelFeeder.fixedId.get() as String,
            note: reelFeeder.note
        ]
    }
}
