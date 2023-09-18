package edu.uob;

import java.io.*;
import java.nio.file.*;

public class InsertQuery {

    private final Path storageFolderPath;

    public InsertQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public boolean insertIntoTable(String databaseName, String tableName, String[] values) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (!tablePath.toFile().exists()) {
            System.err.println("Table " + tableName + " does not exist in database " + databaseName);
            return false;
        }

        String[] tableColumns;
        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            tableColumns = reader.readLine().split("\t");
        } catch (IOException e) {
            System.err.println("Failed to read table columns from file.");
            return false;
        }

        // Now check the length
        if ((tableColumns.length - 1) != values.length) {
            System.err.println("Mismatch between column count and value count.");
            return false;
        }

        String[] orderedValues = new String[tableColumns.length];

        // Handling the auto-generation of ID
        int newID = generateNextIDForTable(tablePath);
        orderedValues[0] = String.valueOf(newID);

        for (int i = 1; i < tableColumns.length; i++) {
            orderedValues[i] = values[i-1];
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tablePath.toFile(), true))) {
            for (String value : orderedValues) {
                writer.write(value);
                writer.write("\t");
            }
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private int generateNextIDForTable(Path tablePath) {
        int highestID = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(tablePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length > 0) {
                    try {
                        int currentID = Integer.parseInt(parts[0]);
                        if (currentID > highestID) {
                            highestID = currentID;
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid IDs
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return highestID + 1;
    }
}
