package com.seriouslypro.eda.part

import java.util.regex.Matcher
import java.util.regex.Pattern

class PatternParser {
    static Optional<Pattern> parsePattern(String s) {
        String trimmedValue = s.trim()

        Matcher matcher = trimmedValue =~ ~/^\/(.*)\/$/
        if (!matcher.matches()) {
            return Optional.empty()
        }
        String pattern = matcher[0][1]
        return Optional.of(Pattern.compile(pattern))
    }
}
