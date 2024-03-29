package com.seriouslypro.pnpconvert

class FeedersHelper {
    static Feeder createReelFeeder(int id, int tapeWidth, String partCode, String manufacturer, String description, PickSettings pickSettings, String note) {
        new ReelFeeder(
            fixedId: Optional.of(id),
            tapeWidth: tapeWidth,
            partCode: partCode,
            manufacturer: manufacturer,
            description: description,
            pickSettings: pickSettings,
            note: note,
        )
    }

    static Feeder createTrayFeeder(Tray tray, String partCode, String manufacturer, String description, PickSettings pickSettings, String note) {
        new TrayFeeder(
            tray: tray,
            partCode: partCode,
            manufacturer: manufacturer,
            description: description,
            pickSettings: pickSettings,
            note: note,
        )
    }
}
