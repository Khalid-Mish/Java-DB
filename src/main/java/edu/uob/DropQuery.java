package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;

public class DropQuery {

    private String storageFolderPath;

    public DropQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public String dropDatabase(String dbName) {
        File databaseDirectory = new File(storageFolderPath + File.separator + dbName);
        if (databaseDirectory.exists()) {
            try {
                Files.deleteIfExists(databaseDirectory.toPath());
                return "[OK] Database deleted successfully.";
            } catch (DirectoryNotEmptyException e) {
                return "[ERROR] Database directory is not empty.";
            } catch (IOException e) {
                e.printStackTrace();
                return "[ERROR] Failed to delete the database.";
            }
        } else {
            return "[ERROR] Database does not exist.";
        }
    }

    public String dropTable(String dbName, String tableName) {
        File tableFile = new File(storageFolderPath + File.separator + dbName + File.separator + tableName + ".tab");
        if (tableFile.exists()) {
            try {
                Files.deleteIfExists(tableFile.toPath());
                return "[OK] Table deleted successfully.";
            } catch (IOException e) {
                e.printStackTrace();
                return "[ERROR] Failed to delete the table.";
            }
        } else {
            return "[ERROR] Table does not exist.";
        }
    }
}
