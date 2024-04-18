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

        if ((tableColumns.length - 1) != values.length) {
            System.err.println("Mismatch between column count and value count.");
            return false;
        }

        String[] orderedValues = new String[tableColumns.length];

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
        Path metaPath = tablePath.getParent().resolve(tablePath.getFileName().toString().replace(".tab", ".meta"));
        int lastID = 0;

        try (BufferedReader metaReader = new BufferedReader(new FileReader(metaPath.toFile()))) {
            String line = metaReader.readLine();
            if (line != null && line.startsWith("lastID:")) {
                lastID = Integer.parseInt(line.split(":")[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastID++;

        // Update the metadata file with the new lastID
        try (BufferedWriter metaWriter = new BufferedWriter(new FileWriter(metaPath.toFile()))) {
            metaWriter.write("lastID:" + lastID);
            metaWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastID;
    }
}
