package com.seriouslypro.pnpconvert

interface FeederPrinter {
    boolean canPrint(Feeder feeder)
    Map<String,String> print(Feeder feeder)
}
