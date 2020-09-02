package com.seriouslypro.pnpconvert.updater

class UnsupportedDPVContent extends RuntimeException {
    UnsupportedDPVContent(String message, String line, int lineIndex, int position) {
        String errorMessage = "Unsupported DPV file content, line: ${lineIndex}, offset ${position}\n"
        +line
        +(' ' * position) + "^  " + message

        new RuntimeException(errorMessage)
    }
}
