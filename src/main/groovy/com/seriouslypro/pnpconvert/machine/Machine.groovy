package com.seriouslypro.pnpconvert.machine

import com.seriouslypro.pnpconvert.FeederProperties

abstract class Machine {
    protected FeederProperties defaultFeederProperties = new FeederProperties()
    Range<Integer> trayIds

    abstract FeederProperties feederProperties(Integer id)
}
