package edu.uob;

import java.util.ArrayList;

public class JoinQuery {

    /**
     * Join two tables based on the provided column index.
     * @param table1 The first table.
     * @param table2 The second table.
     * @param joinColumnIndex1 The index of the column in the first table on which to join.
     * @param joinColumnIndex2 The index of the column in the second table on which to join.
     * @return A new table containing the results of the join operation.
     */
    public Table join(Table table1, Table table2, int joinColumnIndex1, int joinColumnIndex2) {
        String[] newColumns = concatenateColumns(table1, table2);

        Table resultTable = new Table("Result", newColumns);

        for (Row row1 : table1.getRows()) {
            for (Row row2 : table2.getRows()) {
                if (row1.getContent()[joinColumnIndex1].equals(row2.getContent()[joinColumnIndex2])) {
                    String[] combinedContent = concatenateRowContent(row1, row2);
                    resultTable.addRecord(new Row(combinedContent));
                }
            }
        }

        return resultTable;
    }

    /**
     * Concatenate the columns of two tables while avoiding ID columns.
     * @param table1 The first table.
     * @param table2 The second table.
     * @return An array of concatenated columns.
     */
    private String[] concatenateColumns(Table table1, Table table2) {
        String[] columns1 = table1.getColumns();
        String[] columns2 = table2.getColumns();

        int newLength = columns1.length + columns2.length - 2;  // -2 to avoid both ID columns
        String[] newColumns = new String[newLength + 1];  // +1 for the new ID column

        newColumns[0] = "ID";  // The first column is ID

        // Copy non-ID columns from table1 and table2
        System.arraycopy(columns1, 1, newColumns, 1, columns1.length - 1);
        System.arraycopy(columns2, 1, newColumns, columns1.length, columns2.length - 1);

        return newColumns;
    }

    /**
     * Concatenate the content of two rows while avoiding ID columns.
     * @param row1 The first row.
     * @param row2 The second row.
     * @return An array of concatenated content.
     */
    private String[] concatenateRowContent(Row row1, Row row2) {
        String[] content1 = row1.getContent();
        String[] content2 = row2.getContent();

        int newLength = content1.length + content2.length - 2;
        String[] newContent = new String[newLength];  // ID will be added by table class

        System.arraycopy(content1, 1, newContent, 0, content1.length - 1);
        System.arraycopy(content2, 1, newContent, content1.length - 1, content2.length - 1);

        return newContent;
    }
}
