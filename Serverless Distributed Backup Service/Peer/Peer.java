public class Peer {

    private static double protocolVersion;
    private static int serverId;
    private static String serviceAccessPoint;

    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ScheduledThreadPoolExecutor exec;
    private static Storage storage;

    public static void main(String args[]) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {

            if (args.length != 9) {
                System.out.println(
                        "Usage:\tPeer <version> <server id> <access_point> <MC_IP_address> <MC_port> <MDB_IP_address> <MDB_port> <MDR_IP_address> <MDR_port>");
                return;
            }

            protocolVersion = Double.parseDouble(args[0]);
            serverId = Integer.parseInt(args[1]);
            serviceaccessPoint = args[2];
            String MCAddress = args[3];
            int MCPort = Integer.parseInt(args[4]);
            String MDBAddress = args[5];
            int MDBPort = Integer.parseInt(args[6]);
            String MDRAddress = args[7];
            int MDRPort = Integer.parseInt(args[8]);

            Peer peer = new Peer(MCAddress, MCPort, MDBAddress, MDBPort, MDRAddress, MDRPort);
            RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind(accessP, stub);

            System.err.println("Peer configured");

        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }

        deserializeStorage(); // loads storage

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); // Saves storage if CTRL-C is pressed
    }
}