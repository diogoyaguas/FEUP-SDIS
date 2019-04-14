 # FEUP-SDIS
Project for the Distributed Systems (SDIS) class of the Master in Informatics and Computer Engineering (MIEIC) at the Faculty of Engineering of the University of Porto (FEUP).
<br><br>

## Serverless Distributed Backup Service

### Compiling

Open a Terminal by pressing **CTRL+ALT+T** and enter ```javac *.java``` to compile

### Starting RMI

Enter the command ```start rmiregistry``` to execute RMI.

### Running

#### Create Peer(s)

**Usage:** ```java	Peer <protocolVersion> <serverId> <peerApp> <MCAddress><MCPort> <MDBAddress><MDBPort> <MDRAddress><MDRPort>```

**E.g.:** java Peer 1.0 1 peer1 224.0.0.15 8001 224.0.0.16 8002 224.0.0.17 8003     
    
You can also not specify the adresses or ports for the channels, in that case, the defaults will be used.

#### Defaults


|MC            |MDB           |MDR           |
|--------------|--------------|--------------|
|224.0.0.0:8000|224.0.0.0:8001|224.0.0.0:8002|

      
 **Usage:** ```java Peer <protocolVersion> <serverId> <peerApp>```
 
 #### App
 
 ### Backup Protocol:
**Usage:** ```java App <peerAccessPoint> BACKUP <fileID> <replicationDegree>```

**E.g.:**  java App peer1 BACKUP C:\Users\johndoe\Documents\sdis.pdf 2

### Restore Protocol:
**Usage:** ```java App <peerAccessPoint> RESTORE <fileID>```

**E.g.:** java App peer1 RESTORE C:\Users\johndoe\Documents\sdis.pdf

### Delete Protocol:
**Usage:** ```java App <peerAccessPoint> DELETE <fileID>```

**E.g.:** java App peer1 DELETE C:\Users\johndoe\Documents\sdis.pdf

### Reclaim Protocol:
**Usage:** ```java App <peerAccessPoint> RECLAIM <space>```

**E.g.:** java App peer1 RECLAIM 100

### State Protocol:
**Usage:** ```java App <peerAccessPoint> STATE```

**E.g.:** java App peer1 STATE
 
 
### Team Members
Alexandra Isabel Vieites Mendes<br>
* Student Number: 201604741
* E-Mail: up201604741@fe.up.pt

Diogo Filipe da Silva Yaguas<br>
* Student Number: 201606165
* E-Mail: up201606165@fe.up.pt
 
 
 
