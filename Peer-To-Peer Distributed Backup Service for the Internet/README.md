 # FEUP-SDIS
Project for the Distributed Systems (SDIS) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP).
<br>

### Team Members
Alexandra Isabel Vieites Mendes<br>
* Student Number: 201604741
* E-Mail: up201604741@fe.up.pt

Diogo Filipe da Silva Yaguas<br>
* Student Number: 201606165
* E-Mail: up201606165@fe.up.pt

Gonçalo Nuno Bernardo<br>
* Student Number : 201606058
* E-Mail : up201606058@fe.up.pt

Pedro Lopes<br>
* Student Number : 201603557
* E-Mail : up201603557@fe.up.pt


## Peer-To-Peer Distributed Backup Service for the Internet

### Compiling

Open a Terminal by pressing **CTRL+ALT+T** and enter ```bash compile.sh``` to compile

### Starting RMI

Enter the command ```bash rmi.sh``` to execute RMI.<br>
A message saying "rmiregistry : no process found" will appear. Afterwards, a new process will be created. 

## Running

### Create Peer(s)

When running on the same computer, you define your IP as your localhost. On different computers, you run ```if config ``` and check that computer's IP and use it as your second argument when running your peer.

Firstly, you create a peer initiator 

**Usage:** ```java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit <port_number>```

**E.g.:** java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit 8080   


Then, you create the rest of the peers using the port number of the first one

**Usage:** ```java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit <port_number> <network_adress> <port_numer_peer1>```

**E.g.:** java -Djavax.net.ssl.keyStore=keystore -Djavax.net.ssl.keyStorePassword=mortelenta program.PeerInit 8081 localhost 8080     
    
You can also not specify the adresses or ports for the channels, in that case, the defaults will be used.
      
 **Usage:** ```java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit <peerID> <subProtocol> <OPER1> <OPER2>```
 
 ## ClientInit
 
 ### Backup Protocol:
**Usage:** ```java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit <peerID> BACKUP <fileID> <replicationDegree>```

**E.g.:** java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit 586004191 BACKUP boo.jpg 2

### Restore Protocol:
**Usage:** ```java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit <peerID> RESTORE <fileID>```

**E.g.:** java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit 586004191 RESTORE boo.jpg

### Delete Protocol:
**Usage:** ```java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit <peerID> DELETE <fileID>```

**E.g.:** java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit 586004191 DELETE boo.jpg

### Reclaim Protocol:
**Usage:** ```java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit <peerID> RECLAIM <space>```

**E.g.:** java -Djavax.net.ssl.trustStore=truststore -Djavax.net.ssl.trustStorePassword=mortelenta comms.ClientInit 586004191 RECLAIM 100

