package com.seriouslypro.pnpconvert.updater

import groovy.transform.InheritConstructors

@InheritConstructors
class UnsupportedDPVContent extends RuntimeException {
    String line
    int lineIndex
    int position
    String errorMessage

    UnsupportedDPVContent(String message, String line, int lineIndex, int position) {
        super(message)
        this.line = line
        this.lineIndex = lineIndex
        this.position = position

        errorMessage = "Unsupported DPV file content, line: ${lineIndex}, offset ${position}\n" +
            line + "\n" +
            (' ' * position) + "^  " + message
    }

    String getMessage() {
        return errorMessage
    }
}
