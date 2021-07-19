package com.seriouslypro.eda.diptrace

import com.seriouslypro.eda.AngleConverter

class DiptraceAngleConverter implements AngleConverter {
    @Override
    BigDecimal edaToDesign(BigDecimal value) {
        return (360 - value).remainder(360)
    }

    @Override
    BigDecimal designToEDA(BigDecimal value) {
        return 0 - (value - 360).remainder(360)
    }
}