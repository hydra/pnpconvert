package com.seriouslypro.pnpconvert.machine

import com.seriouslypro.pnpconvert.FeederProperties

class DefaultMachine extends Machine {
    @Override
    FeederProperties feederProperties(Integer id) {
        return defaultFeederProperties
    }
}
