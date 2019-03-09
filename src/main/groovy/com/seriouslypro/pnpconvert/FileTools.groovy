package com.seriouslypro.pnpconvert

class FileTools {

    static boolean isUrl(String fileName) {
        try {
            new URL(fileName)
            return true
        } catch (Exception e) {
            return false
        }
    }

    static Reader openFileOrUrl(String fileName) {
        if (isUrl(fileName)) {
            URL url = new URL(fileName)
            return new StringReader(url.text)
        }

        InputStream inputStream = new FileInputStream(fileName)
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream)
        inputStreamReader
    }
}
