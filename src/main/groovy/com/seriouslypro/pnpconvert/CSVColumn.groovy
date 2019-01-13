package com.seriouslypro.pnpconvert

trait CSVColumn<T extends Enum> {
    List<String> aliases = []

    boolean matches(String candidateTitle) {
        String simpleCandidateTitle = candidateTitle.toLowerCase().replaceAll('[^A-Za-z0-9]', "_")

        boolean titleMatch = (this.toString().toLowerCase() == simpleCandidateTitle)
        if (titleMatch) {
            return titleMatch
        }

        boolean aliasMatch = aliases.find {
            String simpleAlias = it.toLowerCase().replaceAll('[^A-Za-z0-9]', "_")
            simpleAlias == simpleCandidateTitle
        }
        return aliasMatch
    }

    static <E extends CSVColumn> E fromString(Class<E> e, String column) {

        def enumSet = EnumSet.allOf(e)
        return enumSet.find { it ->
            it.matches(column)
        }
    }
}