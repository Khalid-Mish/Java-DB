package edu.uob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JoinQuery {
    private final String storageFolderPath;

    public JoinQuery(String storageFolderPath) {
        this.storageFolderPath = storageFolderPath;
    }

    public String performJoin(String database, String table1Name, String table2Name, String attribute1, String attribute2) {
        Path table1Path = Paths.get(storageFolderPath, database, table1Name + ".tab");
        Path table2Path = Paths.get(storageFolderPath, database, table2Name + ".tab");

        try {
            List<String> table1Lines = Files.readAllLines(table1Path);
            List<String> table2Lines = Files.readAllLines(table2Path);

            String[] table1Headers = table1Lines.get(0).split("\t");
            String[] table2Headers = table2Lines.get(0).split("\t");

            int attribute1Index = findAttributeIndex(table1Headers, attribute1);
            int attribute2Index = findAttributeIndex(table2Headers, attribute2);

            if (attribute1Index == -1 || attribute2Index == -1) {
                return "[ERROR] Attributes for join not found in tables.";
            }

            List<String[]> allRows = new ArrayList<>();
            String[] newHeaders = prepareHeadersForJoin(table1Name, table1Headers, attribute1, table2Name, table2Headers, attribute2).split("\t");
            allRows.add(newHeaders);
            int[] maxLengths = new int[newHeaders.length];
            updateMaxLengths(Collections.singletonList(newHeaders), maxLengths);

            HashMap<String, List<String[]>> table2Map = createMapForJoin(table2Lines, attribute2Index);

            int newId = 1;
            for (String line1 : table1Lines.subList(1, table1Lines.size())) {
                String[] values1 = line1.split("\t");
                List<String[]> matchedRows = table2Map.get(values1[attribute1Index]);
                if (matchedRows != null) {
                    for (String[] values2 : matchedRows) {
                        String[] newRow = new String[newHeaders.length];
                        newRow[0] = String.valueOf(newId++);

                        System.arraycopy(values1, 1, newRow, 1, values1.length - 1);

                        int startPositionForValues2 = values1.length - 1;

                        System.arraycopy(values2, 1, newRow, startPositionForValues2, values2.length - 1);

                        allRows.add(newRow);
                    }
                }
            }


            updateMaxLengths(allRows, maxLengths);

            StringBuilder result = new StringBuilder();
            formatRowsForOutput(allRows, IntStream.range(0, newHeaders.length).boxed().collect(Collectors.toList()), maxLengths, result);

            return result.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "[ERROR] Failed to perform join.";
        }
    }

    private int findAttributeIndex(String[] headers, String attribute) {
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].equals(attribute)) {
                return i;
            }
        }
        return -1;
    }

    private String prepareHeadersForJoin(String tableName1, String[] headers1, String excludeAttribute1, String tableName2, String[] headers2, String excludeAttribute2) {
        StringBuilder headers = new StringBuilder("id\t");
        for (int i = 1; i < headers1.length; i++) {
            String header = headers1[i];
            if (!header.equals(excludeAttribute1)) {
                headers.append(tableName1).append(".").append(header).append("\t");
            }
        }
        for (String header : headers2) {
            if (!header.equals(excludeAttribute2)) {
                headers.append(tableName2).append(".").append(header).append("\t");
            }
        }
        return headers.toString().trim();
    }

    private HashMap<String, List<String[]>> createMapForJoin(List<String> lines, int attributeIndex) {
        HashMap<String, List<String[]>> map = new HashMap<>();
        for (String line : lines.subList(1, lines.size())) {
            String[] values = line.split("\t");
            map.computeIfAbsent(values[attributeIndex], k -> new ArrayList<>()).add(values);
        }
        return map;
    }

    private void updateMaxLengths(List<String[]> allRows, int[] maxLengths) {
        for (String[] row : allRows) {
            for (int i = 0; i < row.length; i++) {
                maxLengths[i] = Math.max(maxLengths[i], row[i] != null ? row[i].length() : 0);
            }
        }
    }

    private void formatRowsForOutput(List<String[]> allRows, List<Integer> desiredColumnIndices, int[] maxLengths, StringBuilder result) {
        for (String[] row : allRows) {
            for (int index : desiredColumnIndices) {
                String cellValue = index < row.length ? row[index] : "";
                result.append(String.format("%-" + maxLengths[index] + "s\t", cellValue));
            }
            result.setLength(result.length() - 1);
            result.append("\n");
        }
    }
}
