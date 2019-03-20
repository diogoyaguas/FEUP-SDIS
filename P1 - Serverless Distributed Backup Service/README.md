# L03

## Server

Open a Terminal.

Start the RMI registry: ```start rmiregistry```

**Usage:** java Server \<remote_object_name\>

**Example:** ```java server 8080```

Stop the server.


## Client

**Usage:** java Client \<host_name\> \<remote_object_name\> \<oper\> \<opnd\>*

Open a Terminal and send a request to the server:

### Register a plate

**Usage:** java Client \<host_name\> \<remote_object_name\> register \<plate\> \<owner\>

**Example:** ```java client localhost 8080 register 11-22-AA Alexa```


### Lookup a plate

**Usage:** java Client \<host_name\> \<remote_object_name\> lookup \<plate\>

**Example:** ```java l04/Client localhost 8080 lookup 11-22-AA```
