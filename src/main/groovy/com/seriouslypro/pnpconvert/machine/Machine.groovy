package com.seriouslypro.pnpconvert.machine

import com.seriouslypro.pnpconvert.FeederProperties

abstract class Machine {
    protected FeederProperties defaultFeederProperties = new FeederProperties()

    abstract FeederProperties feederProperties(Integer id)
}
