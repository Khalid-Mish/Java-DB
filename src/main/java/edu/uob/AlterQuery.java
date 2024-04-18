package edu.uob;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class AlterQuery {
    private final String storageFolderPath;
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "ALTER", "CREATE", "DELETE", "DROP", "INSERT", "JOIN", "SELECT",
            "UPDATE", "USE", "TRUE", "FALSE", "AND", "OR", "LIKE", "ADD",
            "SET", "VALUES", "FROM", "ID");

    public AlterQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    private boolean isReservedKeyword(String word) {
        return RESERVED_KEYWORDS.contains(word.toUpperCase());
    }

    public String alterTable(String database, String tableName, String alterationType, String attributeName) {
        if (isReservedKeyword(attributeName)) {
            return "[ERROR] The attribute name '" + attributeName + "' is a reserved keyword and cannot be altered.";
        }

        if ("id".equalsIgnoreCase(attributeName)) {
            return "[ERROR] The 'id' attribute cannot be altered.";
        }

        Path tablePath = Paths.get(storageFolderPath, database, tableName + ".tab");
        if (!Files.exists(tablePath)) {
            return "[ERROR] Table does not exist.";
        }
        try {
            List<String> lines = Files.readAllLines(tablePath);
            if (lines.size() < 1) {
                return "[ERROR] Invalid table file format.";
            }

            String[] columns = lines.get(0).split("\t");
            ArrayList<String> newColumnsList = new ArrayList<>(List.of(columns));

            if ("ADD".equalsIgnoreCase(alterationType)) {
                if (newColumnsList.contains(attributeName)) {
                    return "[ERROR] Column already exists.";
                }
                newColumnsList.add(attributeName);
                lines.set(0, String.join("\t", newColumnsList));

                for (int i = 1; i < lines.size(); i++) {
                    lines.set(i, lines.get(i) + "\t" + "");
                }

            } else if ("DROP".equalsIgnoreCase(alterationType)) {
                int columnIndex = newColumnsList.indexOf(attributeName);

                if (columnIndex == -1) {
                    return "[ERROR] Column does not exist.";
                }

                // Since the first column is always 'id'
                if (columnIndex == 0) {
                    return "[ERROR] The 'id' column cannot be dropped.";
                }

                newColumnsList.remove(columnIndex);
                lines.set(0, String.join("\t", newColumnsList));

                for (int i = 1; i < lines.size(); i++) {
                    String[] rowValues = lines.get(i).split("\t", -1);
                    ArrayList<String> newRowList = new ArrayList<>(List.of(rowValues));
                    newRowList.remove(columnIndex);
                    lines.set(i, String.join("\t", newRowList));
                }
            } else {
                return "[ERROR] Invalid alteration type.";
            }

            Files.write(tablePath, lines);
            return "[OK] Table altered successfully.";

        } catch (IOException ex) {
            return "[ERROR] An error occurred while altering table: " + ex.getMessage();
        }
    }
}