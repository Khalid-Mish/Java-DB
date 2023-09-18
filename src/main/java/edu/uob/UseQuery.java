package edu.uob;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UseQuery {
    private final Path storageFolderPath;

    public UseQuery(String storageFolderPath) {
        this.storageFolderPath = Paths.get(storageFolderPath);
    }

    public String switchDatabase(String databaseName) {
        Path databasePath = storageFolderPath.resolve(databaseName);
        if (databasePath.toFile().exists()) {
            return databaseName;  // If the database exists, return its name to set as currentDatabase in DBServer
        } else {
            return null;  // Else, return null indicating the database doesn't exist
        }
    }
}
