package com.seriouslypro.pnpconvert.updater

class ColumnIdentifier {
    static String fromInt(int n) {
        String result = ""

        while (n > 0) {
            int remainder = (n - 1) % 26
            n = (n - 1) / 26

            String c = String.valueOf(Character.toChars(65 + remainder))
            result = c + result
        }
        if (result.empty) {
            throw new IllegalArgumentException("Unable to convert value to Column identifier, value: ${n}")
        }
        return result
    }
}