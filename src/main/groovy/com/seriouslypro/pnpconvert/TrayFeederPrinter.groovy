package com.seriouslypro.pnpconvert

class TrayFeederPrinter implements FeederPrinter {
    @Override
    boolean canPrint(Feeder feeder) {
        feeder instanceof TrayFeeder
    }

    @Override
    Map<String,String> print(Feeder feeder) {
        TrayFeeder trayFeeder = feeder as TrayFeeder

        [
            tray: trayFeeder.tray.name,
            note: trayFeeder.note
        ]
    }
}
