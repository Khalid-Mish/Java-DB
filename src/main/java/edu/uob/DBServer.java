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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    private String storageFolderPath;
    private String currentDatabase;


    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */

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
                    String result = new CreateQuery(storageFolderPath).createDatabase(tokens[2]);
                    return result;
                } else if (tokens[1].equalsIgnoreCase("TABLE")) {
                    if (this.currentDatabase != null) {
                        String tableName = tokens[2].toLowerCase();

                        String[] columns = null;

                        if (command.contains("(") && command.contains(")")) {
                            String columnString = command.substring(command.indexOf('(') + 1, command.lastIndexOf(')'));
                            columns = columnString.split(",");
                            for (int i = 0; i < columns.length; i++) {
                                columns[i] = columns[i].trim();
                                if (columns[i].isEmpty() || new CreateQuery(storageFolderPath).isReservedKeyword(columns[i])) {
                                    return "[ERROR] The column name '" + columns[i] + "' is a reserved keyword and cannot be used.";
                                }
                            }
                        }
                        String result = new CreateQuery(storageFolderPath).createTable(this.currentDatabase, tableName, columns);
                        return result;
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
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing INSERT.";
                }
                if (tokens.length < 3) {
                    return "[ERROR] Invalid INSERT command.";
                }
                if (tokens[1].equalsIgnoreCase("INTO")) {
                    String tableName = tokens[2];

                    if (!command.toLowerCase().contains("values")) {
                        return "[ERROR] INSERT statement missing VALUES clause.";
                    }

                    String valuePart = command.split("(?i)VALUES")[1].trim();

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
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing SELECT.";
                }
                if (tokens.length > 3 && "FROM".equalsIgnoreCase(tokens[2])) {
                    String tableName = tokens[3];
                    String[] columnNames = tokens[1].split(",");
                    for (int i = 0; i < columnNames.length; i++) {
                        columnNames[i] = columnNames[i].trim();
                    }

                    List<String> conditionColumns = new ArrayList<>();
                    List<String> conditionValues = new ArrayList<>();
                    List<String> operators = new ArrayList<>();

                    if (command.toLowerCase().contains("where")) {
                        String conditionPart = command.split("(?i)where", 2)[1].trim();
                        if (conditionPart.isEmpty()) {
                            return "[ERROR] Missing WHERE condition.";
                        }

                        String[] conditions = conditionPart.split("(?i)and");
                        for (String condition : conditions) {
                            condition = condition.trim();
                            Matcher m = Pattern.compile("(\\w+)\\s*(==|!=|<>|LIKE|>=|<=|<|>)\\s*(\\w+|'[^']*')", Pattern.CASE_INSENSITIVE).matcher(condition);
                            if (m.find()) {
                                conditionColumns.add(m.group(1));
                                operators.add(m.group(2).toUpperCase());
                                conditionValues.add(m.group(3).startsWith("'") && m.group(3).endsWith("'") ? m.group(3).substring(1, m.group(3).length() - 1) : m.group(3));
                            } else {
                                return "[ERROR] Invalid WHERE clause.";
                            }
                        }
                    }

                    String result = new SelectQuery(storageFolderPath).selectFromTable(this.currentDatabase, tableName, columnNames, conditionColumns.toArray(new String[0]), conditionValues.toArray(new String[0]), operators.toArray(new String[0]));

                    if (result == null || result.contains("[ERROR]")) {
                        return result;
                    }

                    return "[OK]\n" + result;
                } else {
                    return "[ERROR] Invalid SELECT command.";
                }
            case "DELETE":
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing DELETE.";
                }
                if (tokens.length < 3) {
                    return "[ERROR] Invalid DELETE command.";
                }
                if (tokens[1].equalsIgnoreCase("FROM")) {
                    String tableName = tokens[2];

                    if (!command.toLowerCase().contains("where")) {
                        return "[ERROR] DELETE statement missing WHERE clause.";
                    }

                    String conditionPart = command.split("(?i)WHERE", 2)[1].trim();

                    List<String> conditionColumns = new ArrayList<>();
                    List<String> conditionValues = new ArrayList<>();
                    List<String> operators = new ArrayList<>();

                    String[] conditions = conditionPart.split("(?i)AND");
                    for (String condition : conditions) {
                        condition = condition.trim();
                        Matcher m = Pattern.compile("(\\w+)\\s*(==|!=|<>|LIKE|>=|<=|<|>)\\s*(\\w+|'[^']*')", Pattern.CASE_INSENSITIVE).matcher(condition);
                        if (m.find()) {
                            conditionColumns.add(m.group(1));
                            operators.add(m.group(2).toUpperCase());
                            conditionValues.add(m.group(3).startsWith("'") && m.group(3).endsWith("'") ? m.group(3).substring(1, m.group(3).length() - 1) : m.group(3));
                        } else {
                            return "[ERROR] Invalid WHERE clause.";
                        }
                    }

                    boolean success = new DeleteQuery(storageFolderPath).deleteFromTable(this.currentDatabase, tableName, conditionColumns.toArray(new String[0]), conditionValues.toArray(new String[0]), operators.toArray(new String[0]));
                    return success ? "[OK] Rows deleted successfully." : "[ERROR] Failed to delete rows.";
                } else {
                    return "[ERROR] Invalid DELETE command.";
                }
            case "UPDATE":
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing UPDATE.";
                }
                String lowerCaseCommand = command.toLowerCase();
                int setIndex = lowerCaseCommand.indexOf(" set ");
                int whereIndex = lowerCaseCommand.indexOf(" where ");
                if (setIndex == -1 || whereIndex == -1 || setIndex > whereIndex) {
                    return "[ERROR] Invalid UPDATE command.";
                }
                String updateTableName = command.substring(6, setIndex).trim();
                String setClause = command.substring(setIndex + 5, whereIndex).trim();
                String conditionPart = command.substring(whereIndex + 7).trim();

                ArrayList<String> setColumns = new ArrayList<>();
                ArrayList<String> setValues = new ArrayList<>();
                String[] setPairs = setClause.split(",");
                for (String pair : setPairs) {
                    String[] parts = pair.trim().split("=");
                    if (parts.length != 2) {
                        return "[ERROR] Invalid SET clause.";
                    }
                    setColumns.add(parts[0].trim());
                    String value = parts[1].trim().replaceFirst("^'(.*)'$", "$1");
                    setValues.add(value);
                }

                ArrayList<String> conditionColumns = new ArrayList<>();
                ArrayList<String> conditionValues = new ArrayList<>();
                ArrayList<String> operators = new ArrayList<>();

                String[] conditions = conditionPart.split("(?i)AND");
                for (String condition : conditions) {
                    condition = condition.trim();
                    Matcher m = Pattern.compile("(\\w+)\\s*(==|!=|<>|LIKE|>=|<=|<|>)\\s*('([^']*)'|\\w+)", Pattern.CASE_INSENSITIVE).matcher(condition);
                    if (m.find()) {
                        conditionColumns.add(m.group(1));
                        operators.add(m.group(2).toUpperCase());
                        conditionValues.add(m.group(3).startsWith("'") ? m.group(4) : m.group(3));
                    } else {
                        return "[ERROR] Invalid WHERE clause.";
                    }
                }

                String updateResult = new UpdateQuery(storageFolderPath).updateTable(this.currentDatabase, updateTableName, setColumns.toArray(new String[0]), setValues.toArray(new String[0]), conditionColumns.toArray(new String[0]), conditionValues.toArray(new String[0]), operators.toArray(new String[0]));
                return updateResult;

            case "DROP":
                String upperCommand = command.toUpperCase();

                Matcher dbMatcher = Pattern.compile("DROP\\s+DATABASE\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(upperCommand);
                Matcher tableMatcher = Pattern.compile("DROP\\s+TABLE\\s+(\\w+)", Pattern.CASE_INSENSITIVE).matcher(upperCommand);

                if (dbMatcher.find()) {
                    String dbName = command.substring(dbMatcher.start(1), dbMatcher.end(1));
                    String result = new DropQuery(storageFolderPath).dropDatabase(dbName);
                    return result;

                } else if (tableMatcher.find()) {
                    String tableName = command.substring(tableMatcher.start(1), tableMatcher.end(1));
                    String result = new DropQuery(storageFolderPath).dropTable(this.currentDatabase, tableName);
                    return result;
                } else {
                    return "[ERROR] Invalid DROP command.";
                }
            case "ALTER":
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing ALTER.";
                }
                if (tokens.length < 5) {
                    return "[ERROR] Invalid ALTER command.";
                }
                if (tokens[1].equalsIgnoreCase("TABLE")) {
                    String result = new AlterQuery(storageFolderPath).alterTable(this.currentDatabase, tokens[2], tokens[3], tokens[4]);
                    return result;
                } else {
                    return "[ERROR] Invalid ALTER command.";
                }
            case "JOIN":
                if (this.currentDatabase == null) {
                    return "[ERROR] No database selected. Please select a database using the USE command before executing JOIN.";
                }
                Pattern joinPattern = Pattern.compile("JOIN\\s+(\\w+)\\s+AND\\s+(\\w+)\\s+ON\\s+(\\w+)\\s+AND\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
                Matcher joinMatcher = joinPattern.matcher(command);
                if (joinMatcher.find()) {
                    String table1 = joinMatcher.group(1);
                    String table2 = joinMatcher.group(2);
                    String attribute1 = joinMatcher.group(3);
                    String attribute2 = joinMatcher.group(4);

                    JoinQuery joinQuery = new JoinQuery(storageFolderPath);
                    String result = joinQuery.performJoin(this.currentDatabase, table1, table2, attribute1, attribute2);
                    return result != null ? "[OK]\n" + result : "[ERROR] Failed to join tables.";
                } else {
                    return "[ERROR] Invalid JOIN command.";
                }

        }
        return "[ERROR] Unknown command.";
    }


    //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

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
