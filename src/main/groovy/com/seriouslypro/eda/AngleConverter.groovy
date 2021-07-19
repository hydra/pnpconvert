package com.seriouslypro.eda

interface AngleConverter {
    BigDecimal edaToDesign(BigDecimal value)
    BigDecimal designToEDA(BigDecimal value)
}

