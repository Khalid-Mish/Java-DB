package edu.uob;

import java.util.ArrayList;

public class Table {
    private final String name;
    private final String[] columns;
    private final ArrayList<Row> rows;
    private int currentId;  // counter for ID auto-increment

    public String[] getColumns() {
        return columns;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public Table(String name, String[] columns) {
        this.name = name;
        this.columns = columns;
        this.rows = new ArrayList<>();
        this.currentId = 1;  // initialize with 1
    }

    public void addRecord(Row row) {
        if (row.getContent().length != columns.length - 1) {  // -1 to account for the id column
            System.err.println(row + " couldn't be added to the table (Column sizes don't fit)");
            return;
        }

        for (Row value : rows) {
            if (value.getContent()[0].equals(row.getContent()[0])) {
                System.err.println(row + " couldn't be added to the table (Primary key already in table)");
                return;
            }
        }

        // Assign ID and add to the row
        String[] finalContent = new String[row.getContent().length + 1];
        finalContent[0] = String.valueOf(currentId++);
        System.arraycopy(row.getContent(), 0, finalContent, 1, row.getContent().length);
        rows.add(new Row(finalContent));

        // TODO: Add datatype check
    }

    @Override
    public String toString() {
        StringBuilder sB = new StringBuilder();
        sB.append(String.join("\t|\t", columns)).append("\n");

        for (Row row : rows) {
            sB.append(row).append("\n");
        }

        return sB.toString();
    }
}
