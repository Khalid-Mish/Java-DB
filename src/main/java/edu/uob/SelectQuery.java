package edu.uob;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SelectQuery {
    private final Path storageFolderPath;

    public SelectQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public String selectFromTable(String databaseName, String tableName, String[] desiredColumns, String conditionColumn, String conditionValue, String operator) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (!tablePath.toFile().exists()) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        List<String[]> allRows = new ArrayList<>();
        int[] maxLengths;

        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            String header = reader.readLine();
            allRows.add(header.split("\t"));
            maxLengths = new int[allRows.get(0).length];
            Arrays.fill(maxLengths, 0);

            int conditionColumnIndex = -1;
            for (int i = 0; i < allRows.get(0).length; i++) {
                if (allRows.get(0)[i].equals(conditionColumn)) {
                    conditionColumnIndex = i;
                    break;
                }
            }

            if (conditionColumnIndex == -1 && conditionColumn != null) {
                return "[ERROR] Column '" + conditionColumn + "' does not exist in table " + tableName;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\t");
                if (conditionColumnIndex == -1) {
                    allRows.add(splitLine);
                } else {
                    switch (operator) {
                        case "==":
                            if (splitLine[conditionColumnIndex].equals(conditionValue)) {
                                allRows.add(splitLine);
                            }
                            break;
                        case "!=":
                            if (!splitLine[conditionColumnIndex].equals(conditionValue)) {
                                allRows.add(splitLine);
                            }
                            break;
                        case "LIKE":
                            if (stringMatches(splitLine[conditionColumnIndex], conditionValue)) {
                                allRows.add(splitLine);
                            }
                            break;
                        case ">":
                            if (isNumeric(splitLine[conditionColumnIndex]) && isNumeric(conditionValue) &&
                                    Double.parseDouble(splitLine[conditionColumnIndex]) > Double.parseDouble(conditionValue)) {
                                allRows.add(splitLine);
                            }
                            break;
                        case "<":
                            if (isNumeric(splitLine[conditionColumnIndex]) && isNumeric(conditionValue) &&
                                    Double.parseDouble(splitLine[conditionColumnIndex]) < Double.parseDouble(conditionValue)) {
                                allRows.add(splitLine);
                            }
                            break;
                    }
                }
            }

            List<Integer> desiredColumnIndices = new ArrayList<>();
            if (desiredColumns.length == 1 && desiredColumns[0].equals("*")) {
                for (int i = 0; i < allRows.get(0).length; i++) {
                    desiredColumnIndices.add(i);
                }
            } else {
                for (String desiredColumn : desiredColumns) {
                    int index = Arrays.asList(allRows.get(0)).indexOf(desiredColumn);
                    if (index != -1) {
                        desiredColumnIndices.add(index);
                    }
                }
            }

            for (String[] row : allRows) {
                for (int i = 0; i < row.length; i++) {
                    maxLengths[i] = Math.max(maxLengths[i], row[i].length());
                }
            }

            for (String[] row : allRows) {
                for (int index : desiredColumnIndices) {
                    result.append(String.format("%-" + maxLengths[index] + "s", row[index]));
                    result.append("\t");
                }
                if (result.length() > 0) {
                    result.setLength(result.length() - 1); // Remove the last tab character
                }
                result.append("\n");
            }
        } catch (IOException e) {
            return "Failed to read from table file.";
        }

        return result.toString();
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
