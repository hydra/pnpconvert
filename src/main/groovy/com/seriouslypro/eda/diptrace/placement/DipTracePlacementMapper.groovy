package com.seriouslypro.eda.diptrace.placement

import com.seriouslypro.eda.part.PartSubstitution
import com.seriouslypro.eda.part.PatternParser

import java.util.regex.Pattern

class DipTracePlacementMapper {

    List<PartSubstitution> buildOptions(List<PartSubstitution> partSubstitutions, String name, String value) {
        List<PartSubstitution> options = partSubstitutions.findAll { partSubstitution ->

            boolean nameMatched = partSubstitution.namePattern == name
            boolean valueMatched = partSubstitution.valuePattern == value

            if (nameMatched && valueMatched) {
                return true
            }

            Optional<Pattern> namePattern = PatternParser.parsePattern(partSubstitution.namePattern)
            Optional<Pattern> valuePattern = PatternParser.parsePattern(partSubstitution.valuePattern)

            nameMatched |= namePattern.present && name ==~ namePattern.get()
            valueMatched |= valuePattern.present && value ==~ valuePattern.get()

            return (nameMatched && valueMatched)
        }

        options
    }
}
