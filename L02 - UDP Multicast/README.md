# L02 - UDP Multicast

## Server

Open a Terminal by pressing <kbd>CTRL</kbd> + <kbd>ALT</kbd> + <kbd>T</kbd>, and start the server.

**Usage:** java server \<srvc_port\> \<mcast_addr\> \<mcast_port\>

**Example:** ```java server 8080 225.0.0 8000```

Press <kbd>CTRL</kbd> + <kbd>C</kbd> to stop the server.


## Client

Open a Terminal and send a request to the server.

**Usage:** java client \<mcast_addr\> \<mcast_port\> \<oper\> \<opnd\>*

### Register

**Usage:** java client \<mcast_addr\> \<mcast_port\> register \<plate\> \<owner\>

**Example:** ```java client 225.0.0.0 8000 register 11-11-AA Diogo```


### Lookup

**Usage:** java client \<mcast_addr\> \<mcast_port\> lookup \<plate\>

**Example:** ```java client 225.0.0.0 8000 lookup 11-11-AA```
