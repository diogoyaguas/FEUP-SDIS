import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

class FileData {

    private File file;
    private int repDegree;
    //do i need this?
    private ArrayList<Chunk> chunks;

    public FileData(String path, int repDegree){
        this.file = new File(path);
        this.repDegree = repDegree;
        this.chunks = new ArrayList<>();
    }

    static byte[] loadFile(File file) throws IOException {
        FileInputStream file_is = new FileInputStream(file);

        byte[] file_data = new byte[(int) file.length()];

        file_is.read(file_data);
        file_is.close();

        return file_data;
    }

    static String getFileId(File file) throws NoSuchAlgorithmException {

        String file_id = file.getName() + file.lastModified() + Peer.getServerId();

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] hash = messageDigest.digest(file_id.getBytes(StandardCharsets.UTF_8));

        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[hash.length * 2];

        for (int j = 0; j < hash.length; j++) {
            int v = hash[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);

    }

    static File createFolder(String name) {

        File theDir = new File(name);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("\nCreating Folder " + theDir.getName() + " ...");
            boolean result = false;

            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                System.out.println("\nError creating Peer Folder");
            }
            if (result) {
                System.out.println("Folder created!");
            }
        }

        return theDir;
    }

    public int getReplicationDegree() { return this.repDegree;}
    public File getFile() { return file;}
}
