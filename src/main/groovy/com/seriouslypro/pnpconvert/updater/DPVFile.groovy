package com.seriouslypro.pnpconvert.updater

class DPVFileParser {

    private enum Task {
        PROPERTY,
        PREPARE_TABLE,
        TABLE,
    }

    private int lineNumber = 0

    String nextLine(Scanner scanner) {
        String line = scanner.nextLine()
        lineNumber++
        return line
    }

    DPVFile parse(InputStream inputStream) {

        lineNumber = 0;

        DPVFile dpvFile = new DPVFile()

        Scanner scanner = new Scanner(inputStream)
        String line
        line = nextLine(scanner)

        if (line != "separated") {
            throw new UnsupportedDPVContent("Expected first line to start with 'separated'", line, lineNumber, 0)
        }

        Task task = Task.PROPERTY

        DPVTable activeTable

        while (scanner.hasNextLine()) {
            line = nextLine(scanner)

            if (line.trim().empty) {
                continue
            }

            boolean done = false
            while (!done) {

                switch (task) {
                    case Task.PROPERTY:
                        if (!isTableHeader(line)) {
                            parseProperty(dpvFile.properties, line)
                            done = true
                        } else {
                            task = Task.PREPARE_TABLE
                        }
                        break;

                    case Task.PREPARE_TABLE:
                        if (isTableHeader(line)) {
                            activeTable = new DPVTable()
                            activeTable.parseHeaders(line, lineNumber)
                        } else {
                            activeTable.parseFirstTableEntry(line, lineNumber)
                            task = Task.TABLE

                            // table name is only known after the first entry has been read
                            dpvFile.tables[activeTable.name] = activeTable
                        }
                        done = true

                        break;

                    case Task.TABLE:
                        if (isTableHeader(line)) {
                            task = Task.PREPARE_TABLE
                            done = false
                        } else
                        {
                            activeTable.parseTableEntry(line, lineNumber)
                            done = true
                        }
                        break;
                }
            }
        }

        return dpvFile
    }

    void parseProperty(Properties properties, String line) {
        String key
        String value

        try {
            (key, value) = line.split(",", 2)
        } catch (Exception e) {
            throw new UnsupportedDPVContent("Expected 'key,value'", line, lineNumber, 0)
        }

        properties.setProperty(key, value)
    }

    static boolean isTableHeader(String line) {
        return line.startsWith('Table')
    }
}

class DPVFile {
    Properties properties = new Properties()
    Map<String, DPVTable> tables = [:]
}

class DPVTable {
    String name
    List<String> headers = []
    List<List<String>> entries = []

    void parseHeaders(String line, int lineIndex) {
        List<String> values = parseValues(line, lineIndex)

        String firstValue = values.pop()
        assert(firstValue == "Table")

        headers = values
    }

    void parseFirstTableEntry(String line, int lineIndex) {
        List<String> values = parseValues(line, lineIndex)
        String tableName = values.pop()
        name = tableName

        verifyValueCount(values)

        entries << values
    }

    void parseTableEntry(String line, int lineIndex) {
        List<String> values = parseValues(line, lineIndex)

        String tableName = values.pop()
        if (tableName != name) {
            throw new UnsupportedDPVContent("Unexpected table name '${tableName}'", line, lineIndex, 0)
        }

        verifyValueCount(values)

        entries << values
    }

    void verifyValueCount(List<String> values) {
        int expectedCount = headers.size()
        int actualCount = values.size()
        if (actualCount != expectedCount) {
            throw new UnsupportedDPVContent("Value count mismatch, expected: ${expectedCount}, actual: ${actualCount}", line, lineIndex, 0)
        }

    }

    private List<String> parseValues(String line, lineIndex) {
        List<String> values = line.split(',')
        if (values.size() == 0) {
            throw new UnsupportedDPVContent("No values to parse", line, lineIndex, 0)
        }
        values
    }
}