package edu.uob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;


/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    private String currentDatabase;


    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }


    public String handleCommand(String command) {
        command = command.trim();
        if (!command.endsWith(";")) {
            return "[ERROR] Missing semi-colon in instruction.";
        }
        command = command.substring(0, command.length() - 1).trim();
        String[] tokens = command.trim().split("\\s+");
        String action = tokens[0].toUpperCase();

        switch (action) {
            case "CREATE":
                if (tokens.length < 3) {
                    return "[ERROR] Invalid CREATE command.";
                }
                if (tokens[1].equalsIgnoreCase("DATABASE") && tokens.length == 3) {
                    boolean success = new CreateQuery(storageFolderPath).createDatabase(tokens[2]);
                    return success ? "[OK] Database created successfully." : "[ERROR] Database already exists.";
                } else if (tokens[1].equalsIgnoreCase("TABLE")) {
                    if (this.currentDatabase != null && tokens.length > 3) {

                        StringBuilder columnSpecBuilder = new StringBuilder();
                        for (int i = 3; i < tokens.length; i++) {
                            columnSpecBuilder.append(tokens[i]);
                            if (i < tokens.length - 1) {
                                columnSpecBuilder.append(" ");
                            }
                        }
                        String columnSpec = columnSpecBuilder.toString().trim();
                        if (columnSpec.startsWith("(") && columnSpec.endsWith(")")) {
                            columnSpec = columnSpec.substring(1, columnSpec.length() - 1).trim();
                        }

                        String[] columns = columnSpec.split(",\\s*");
                        for (int i = 0; i < columns.length; i++) {
                            columns[i] = columns[i].trim();
                        }
                        boolean success = new CreateQuery(storageFolderPath).createTable(this.currentDatabase, tokens[2], columns);
                        return success ? "[OK] Table created successfully." : "[ERROR] Failed to create table.";
                    } else {
                        return "[ERROR] No database selected or invalid CREATE TABLE command.";
                    }
                } else {
                    return "[ERROR] Invalid CREATE command.";
                }

            case "USE":
                if (tokens.length == 2) {
                    String dbName = new UseQuery(storageFolderPath).switchDatabase(tokens[1]);
                    if (dbName != null) {
                        this.currentDatabase = dbName;
                        return "[OK] Switched to database: " + dbName;
                    } else {
                        return "[ERROR] Database " + tokens[1] + " does not exist.";
                    }
                } else {
                    return "[ERROR] Invalid USE command.";
                }

            case "INSERT":
                if (tokens.length < 3) {
                    return "[ERROR] Invalid INSERT command.";
                }
                if (tokens[1].equalsIgnoreCase("INTO")) {
                    String tableName = tokens[2];

                    if (!command.contains("VALUES")) {
                        return "[ERROR] INSERT statement missing VALUES clause.";
                    }

                    String valuePart = command.split("VALUES")[1].trim();

                    if (!valuePart.startsWith("(") || !valuePart.endsWith(")")) {
                        return "[ERROR] Invalid format for value data in INSERT statement.";
                    }

                    valuePart = valuePart.substring(1, valuePart.length() - 1);
                    String[] values = valuePart.split(",");

                    for (int i = 0; i < values.length; i++) {
                        values[i] = values[i].trim();
                        if (values[i].startsWith("'") && values[i].endsWith("'")) {
                            values[i] = values[i].substring(1, values[i].length() - 1);
                        }
                    }

                    boolean success = new InsertQuery(storageFolderPath).insertIntoTable(this.currentDatabase, tableName, values);
                    return success ? "[OK] Data inserted successfully." : "[ERROR] Failed to insert data.";
                } else {
                    return "[ERROR] Invalid INSERT command.";
                }
            case "SELECT":
                if (tokens.length > 3 && tokens[2].equalsIgnoreCase("FROM")) {
                    String[] columnNames = tokens[1].split(",");
                    String tableName = tokens[3];
                    String conditionColumn = null;
                    String conditionValue = null;
                    String operator = "=";

                    if (command.contains("WHERE")) {
                        String conditionPart = command.split("WHERE")[1].trim();
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\s*(==|!=|<>|LIKE|>|<)\\s*(\\w+|'[^']*')").matcher(conditionPart);
                        if (m.find()) {
                            conditionColumn = m.group(1);
                            operator = m.group(2);
                            conditionValue = m.group(3);

                            if (conditionValue.startsWith("'") && conditionValue.endsWith("'")) {
                                conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
                            }
                        } else {
                            return "[ERROR] Invalid WHERE clause.";
                        }
                    }

                    String result = new SelectQuery(storageFolderPath).selectFromTable(this.currentDatabase, tableName, columnNames, conditionColumn, conditionValue, operator);

                    if (result == null || result.contains("Table " + tableName + " does not exist")) {
                        return "[ERROR] " + "Table " + tableName + " does not exist in database " + this.currentDatabase;
                    }
                    if (result.startsWith("[ERROR]")) {
                        return result;
                    }

                    return "[OK]\n" + result;
                } else {
                    return "[ERROR] Invalid SELECT command.";
                }
            case"DELETE":
                if (tokens.length < 3) {
                    return "[ERROR] Invalid DELETE command.";
                }
                if (tokens[1].equalsIgnoreCase("FROM")) {
                    String tableName = tokens[2];

                    if (!command.contains("WHERE")) {
                        return "[ERROR] DELETE statement missing WHERE clause.";
                    }

                    String conditionPart = command.split("WHERE")[1].trim();
                    conditionPart = conditionPart.replace(";", "").trim(); // Remove the semicolon

                    String conditionColumn, operator, conditionValue;

                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\w+)\\s*([=<>!]+)\\s*(\\w+|'[^']*')").matcher(conditionPart);
                    if (m.find()) {
                        conditionColumn = m.group(1);
                        operator = m.group(2);
                        conditionValue = m.group(3);
                    } else {
                        return "[ERROR] Invalid WHERE clause.";
                    }
                    if (conditionValue.startsWith("'") && conditionValue.endsWith("'")) {
                        conditionValue = conditionValue.substring(1, conditionValue.length() - 1);
                    }
                    if (!Arrays.asList("==", "!=", "<", ">", "<=", ">=").contains(operator)) {
                        return "[ERROR] Unsupported operator in WHERE clause.";
                    }
                    boolean success = new DeleteQuery(storageFolderPath).deleteFromTable(this.currentDatabase, tableName, conditionColumn, conditionValue, operator);
                    return success ? "[OK] Rows deleted successfully." : "[ERROR] Failed to delete rows.";
                } else {
                    return "[ERROR] Invalid DELETE command.";
                }
            case "UPDATE":
                java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                        "UPDATE\\s+(\\w+)\\s+SET\\s+(\\w+)\\s*=\\s*(\\w+|'[^']*')\\s+WHERE\\s+(\\w+)\\s*==\\s*(\\w+|'[^']*')").matcher(command);
                if (m.find()) {
                    String tableName = m.group(1);
                    String setColumn = m.group(2);
                    String setValue = m.group(3).replace("'", "");
                    String whereColumn = m.group(4);
                    String whereValue = m.group(5).replace("'", "");

                    boolean success = new UpdateQuery(storageFolderPath).updateTable(this.currentDatabase, tableName, setColumn, setValue, whereColumn, whereValue);
                    return success ? "[OK] Table updated successfully." : "[ERROR] Failed to update table.";
                } else {
                    return "[ERROR] Invalid UPDATE command.";
                }
            case "DROP":
                java.util.regex.Matcher dbMatcher = java.util.regex.Pattern.compile("DROP\\s+DATABASE\\s+(\\w+)").matcher(command);
                java.util.regex.Matcher tableMatcher = java.util.regex.Pattern.compile("DROP\\s+TABLE\\s+(\\w+)").matcher(command);

                if (dbMatcher.find()) {
                    String dbName = dbMatcher.group(1);
                    String result = new DropQuery(storageFolderPath).dropDatabase(dbName);
                    return result;

                } else if (tableMatcher.find()) {
                    if (this.currentDatabase == null) {
                        return "[ERROR] No database selected. Please select a database first.";
                    }

                    String tableName = tableMatcher.group(1);
                    String result = new DropQuery(storageFolderPath).dropTable(this.currentDatabase, tableName);
                    return result;

                } else {
                    return "[ERROR] Invalid DROP command.";
                }
            case "ALTER":
                if (tokens.length < 5) { // Check if the tokens array has enough elements
                    return "[ERROR] Invalid ALTER command.";
                }
                if (tokens[1].equalsIgnoreCase("TABLE")) {
                    if (this.currentDatabase != null) {
                        boolean success = new AlterQuery(storageFolderPath).alterTable(this.currentDatabase, tokens[2], tokens[3], tokens[4]);
                        return success ? "[OK] Table altered successfully." : "[ERROR] Failed to alter table.";
                    } else {
                        return "[ERROR] No database selected.";
                    }
                } else {
                    return "[ERROR] Invalid ALTER command.";
                }
            case "JOIN":
               // String[] tableNames = tokens[1].split("AND");
               // String[] columnNames = tokens[3].split("AND");
             //   if (tableNames.length != 2 || columnNames.length != 2) {
               //     return "[ERROR] Invalid JOIN command.";
              //  }

              //  Table resultTable = new JoinQuery().join(table1, table2, columnIndex1, columnIndex2);


              //  if (resultTable == null) {
               //     return "[ERROR] Failed to join tables.";
             //   }
              //  return "[OK]\n" + resultTable.toString();
                return "[ERROR] Invalid JOIN command";
        }
        return "[ERROR] Unknown command.";
    }


    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
