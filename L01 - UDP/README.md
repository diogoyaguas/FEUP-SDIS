# L01 - UDP

## Server

Open a Terminal by pressing **CTRL+ALT+T**, and run the server at a chosen port:

**Usage:** java server \<port\>

**Example:** ```java server 8080```

Press **CTRL+C** to stop the server.


## Client

Open a Terminal and send a request to the server:

### Register a plate

**Usage:** java l01.Client \<serverIP\> \<port\> register \<plate\> \<owner\>

**Example:** ```java client localhost 8080 register 20-02-SD Diogo```

To which the server will respond:
- Size of the database -> if the plate was added to the database;
- -1 -> if the plate already exists in the database.

### Lookup a plate

**Usage:** java l01.Client \<serverIP\> \<port\> lookup \<plate\>

**Example:** ```java client localhost 8080 lookup 20-02-SD```

To which the server will respond:
- Name of plate owner -> if the plate was found;
- NOT_FOUND -> if the plate was not found.
