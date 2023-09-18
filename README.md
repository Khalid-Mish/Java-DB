# Java Database:
This project folder contains a fully working relational database built from scratch in Java. The database works with a limited SQL syntax detailed below.

## How to use:
This project consists of a server and a client, both of which must be run in order to run the server and have the user connect to it. Both of these can be run from the command line.
### Connect to the server from the command line
    mvnw exec:java@server
### Connect to the client from the command line
    mvnw exec:java@client
  The server has persistent storage as created databases are automatically stored on the users file system in a folder called 'Databases'

