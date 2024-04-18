package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for (int i = 0; i < 10; i++) randomName += (char) (97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> {
                    return server.handleCommand(command);
                },
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that they work !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Steve';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n", " ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length - 1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Steve';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "Steve was added to a table and the server restarted - but Steve was not returned by SELECT *");

    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");

    }

    @Test
    public void testSELECTAndINSERTCommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name != 'Dave';");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
        assertFalse(response.contains("Dave"), "A query was made to exclude Dave however he was still in the results");

        String response2 = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 've';");
        assertTrue(response2.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response2.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response2.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
        assertTrue(response2.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertFalse(response2.contains("Bob"), "A query was made to exclude Bob however he was still in the results");
    }

    @Test
    public void testDELETECommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        sendCommandToServer("DELETE FROM marks WHERE name == 'Dave';");
        String response = sendCommandToServer("SELECT * FROM marks;");
        //assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        //assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "Steve was not deleted but he is missing from the table");
        assertTrue(response.contains("Clive"), "Clive was not deleted but he is missing from the table");
        assertFalse(response.contains("Dave"), "A query was made to delete Dave's record, however he was still in the results");

    }

    @Test
    public void testUPDATECommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Clive';");

        assertTrue(response.contains("[OK]"), "Clive's mark was not successfully updated");

    }

    @Test
    public void testSELECTLIKECommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 've';");
        assertTrue(response.contains("[OK]"), "Query threw and error when it should have thrown an [OK]");
        assertFalse(response.contains("BOB"), "Bob was included in the results when he shouldn't have been");
    }

    @Test
    public void testALTERDROPCommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        sendCommandToServer("ALTER TABLE marks DROP pass");
        String response = ("SELECT * FROM marks");
        assertFalse(response.contains("pass"), "The pass column should have been deleted but was not");
    }

    @Test
    public void testALTERADDCommand() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("ALTER TABLE marks ADD number;");
        assertTrue(response.contains("[OK]"), "The number column should have been added but was not");
        assertFalse(response.contains("[ERROR]"), "An ERROR was thrown when a valid query was made");
    }

    @Test
    public void testWhitespaces() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE    TABLE    marks (name, mark, pass);");
        sendCommandToServer("INSERT   INTO    marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO       marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT     INTO      marks      VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT          INTO     marks        VALUES ('Clive', 20, FALSE);");

        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "The number column should have been added but was not");
        assertFalse(response.contains("[ERROR]"), "An ERROR was thrown when it shouldn't have been");
        assertTrue(response.contains("Steve"), "Steve was not added to the table");
        assertTrue(response.contains("Dave"), "Dave was not added to the table");
        assertTrue(response.contains("Bob"), "Bob was not added to the table");
        assertTrue(response.contains("Clive"), "Clive was not added to the table");
    }

    @Test
    public void testValidColumnSelection() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE users (name, age);");
        sendCommandToServer("INSERT INTO users VALUES ('Bob', 30);");
        String response = sendCommandToServer("SELECT name FROM users;");
        assertTrue(response.contains("[OK]"), "Valid column selection failed");
        assertTrue(response.contains("Bob"), "Bob was not added or fetched correctly");
        assertFalse(response.contains("30"), "Age should not be fetched");
    }

    @Test
    public void testDuplicateDatabaseCreation() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        String response = sendCommandToServer("CREATE DATABASE " + dbName + ";");
        assertFalse(response.contains("[OK]"), "Databases with the same name should not be allowed");
        assertTrue(response.contains("[ERROR]"), "Expected error when creating a duplicate database");
    }

    @Test
    public void testNonExistentTable() {
        String dbName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName + ";");
        String response = sendCommandToServer("SELECT * FROM nonexistent_table;");
        assertFalse(response.contains("[OK]"), "Table should not exist yet");
        assertTrue(response.contains("[ERROR]"), "Expected error when querying a non-existing table");
    }

    @Test
    public void testNonexistentDatabase() {
        String dbName = generateRandomName();
        String response = sendCommandToServer("USE " + dbName + ";");
        assertFalse(response.contains("[OK]"), "Database should not exist yet");
        assertTrue(response.contains("[ERROR]"), "Expected error when switching to a non-existing database");
    }

    @Test
    public void testMultipleDatabases() {
        String dbName1 = generateRandomName();
        String dbName2 = generateRandomName();

        sendCommandToServer("CREATE DATABASE " + dbName1 + ";");
        sendCommandToServer("CREATE DATABASE " + dbName2 + ";");
        sendCommandToServer("USE " + dbName1 + ";");
        sendCommandToServer("CREATE TABLE users (name, age);");
        sendCommandToServer("INSERT INTO users VALUES ('Alice', 25);");

        sendCommandToServer("USE " + dbName2 + ";");
        sendCommandToServer("CREATE TABLE items (name, price);");
        sendCommandToServer("INSERT INTO items VALUES ('Book', 15.5);");

        sendCommandToServer("USE " + dbName1 + ";");
        String response1 = sendCommandToServer("SELECT * FROM users;");
        assertTrue(response1.contains("[OK]"), "Failed to query from first database");
        assertTrue(response1.contains("Alice"), "Alice was not added to the first database");

        sendCommandToServer("USE " + dbName2 + ";");
        String response2 = sendCommandToServer("SELECT * FROM items;");
        assertTrue(response2.contains("[OK]"), "Failed to query from second database");
        assertTrue(response2.contains("Book"), "Book was not added to the second database");

        sendCommandToServer("USE " + dbName1 + ";");
        sendCommandToServer("DROP TABLE users;");
        sendCommandToServer("DROP DATABASE " + dbName1 + ";");
        sendCommandToServer("USE " + dbName2 + ";");
        sendCommandToServer("DROP TABLE items;");
        sendCommandToServer("DROP DATABASE " + dbName2 + ";");
    }

    @Test
    public void testDatabaseCaseInsensitivity() {
        String dbName = generateRandomName().toUpperCase();

        sendCommandToServer("create DATABASE " + dbName + ";");
        sendCommandToServer("USE " + dbName.toLowerCase() + ";");
        sendCommandToServer("create table USERS (name, age);");
        sendCommandToServer("INSERT INTO users VALUES ('Charlie', 35);");

        String response = sendCommandToServer("SELECT * from USERS;");

        assertTrue(response.contains("[OK]"), "Expected successful operation");
        assertTrue(response.contains("Charlie"), "Charlie was not added or fetched correctly");
    }

    @Test
    public void testSELECTBeforeUSE() {
        String response = sendCommandToServer("SELECT * from marks;");
        assertTrue(response.contains("[ERROR]"), "The user should not be able to use SELECT without opening a database first");
    }

    @Test
    public void testDROPTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("DROP TABLE marks;");
        assertTrue(response.contains("[OK]"), "The marks table was not successfully deleted");
        String response2 = sendCommandToServer("SELECT * FROM marks");
        assertTrue(response2.contains("[ERROR]"),"The marks table should not exist");
    }

    @Test
    public void testDROPDatabase() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("DROP DATABASE " + randomName + ";");
        String response = sendCommandToServer(("USE " + randomName));
        assertTrue(response.contains("[ERROR]"),"The database was not successfully dropped");
    }

    @Test
    public void testSELECTComparison() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE name > 'Steve';");
        assertFalse(response.contains("Steve"), "Comparing a string with > should not be valid");
    }

    @Test
    public void testInvalidWHERECondition() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE;");
        assertTrue(response.contains("[ERROR]"), "The WHERE condition specified by the user was invalid");
    }

    @Test
    public void testInvalidSELECT() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT date FROM marks;");
        assertTrue(response.contains("[ERROR]"), "The table date should not exist");
    }

    @Test
    public void testALTERADDKeyword() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("ALTER TABLE marks ADD TRUE;");
        assertTrue(response.contains("[ERROR]"), "TRUE is a keyword and should not be used to create a new attribute");
    }

    @Test
    public void testCREATEKeywordDatabase() {
        String response = sendCommandToServer("CREATE DATABASE TRUE;");
        assertTrue(response.contains("[ERROR]"), "You should not be able to create a database named with a keyword");
    }

    @Test
    public void testCREATEKeywordTables() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (TRUE,FALSE,LIKE);");
        assertTrue(response.contains("[ERROR]"), "You should not be able to create attributes named after keywords");
    }

    @Test
    public void testALTERDROPIDError() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("ALTER TABLE marks DROP id;");
        assertTrue(response.contains("[ERROR]"), "The ID attribute cannot be dropped");
        String response2 = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response2.contains("id"), "The id attribute should not have been deleted");
    }

    @Test
    public void testUPDATEIDError() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("UPDATE marks SET id = 5 WHERE name == 'Steve';");
        assertTrue(response.contains("[ERROR]"), "The ID attribute cannot be updated;");
    }

    @Test
    public void testCreateDuplicateColumns() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE marks (mark, mark, mark);");
        assertTrue(response.contains("[ERROR]"), "The user cannot create duplicate columns");
    }

    @Test
    public void testIDRecycling() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("DELETE FROM marks WHERE name == Dave;");
        sendCommandToServer("INSERT INTO marks VALUES (Clive, 70, TRUE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("3"), "Clive's ID should be 3");
        assertFalse(response.contains("2"),"The second ID should have been deleted and not reused");
    }

    @Test
    public void testSelectANDOperator() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks WHERE mark > 21 AND pass == FALSE;");
        assertTrue(response.contains("[OK]"), "This command should not contain an error");
        assertTrue(response.contains("Bob"), "Bob's value should not be missing");
        assertFalse(response.contains("Dave"), "Dave should not be included in the new selected values of this command");
    }
}