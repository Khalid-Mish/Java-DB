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
    CREATE DATABASE markbook;
    [OK]

    USE markbook;
    [OK]

    CREATE TABLE marks (name, mark, pass);
    [OK]

    INSERT INTO marks VALUES ('Simon', 65, TRUE);
    [OK]

    INSERT INTO marks VALUES ('Sion', 55, TRUE);
    [OK]

    INSERT INTO marks VALUES ('Rob', 35, FALSE);
    [OK]

    INSERT INTO marks VALUES ('Chris', 20, FALSE);
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE
    2	Sion	55	TRUE
    3	Rob	35	FALSE
    4	Chris	20	FALSE

    SELECT * FROM marks WHERE name != 'Sion';
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE
    3	Rob	35	FALSE
    4	Chris	20	FALSE

    SELECT * FROM marks WHERE pass == TRUE;
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE
    2	Sion	55	TRUE


    SELECT * FROM coursework;
    [OK]
    id	task	submission
    1	OXO	3
    2	DB	1
    3	OXO	4
    4	STAG	2

    JOIN coursework AND marks ON submission AND id;
    [OK]
    id	coursework.task	marks.name	marks.mark	marks.pass
    1	OXO			Rob		35		FALSE
    2	DB			Simon		65		TRUE
    3	OXO			Chris		20		FALSE
    4	STAG			Sion		55		TRUE

    UPDATE marks SET mark = 38 WHERE name == 'Chris';
    [OK]

    SELECT * FROM marks WHERE name == 'Chris';
    [OK]
    id	name	mark	pass
    4	Chris	38	FALSE

    DELETE FROM marks WHERE name == 'Sion';
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE
    3	Rob	35	FALSE
    4	Chris	38	FALSE

    SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);
    [OK]
    id	name	mark	pass
    4	Chris	38	FALSE

    SELECT * FROM marks WHERE name LIKE 'i';
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE
    4	Chris	38	FALSE

    SELECT id FROM marks WHERE pass == FALSE;
    [OK]
    id
    3
    4

    SELECT name FROM marks WHERE mark>60;
    [OK]
    name
    Simon


    DELETE FROM marks WHERE mark<40;
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	pass
    1	Simon	65	TRUE

    ALTER TABLE marks ADD age;
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	pass	age
    1	Simon	65	TRUE	

    UPDATE marks SET age = 35 WHERE name == 'Simon';
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	pass	age
    1	Simon	65	TRUE	35

    ALTER TABLE marks DROP pass;
    [OK]

    SELECT * FROM marks;
    [OK]
    id	name	mark	age
    1	Simon	65	35

    SELECT * FROM marks
    [ERROR]: Semi colon missing at end of line

    SELECT * FROM crew;
    [ERROR]: Table does not exist

    SELECT height FROM marks WHERE name == 'Chris';
    [ERROR]: Attribute does not exist (or similar message !)

    DROP TABLE marks;
    [OK]

    DROP DATABASE markbook;
    [OK]


## Disclaimer
Please note, this work belongs to me and may not be used by students without my permission.
