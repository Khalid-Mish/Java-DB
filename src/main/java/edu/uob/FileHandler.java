package edu.uob;

import java.io.*;

public class FileHandler {

    public static Table readInFile(String path, String tableName) {
        Table table = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            // Reads in the first line and creates the table with the column names
            String line = reader.readLine();
            String[] data = line.replace("\n", "").split("\t");
            table = new Table(tableName, data);

            // Reads in the content of the table
            while((line = reader.readLine()) != null) {
                data = line.replace("\n", "").split("\t");
                table.addRecord(new Row(data));
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return table;
    }
}
