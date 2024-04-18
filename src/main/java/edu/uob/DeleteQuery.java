package edu.uob;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DeleteQuery {
    private final Path storageFolderPath;

    public DeleteQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public boolean deleteFromTable(String databaseName, String tableName, String[] conditionColumns, String[] conditionValues, String[] operators) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (!tablePath.toFile().exists()) {
            return false;
        }

        List<String> updatedLines = new ArrayList<>();
        String[] columns;

        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            String header = reader.readLine();
            if (header == null) {
                return false;
            }
            columns = header.split("\t");
            updatedLines.add(header);

            List<Integer> conditionColumnIndexes = new ArrayList<>();
            for (String conditionColumn : conditionColumns) {
                int index = Arrays.asList(columns).indexOf(conditionColumn);
                if (index != -1) {
                    conditionColumnIndexes.add(index);
                } else {
                    return false;
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\t");
                boolean shouldDelete = true;

                for (int i = 0; i < conditionColumnIndexes.size(); i++) {
                    int columnIndex = conditionColumnIndexes.get(i);
                    if (columnIndex >= splitLine.length) {
                        shouldDelete = false;
                        break;
                    }
                    String actualValue = splitLine[columnIndex].trim();
                    String conditionValue = conditionValues[i].trim();
                    String operator = operators[i];

                    if (!checkCondition(actualValue, conditionValue, operator)) {
                        shouldDelete = false;
                        break;
                    }
                }

                if (!shouldDelete) {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tablePath.toFile()))) {
            for (String line : updatedLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean checkCondition(String actualValue, String conditionValue, String operator) {
        switch (operator) {
            case "==":
                return actualValue.equals(conditionValue);
            case "!=":
                return !actualValue.equals(conditionValue);
            case "<":
                return Double.compare(Double.parseDouble(actualValue), Double.parseDouble(conditionValue)) < 0;
            case ">":
                return Double.compare(Double.parseDouble(actualValue), Double.parseDouble(conditionValue)) > 0;
            case "<=":
                return Double.compare(Double.parseDouble(actualValue), Double.parseDouble(conditionValue)) <= 0;
            case ">=":
                return Double.compare(Double.parseDouble(actualValue), Double.parseDouble(conditionValue)) >= 0;
            case "LIKE":
                return stringMatches(actualValue, conditionValue);
            default:
                return false;
        }
    }

    private boolean stringMatches(String value, String pattern) {
        String regex = pattern.replace("%", ".*").replace("_", ".");
        return value.matches(regex);
    }
}
