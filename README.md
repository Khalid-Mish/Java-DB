# Java Database:
This project folder contains a fully working relational database built from scratch in Java.

## How to use:
This project consists of a server and a client, both of which must be run in order to start the server and have the user connect to it. Both of these can be run from the command line.
### Connect to the server from the command line:
    ./mvnw exec:java@server
### Connect to the client from the command line:
    ./mvnw exec:java@client
  The server has persistent storage as created databases are automatically stored on the users file system in a folder called 'Databases'

## Grammar & Syntax:
The database works with a limited SQL syntax detailed below.

    <Command>         ::=  <CommandType> ";"

    <CommandType>     ::=  <Use> | <Create> | <Drop> | <Alter> | <Insert> | <Select> | <Update> | <Delete> | <Join>

    <Use>             ::=  "USE " [DatabaseName]

    <Create>          ::=  <CreateDatabase> | <CreateTable>

    <CreateDatabase>  ::=  "CREATE DATABASE " [DatabaseName]

    <CreateTable>     ::=  "CREATE TABLE " [TableName] | "CREATE TABLE " [TableName] "(" <AttributeList> ")"

    <Drop>            ::=  "DROP DATABASE " [DatabaseName] | "DROP TABLE " [TableName]

    <Alter>           ::=  "ALTER TABLE " [TableName] " " [AlterationType] " " [AttributeName]

    <Insert>          ::=  "INSERT INTO " [TableName] " VALUES(" <ValueList> ")"

    <Select>          ::=  "SELECT " <WildAttribList> " FROM " [TableName] | "SELECT " <WildAttribList> " FROM " [TableName] " WHERE " <Condition> 

    <Update>          ::=  "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition> 

    <Delete>          ::=  "DELETE FROM " [TableName] " WHERE " [Condition]

    <Join>            ::=  "JOIN " [TableName] " AND " [TableName] " ON " [AttributeName] " AND " [AttributeName]

    [Digit]           ::=  "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"

    [Uppercase]       ::=  "A" | "B" | "C" | "D" | "E" | "F" | "G" | "H" | "I" | "J" | "K" | "L" | "M" | "N" | "O" | "P" | "Q" | "R" | "S" | "T" | "U" | "V" | "W" | "X" | "Y" | "Z"

    [Lowercase]       ::=  "a" | "b" | "c" | "d" | "e" | "f" | "g" | "h" | "i" | "j" | "k" | "l" | "m" | "n" | "o" | "p" | "q" | "r" | "s" | "t" | "u" | "v" | "w" | "x" | "y" | "z"

    [Letter]          ::=  [Uppercase] | [Lowercase]

    [PlainText]       ::=  [Letter] | [Digit] | [PlainText] [Letter] | [PlainText] [Digit]

    [Symbol]          ::=  "!" | "#" | "$" | "%" | "&" | "(" | ")" | "*" | "+" | "," | "-" | "." | "/" | ":" | ";" | ">" | "=" | "<" | "?" | "@" | "[" | "\" | "]" | "^" | "_" | "`" | "{" | "}" | "~"

    [Space]           ::=  " "

    <NameValueList>   ::=  <NameValuePair> | <NameValuePair> "," <NameValueList>

    <NameValuePair>   ::=  [AttributeName] "=" [Value]

    [AlterationType]  ::=  "ADD" | "DROP"

    <ValueList>       ::=  [Value] | [Value] "," <ValueList>

    [DigitSequence]   ::=  [Digit] | [Digit] [DigitSequence]

    [IntegerLiteral]  ::=  [DigitSequence] | "-" [DigitSequence] | "+" [DigitSequence] 

    [FloatLiteral]    ::=  [DigitSequence] "." [DigitSequence] | "-" [DigitSequence] "." [DigitSequence] | "+" [DigitSequence] "." [DigitSequence]

    [BooleanLiteral]  ::=  "TRUE" | "FALSE"

    [CharLiteral]     ::=  [Space] | [Letter] | [Symbol] | [Digit]

    [StringLiteral]   ::=  "" | [CharLiteral] | [StringLiteral] [CharLiteral]

    [Value]           ::=  "'" [StringLiteral] "'" | [BooleanLiteral] | [FloatLiteral] | [IntegerLiteral] | "NULL"

    [TableName]       ::=  [PlainText]

    [AttributeName]   ::=  [PlainText] | [TableName] "." [PlainText]

    [DatabaseName]    ::=  [PlainText]

    <WildAttribList>  ::=  <AttributeList> | "*"

    <AttributeList>   ::=  [AttributeName] | [AttributeName] "," <AttributeList>

    <Condition>       ::=  "(" <Condition> [BoolOperator] <Condition> ")" | <Condition> [BoolOperator] <Condition> | "(" [AttributeName] [Comparator] [Value] ")" | [AttributeName] [Comparator] [Value]

    [BoolOperator]    ::= "AND" | "OR"

    [Comparator]      ::=  "==" | ">" | "<" | ">=" | "<=" | "!=" | " LIKE "


    Note:
    <name> denotes a rule which may contain arbitrary additional whitespace within the token, where as [name] indicates a rule that cannot contain additional whitespace    

## Transcript Example:
    SQL:> CREATE DATABASE markbook;
    [OK] Database created successfully.

    SQL:> USE markbook;
    [OK] Switched to database: markbook

    SQL:> CREATE TABLE marks (name, mark, pass);
    [OK] Table created successfully.
    
    SQL:> INSERT INTO marks VALUES ('Steve', 65, TRUE);
    [OK] Data inserted successfully.
    
    SQL:> INSERT INTO marks VALUES ('Dave', 55, TRUE);
    [OK] Data inserted successfully.
    
    SQL:> INSERT INTO marks VALUES ('Bob', 35, FALSE);
    [OK] Data inserted successfully.
    
    SQL:> INSERT INTO marks VALUES ('Clive', 20, FALSE);
    [OK] Data inserted successfully.
    
    SQL:> SELECT * FROM marks;
    [OK]
    id	name 	mark	pass 
    1 	Steve	65  	TRUE 
    2 	Dave 	55  	TRUE 
    3 	Bob  	35  	FALSE
    4 	Clive	20  	FALSE
    
    SQL:> SELECT * FROM marks WHERE name != 'Dave';
    [OK]
    id	name 	mark	pass 
    1 	Steve	65  	TRUE 
    3 	Bob  	35  	FALSE
    4 	Clive	20  	FALSE

    SQL:> SELECT * FROM marks WHERE pass == TRUE;
    [OK]
    id	name 	mark	pass
    1 	Steve	65  	TRUE
    2 	Dave 	55  	TRUE

    SQL:> UPDATE marks SET mark = 38 WHERE name == 'Clive';
    [OK] Table updated successfully.

    SQL:> SELECT * FROM marks WHERE name LIKE 've';
    [OK]
    id	name 	mark	pass 
    1 	Steve	65  	TRUE 
    2 	Dave 	55  	TRUE 
    4 	Clive	38  	FALSE

    SQL:> SELECT id FROM marks WHERE pass == FALSE;
    [OK]
    id
    3 
    4 

    SQL:> DELETE FROM marks WHERE mark<40;
    [OK] Rows deleted successfully.

    SQL:> SELECT * FROM marks;
    [OK]
    id	name	mark	pass
    1	Steve	65	TRUE

## Disclaimer
Please note, this work belongs to me and may not be used by students without my permission.
