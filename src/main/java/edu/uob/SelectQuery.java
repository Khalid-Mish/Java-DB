package edu.uob;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SelectQuery {
    private final Path storageFolderPath;

    public SelectQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public String selectFromTable(String databaseName, String tableName, String[] desiredColumns, String[] conditionColumns, String[] conditionValues, String[] operators) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (!tablePath.toFile().exists()) {
            return "[ERROR] Table " + tableName + " does not exist.";
        }

        StringBuilder result = new StringBuilder();
        List<String[]> allRows = new ArrayList<>();
        int[] maxLengths;

        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            String header = reader.readLine();
            if (header == null) {
                return "[ERROR] Table is empty.";
            }
            String[] headerColumns = header.split("\t");
            allRows.add(headerColumns);
            maxLengths = new int[headerColumns.length];

            List<Integer> conditionColumnIndexes = new ArrayList<>();
            for (String conditionColumn : conditionColumns) {
                int index = Arrays.asList(headerColumns).indexOf(conditionColumn);
                if (index == -1) {
                    return "[ERROR] Column '" + conditionColumn + "' does not exist in table " + tableName + ".";
                }
                conditionColumnIndexes.add(index);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\t");
                if (checkConditions(splitLine, conditionColumnIndexes, conditionValues, operators)) {
                    allRows.add(splitLine);
                }
            }

            updateMaxLengths(allRows, maxLengths);

            List<Integer> desiredColumnIndices;
            try {
                desiredColumnIndices = getDesiredColumnIndices(headerColumns, desiredColumns);
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }

            formatRowsForOutput(allRows, desiredColumnIndices, maxLengths, result);

        } catch (IOException e) {
            return "Failed to read from table file.";
        }

        return result.toString().trim();
    }

    private boolean checkConditions(String[] row, List<Integer> conditionColumnIndexes, String[] conditionValues, String[] operators) {
        for (int i = 0; i < conditionColumnIndexes.size(); i++) {
            if (!checkCondition(row, conditionColumnIndexes.get(i), conditionValues[i], operators[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCondition(String[] row, int conditionColumnIndex, String conditionValue, String operator) {
        boolean matchesCondition = false;
        String cellValue = row[conditionColumnIndex];
        switch (operator) {
            case "==":
                matchesCondition = cellValue.equalsIgnoreCase(conditionValue);
                break;
            case "!=":
                matchesCondition = !cellValue.equalsIgnoreCase(conditionValue);
                break;
            case "LIKE":
                matchesCondition = stringMatches(cellValue, conditionValue);
                break;
            case ">":
            case "<":
            case ">=":
            case "<=":
                if (isNumeric(cellValue) && isNumeric(conditionValue)) {
                    double rowValue = Double.parseDouble(cellValue);
                    double conditionDoubleValue = Double.parseDouble(conditionValue);
                    matchesCondition = compareValues(rowValue, conditionDoubleValue, operator);
                }
                break;
        }
        return matchesCondition;
    }

    private boolean compareValues(double rowValue, double conditionValue, String operator) {
        switch (operator) {
            case ">":
                return rowValue > conditionValue;
            case "<":
                return rowValue < conditionValue;
            case ">=":
                return rowValue >= conditionValue;
            case "<=":
                return rowValue <= conditionValue;
            default:
                return false;
        }
    }

    private void updateMaxLengths(List<String[]> allRows, int[] maxLengths) {
        for (String[] row : allRows) {
            for (int i = 0; i < row.length; i++) {
                maxLengths[i] = Math.max(maxLengths[i], row[i] != null ? row[i].length() : 0);
            }
        }
    }

    private List<Integer> getDesiredColumnIndices(String[] headerColumns, String[] desiredColumns) throws IllegalArgumentException {
        List<Integer> indices = new ArrayList<>();
        List<String> headerList = Arrays.asList(headerColumns).stream().map(String::toLowerCase).collect(Collectors.toList());
        if (desiredColumns.length == 1 && "*".equals(desiredColumns[0])) {
            return IntStream.range(0, headerColumns.length).boxed().collect(Collectors.toList());
        } else {
            for (String column : desiredColumns) {
                int index = headerList.indexOf(column.toLowerCase());
                if (index != -1) {
                    indices.add(index);
                } else {
                    throw new IllegalArgumentException("[ERROR] Column '" + column + "' does not exist in the table.");
                }
            }
        }
        return indices;
    }

    private void formatRowsForOutput(List<String[]> allRows, List<Integer> desiredColumnIndices, int[] maxLengths, StringBuilder result) {
        for (String[] row : allRows) {
            for (int index : desiredColumnIndices) {
                String cellValue = index < row.length ? row[index] : "";
                result.append(String.format("%-" + maxLengths[index] + "s\t", cellValue));
            }
            result.setLength(result.length() - 1);
            result.append("\n");
        }
    }

    private boolean stringMatches(String value, String pattern) {
        String regex = ".*" + pattern.replace("%", ".*").replace("_", ".?") + ".*";
        return value.matches(regex);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
