package storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileManager {

    private long max_space = 102400000;
    private long used_space = 0;

    private static final String PATH_BASE = "../FILES/";
    private static final String PATH_RESTORE = "RESTORE/";
    private static final String PATH_BACKUP = "BACKUP/";

    private String path_backup;
    private String path_restore;

    private ConcurrentHashMap<String, byte[]> files = new ConcurrentHashMap<String, byte[]>();
    private ConcurrentHashMap<String, Integer> filesRepDegrees = new ConcurrentHashMap<String, Integer>();
    private ConcurrentHashMap<String, InetSocketAddress> originalPeers = new ConcurrentHashMap<String, InetSocketAddress>();


    public FileManager(String peer_id) {
        String path_peer_filesystem = PATH_BASE + peer_id + "/";

        this.path_backup = path_peer_filesystem + PATH_BACKUP;
        this.path_restore = path_peer_filesystem + PATH_RESTORE;

        Path backup_dir = createDirectory(path_peer_filesystem, PATH_BACKUP);
        Path restore_dir = createDirectory(path_peer_filesystem, PATH_RESTORE);
    }

    private Path createDirectory(String base_path, String dir_name) {
        Path path = Paths.get(base_path + dir_name);
        Path new_dir;

        try {
            new_dir = Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Failed to create folder");
            return null;
        }

        System.err.println("Created " + dir_name + " folder");
        return new_dir;
    }

    public boolean backupFile(byte[] bFile, String file_name) {

        String path = this.path_backup + file_name;
        if (writeToFile(bFile, path)) {
            files.put(file_name, bFile);
            return true;
        }
        return false;
    }

    public static byte[] loadFile_bytes(File file) throws IOException {
        FileInputStream file_input = new FileInputStream(file);
        byte[] file_data = new byte[(int) file.length()];

        file_input.read(file_data);
        file_input.close();

        return file_data;
    }

    public byte[] getDataFile(String fileIDtoRestore) {

        byte[] dataToRestore = null;
        String path = this.path_backup + fileIDtoRestore;
        System.out.println("\n\n\n\n\n"+ path + "\n\n\n\n");
        File tempFile = new File(path);

        for (String fileIDBackedUp : files.keySet()) {

            if (fileIDBackedUp.equals(fileIDtoRestore)) {

                 try {

            dataToRestore = loadFile_bytes(tempFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
                
            }
        }

        return dataToRestore;
    }

    public static String getFile_ID(File file) {

        StringBuffer hexadecimal_string = new StringBuffer();

        String file_id = file.getName() + file.lastModified() + file.getParent();

        try {
            // SHA1 Hash function
            MessageDigest computed_content = MessageDigest.getInstance("SHA-1");
            byte[] crypto_hash = computed_content.digest(file_id.getBytes(StandardCharsets.UTF_8));

            // starting to make byte[] into a hexadecimal string through StringBuffer

            for (byte cryptoHash : crypto_hash) {

                String hexadecimal = Integer.toHexString(0xFF & cryptoHash);

                if (hexadecimal.length() == 1)
                    hexadecimal_string.append('0');

                hexadecimal_string.append(hexadecimal);

            }

        } catch (NoSuchAlgorithmException error) {
            error.printStackTrace();
        }

        return new String(hexadecimal_string);
    }

    private boolean writeToFile(byte[] bFile, String file_path) {

        if (this.max_space < this.used_space + bFile.length) {
            System.err.println("Insufficient space to store file");
            return false;
        }

        Path path = Paths.get(file_path);

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        try {
            Files.write(path, bFile);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // System.out.println("FILE_SIZE:    " + bFile.length);
        this.used_space += bFile.length;

        return true;
    }

    public boolean deleteFile(String file_name) {

        String file_path = this.path_backup + file_name;
        Path path = Paths.get(file_path);

        if (Files.exists(path)) {

            try {

                Files.delete(path);

            } catch (IOException e) {

                e.printStackTrace();
                return false;
            }
        }

        int file_size = files.get(file_name).length;
        this.used_space -= file_size;
        // System.out.println("-----------file-size: " + file_size);
        files.remove(file_name);

        return true;
    }

    public String getMostSatisfiedFile() {

        if (this.filesRepDegrees.isEmpty()) {
            System.out.println("Nothing to delete");
            return null;
        }

        String key = null;
        Integer max_chunk = Collections.max(this.filesRepDegrees.values());

        for (Map.Entry<String, Integer> chunk : this.filesRepDegrees.entrySet()) {
            if (chunk.getValue() == max_chunk){
                key = chunk.getKey();
                break;
            }
        }

        System.out.println(" -------- key is: " + key);

        if (key == null)
            return null;

        return key;
    }

    public long getUsed_space() {
        return used_space;
    }

    public long getMax_space() {
        return max_space;
    }

    public void setMax_space(long space) {
        this.max_space = space;
    }

    public void addFileRepDegree(String file_id, int replicationDegree) {
        this.filesRepDegrees.put(file_id, replicationDegree);
    }

    public void decreaseFileRepDegree(String file_id) {
        int replicationDegree = filesRepDegrees.get(file_id) - 1;
        filesRepDegrees.put(file_id, replicationDegree);
    }

    public void addOriginalPeers(String file_id, InetSocketAddress address) {
        this.originalPeers.put(file_id, address);
    }

    public InetSocketAddress getOriginalAddress(String file_id) {
        return this.originalPeers.get(file_id);
    }

    public int getOriginalRepDegree(String file_id) {
        return this.filesRepDegrees.get(file_id);
    }

    public void deleteOriginalPeer(String file_id) {
        this.originalPeers.remove(file_id);
    }

}