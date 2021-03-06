import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class FileData {

    /**
     * Load all information from a File
     *
     * @param file File
     */
    static byte[] loadFile(File file) throws IOException {

        try {
            FileInputStream file_is = new FileInputStream(file);

            byte[] file_data = new byte[(int) file.length()];

            file_is.read(file_data);
            file_is.close();

            return file_data;
        } catch (FileNotFoundException e) {
            // do stuff here..
            return null;
        }
    }

    /**
     * Get file identifier for the backup service
     *
     * @param file File
     */
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

    /**
     * Create a folder
     *
     * @param name name of the folder
     */
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

}
