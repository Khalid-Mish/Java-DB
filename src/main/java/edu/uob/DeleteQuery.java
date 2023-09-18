package edu.uob;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DeleteQuery {
    private final Path storageFolderPath;

    public DeleteQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public boolean deleteFromTable(String databaseName, String tableName, String conditionColumn, String conditionValue, String operator) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (!tablePath.toFile().exists()) {
            return false; // Table does not exist
        }

        List<String> updatedLines = new ArrayList<>();
        String[] columns = null; // Declare the columns array outside the try scope

        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            String header = reader.readLine();
            columns = header.split("\t");
            updatedLines.add(header); // Add the header to the updated lines list

            int conditionColumnIndex = -1;
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].equals(conditionColumn)) {
                    conditionColumnIndex = i;
                    break;
                }
            }

            if (conditionColumnIndex == -1) {
                return false; // Specified column for condition not found
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] splitLine = line.split("\t");

                if (splitLine.length <= conditionColumnIndex) {
                    continue;
                }

                String actualValue = splitLine[conditionColumnIndex];
                boolean shouldDelete = false;

                actualValue = actualValue.trim();
                conditionValue = conditionValue.trim();

                switch (operator) {
                    case "==":
                        shouldDelete = actualValue.equals(conditionValue);
                        break;
                    case "!=":
                        shouldDelete = !actualValue.equals(conditionValue);
                        break;
                    case "<":
                        try {
                            shouldDelete = Double.parseDouble(actualValue) < Double.parseDouble(conditionValue);
                        } catch (NumberFormatException e) {
                            shouldDelete = false;
                        }
                        break;
                    case ">":
                        try {
                            shouldDelete = Double.parseDouble(actualValue) > Double.parseDouble(conditionValue);
                        } catch (NumberFormatException e) {
                            shouldDelete = false;
                        }
                        break;
                    case "<=":
                        try {
                            shouldDelete = Double.parseDouble(actualValue) <= Double.parseDouble(conditionValue);
                        } catch (NumberFormatException e) {
                            shouldDelete = false;
                        }
                        break;
                    case ">=":
                        try {
                            shouldDelete = Double.parseDouble(actualValue) >= Double.parseDouble(conditionValue);
                        } catch (NumberFormatException e) {
                            shouldDelete = false;
                        }
                        break;
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
}
