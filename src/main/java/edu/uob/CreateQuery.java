package edu.uob;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class CreateQuery {

    private final Path storageFolderPath;
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "ALTER", "CREATE", "DELETE", "DROP", "INSERT", "JOIN", "SELECT", "UPDATE",
            "USE", "TRUE", "FALSE", "AND", "OR", "LIKE", "ADD", "SET", "VALUES", "FROM", "ID");

    public CreateQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public boolean isReservedKeyword(String word) {
        return RESERVED_KEYWORDS.contains(word.toUpperCase());
    }

    public String createDatabase(String dbName) {
        if (isReservedKeyword(dbName)) {
            return "[ERROR] The database name '" + dbName + "' is a reserved keyword and cannot be used.";
        }
        dbName = dbName.toLowerCase();
        Path dbPath = storageFolderPath.resolve(dbName);
        File dbFolder = dbPath.toFile();

        if (dbFolder.exists()) {
            return "[ERROR] Database " + dbName + " already exists.";
        }

        if (dbFolder.mkdir()) {
            return "[OK] Database " + dbName + " created successfully.";
        } else {
            return "[ERROR] Failed to create database " + dbName + ".";
        }
    }

    public String createTable(String databaseName, String tableName, String[] columns) {
        if (isReservedKeyword(tableName)) {
            return "[ERROR] The table name '" + tableName + "' is a reserved keyword and cannot be used.";
        }

        tableName = tableName.toLowerCase();
        Path dbPath = storageFolderPath.resolve(databaseName);
        Path tablePath = dbPath.resolve(tableName + ".tab");

        if (tablePath.toFile().exists()) {
            return "[ERROR] Table " + tableName + " already exists in database " + databaseName + ".";
        }

        HashSet<String> uniqueColumnNames = new HashSet<>();

        // Check each column name for reserved keywords or duplicates
        if (columns != null) {
            for (String column : columns) {
                String trimmedColumn = column.trim();
                if (isReservedKeyword(trimmedColumn) || !uniqueColumnNames.add(trimmedColumn)) {
                    return "[ERROR] The column name '" + trimmedColumn + "' is either a reserved keyword or a duplicate and cannot be used.";
                }
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tablePath.toFile()))) {
            writer.write("id");
            if (columns != null) {
                for (String column : columns) {
                    writer.write("\t" + column.trim());
                }
            }
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "[ERROR] Failed to create table " + tableName + " in database " + databaseName + ".";
        }

        Path metaPath = dbPath.resolve(tableName + ".meta");
        try (BufferedWriter metaWriter = new BufferedWriter(new FileWriter(metaPath.toFile()))) {
            metaWriter.write("lastID:0");
            metaWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "[ERROR] Failed to create metadata for table " + tableName + " in database " + databaseName + ".";
        }

        return "[OK] Table " + tableName + " created successfully in database " + databaseName + ".";
    }
}
