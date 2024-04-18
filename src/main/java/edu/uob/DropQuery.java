package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class DropQuery {

    private String storageFolderPath;

    public DropQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public String dropDatabase(String dbName) {
        File storageFolder = new File(storageFolderPath);
        File[] files = storageFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().equalsIgnoreCase(dbName)) {
                    try {
                        deleteDirectoryRecursively(file.toPath());
                        return "[OK] Database deleted successfully.";
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "[ERROR] Failed to delete the database.";
                    }
                }
            }
        }
        return "[ERROR] Database does not exist.";
    }

    public String dropTable(String dbName, String tableName) {
        File databaseDirectory = new File(storageFolderPath + File.separator + dbName);
        File[] files = databaseDirectory.listFiles();
        boolean tableDeleted = false;
        boolean metaDeleted = false;

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equalsIgnoreCase(tableName + ".tab")) {
                    try {
                        Files.deleteIfExists(file.toPath());
                        tableDeleted = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "[ERROR] Failed to delete the table.";
                    }
                } else if (file.isFile() && file.getName().equalsIgnoreCase(tableName + ".meta")) {
                    try {
                        Files.deleteIfExists(file.toPath());
                        metaDeleted = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (tableDeleted) {
            return metaDeleted ? "[OK] Table and metadata deleted successfully." : "[OK] Table deleted successfully, but metadata file was not found.";
        } else {
            return "[ERROR] Table does not exist.";
        }
    }

    private void deleteDirectoryRecursively(Path path) throws IOException {
        Files.walk(path)
                .sorted(java.util.Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}