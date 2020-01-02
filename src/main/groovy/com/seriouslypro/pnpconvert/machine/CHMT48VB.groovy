package com.seriouslypro.pnpconvert.machine

import com.seriouslypro.pnpconvert.FeederProperties

class CHMT48VB extends Machine {
    Range trayIds = 91..99

    FeederProperties leftFeederProperties = new FeederProperties(
            feederAngle: 270
    )
    FeederProperties rightFeederProperties = new FeederProperties(
            feederAngle: 90
    )

    @Override
    FeederProperties feederProperties(Integer id) {
        if (id >= 1 && id <= 35) {
            return leftFeederProperties
        }
        if (id >= 36 && id <= 70) {
            return rightFeederProperties
        }

        return defaultFeederProperties
    }
}
