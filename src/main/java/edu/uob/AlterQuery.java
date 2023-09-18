package edu.uob;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class AlterQuery {
    private final String storageFolderPath;

    public AlterQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public boolean alterTable(String database, String tableName, String alterationType, String attributeName) {
        Path tablePath = Paths.get(storageFolderPath, database, tableName + ".tab");
        if (!Files.exists(tablePath)) {
            System.out.println("[ERROR] Table does not exist.");
            return false;
        }
        try {
            List<String> lines = Files.readAllLines(tablePath);
            if (lines.size() < 1) {
                System.out.println("[ERROR] Invalid table file format.");
                return false;
            }

            String[] columns = lines.get(0).split("\t");
            ArrayList<String> newColumnsList = new ArrayList<>(List.of(columns));

            if ("ADD".equalsIgnoreCase(alterationType)) {
                if (newColumnsList.contains(attributeName)) {
                    System.out.println("[ERROR] Column already exists.");
                    return false;
                }
                newColumnsList.add(attributeName);
                lines.set(0, String.join("\t", newColumnsList));

                for (int i = 1; i < lines.size(); i++) {
                    lines.set(i, lines.get(i) + "\t");
                }

            } else if ("DROP".equalsIgnoreCase(alterationType)) {
                int columnIndex = newColumnsList.indexOf(attributeName);

                if (columnIndex == -1) {
                    System.out.println("[ERROR] Column does not exist.");
                    return false;
                }

                newColumnsList.remove(columnIndex);
                lines.set(0, String.join("\t", newColumnsList));

                for (int i = 1; i < lines.size(); i++) {
                    String[] rowValues = lines.get(i).split("\t");
                    ArrayList<String> newRowList = new ArrayList<>(List.of(rowValues));
                    newRowList.remove(columnIndex);
                    lines.set(i, String.join("\t", newRowList));
                }
            } else {
                System.out.println("[ERROR] Invalid alteration type.");
                return false;
            }

            Files.write(tablePath, lines);

            return true;

        } catch (IOException ex) {
            System.out.println("[ERROR] An error occurred while altering table: " + ex.getMessage());
            return false;
        }
    }
}
