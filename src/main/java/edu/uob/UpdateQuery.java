package edu.uob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class UpdateQuery {
    private String storageFolderPath;

    public UpdateQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public boolean updateTable(String database, String tableName, String setColumn, String setValue, String whereColumn, String whereValue) {
        Path tablePath = Paths.get(storageFolderPath, database, tableName + ".tab");
        if (!Files.exists(tablePath)) {
            return false;
        }

        try {
            List<String> lines = Files.readAllLines(tablePath);
            int colIndexToUpdate = -1;
            int colIndexToCheck = -1;
            String[] headers = lines.get(0).split("\t");
            for (int i = 0; i < headers.length; i++) {
                if (headers[i].equals(setColumn)) {
                    colIndexToUpdate = i;
                }
                if (headers[i].equals(whereColumn)) {
                    colIndexToCheck = i;
                }
            }

            if (colIndexToUpdate == -1 || colIndexToCheck == -1) {
                return false;
            }

            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split("\t");
                if (values[colIndexToCheck].trim().equals(whereValue.trim())) {
                    values[colIndexToUpdate] = setValue;
                    lines.set(i, String.join("\t", values));
                }
            }

            Files.write(tablePath, lines);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
