package edu.uob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class UpdateQuery {
    private String storageFolderPath;

    public UpdateQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath).toAbsolutePath().toString();
    }

    public String updateTable(String database, String tableName, String[] setColumns, String[] setValues, String[] whereColumns, String[] whereValues, String[] operators) {
        Path tablePath = Paths.get(storageFolderPath, database, tableName + ".tab");
        if (!Files.exists(tablePath)) {
            return "[ERROR] Table does not exist.";
        }

        try {
            List<String> lines = Files.readAllLines(tablePath);
            String[] headers = lines.get(0).split("\t");

            for (String column : setColumns) {
                if (column.equalsIgnoreCase("id")) {
                    return "[ERROR] The 'id' column cannot be updated or altered.";
                }
            }

            int[] colIndexesToUpdate = new int[setColumns.length];
            for (int i = 0; i < setColumns.length; i++) {
                colIndexesToUpdate[i] = findColumnIndex(headers, setColumns[i]);
                if (colIndexesToUpdate[i] == -1) {
                    return "[ERROR] Column '" + setColumns[i] + "' does not exist.";
                }
            }

            int[] colIndexesToCheck = new int[whereColumns.length];
            for (int i = 0; i < whereColumns.length; i++) {
                colIndexesToCheck[i] = findColumnIndex(headers, whereColumns[i]);
                if (colIndexesToCheck[i] == -1) {
                    return "[ERROR] Column '" + whereColumns[i] + "' does not exist.";
                }
            }

            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split("\t");

                if (values.length < headers.length) {
                    values = Arrays.copyOf(values, headers.length);
                    Arrays.fill(values, values.length, headers.length, ""); // Fill new columns with default value
                }

                boolean matchesAllConditions = true;
                for (int j = 0; j < whereColumns.length; j++) {
                    if (!checkCondition(values[colIndexesToCheck[j]], whereValues[j], operators[j])) {
                        matchesAllConditions = false;
                        break;
                    }
                }

                if (matchesAllConditions) {
                    for (int j = 0; j < setColumns.length; j++) {
                        values[colIndexesToUpdate[j]] = setValues[j];
                    }
                    lines.set(i, String.join("\t", values));
                }
            }

            Files.write(tablePath, lines);
            return "[OK] Table updated successfully.";
        } catch (IOException e) {
            e.printStackTrace();
            return "[ERROR] Failed to update table.";
        }
    }

    private int findColumnIndex(String[] headers, String column) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase(column)) {
                return i;
            }
        }
        return -1;
    }

    private boolean checkCondition(String actualValue, String expectedValue, String operator) {
        switch (operator) {
            case "==":
                return actualValue.equalsIgnoreCase(expectedValue);
            case "!=":
                return !actualValue.equalsIgnoreCase(expectedValue);
            case "LIKE":
                return stringMatches(actualValue, expectedValue);
            case "<":
            case ">":
            case "<=":
            case ">=":
                try {
                    double actual = Double.parseDouble(actualValue);
                    double expected = Double.parseDouble(expectedValue);
                    return compareNumericValues(actual, expected, operator);
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    private boolean stringMatches(String value, String pattern) {
        String regex = ".*" + pattern.replace("%", ".*").replace("_", ".?") + ".*";
        return value.matches(regex);
    }

    private boolean compareNumericValues(double actual, double expected, String operator) {
        switch (operator) {
            case "<":
                return actual < expected;
            case ">":
                return actual > expected;
            case "<=":
                return actual <= expected;
            case ">=":
                return actual >= expected;
            default:
                return false;
        }
    }
}
