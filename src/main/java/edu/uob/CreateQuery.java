package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateQuery {

    private final Path storageFolderPath;

    public CreateQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public boolean createDatabase(String dbName) {
        Path dbPath = storageFolderPath.resolve(dbName);
        File dbFolder = dbPath.toFile();

        if (dbFolder.exists()) {
            System.out.println("Database " + dbName + " already exists.");
            return false;
        }

        if (dbFolder.mkdir()) {
            System.out.println("Database " + dbName + " created successfully.");
            return true;
        } else {
            System.out.println("Failed to create database " + dbName + ".");
            return false;
        }
    }

    public boolean createTable(String databaseName, String tableName, String[] columns) {
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (tablePath.toFile().exists()) {
            System.out.println("File " + tablePath.toString() + " already exists. Overwriting...");
        }

        String[] allColumns = new String[columns.length + 1];
        allColumns[0] = "id";
        System.arraycopy(columns, 0, allColumns, 1, columns.length);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tablePath.toFile()))) {
            for (int i = 0; i < allColumns.length; i++) {
                writer.write(allColumns[i]);
                if(i != allColumns.length - 1) {
                    writer.write("\t");
                }
            }
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


}