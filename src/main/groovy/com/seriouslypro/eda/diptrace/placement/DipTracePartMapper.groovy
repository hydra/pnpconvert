package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.eda.part.PartMapping
import com.seriouslypro.eda.part.PatternParser

import java.util.regex.Pattern

class DipTracePartMapper {

    List<PartMapping> buildOptions(List<PartMapping> partMappings, String name, String value) {
        List<PartMapping> options = partMappings.findAll { partMapping ->

            boolean nameMatched = partMapping.namePattern == name
            boolean valueMatched = partMapping.valuePattern == value

            if (nameMatched && valueMatched) {
                return true
            }

            Optional<Pattern> namePattern = PatternParser.parsePattern(partMapping.namePattern)
            Optional<Pattern> valuePattern = PatternParser.parsePattern(partMapping.valuePattern)

            nameMatched |= namePattern.present && name ==~ namePattern.get()
            valueMatched |= valuePattern.present && value ==~ valuePattern.get()

            return (nameMatched && valueMatched)
        }

        options
    }
}
