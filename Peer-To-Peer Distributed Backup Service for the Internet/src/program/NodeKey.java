package program;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NodeKey implements Serializable {

    private final long key;
    private static final int KEY_SIZE = 32;
    private static final int MAX = (int) Math.pow(2, KEY_SIZE);

    private NodeKey(final long key) {
        this.key = key % MAX;
    }

    public NodeKey(final int key) {
        this(key & 0x00000000ffffffffL);
    }

    public static NodeKey keyFromAddress(InetSocketAddress address) {
        return address == null ? null : new NodeKey(hashSocketAddress(address));
    }

    private static int hashSocketAddress(InetSocketAddress address) {
        String ip = address.getAddress().getHostAddress();
        String port = Integer.toString(address.getPort());
        byte[] data = (ip + port).getBytes();
        return hashData(data).hashCode();
    }

    private static String hashData(byte[] data) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to get algorithm instance.", e);
        }

        messageDigest.update(data);

        return new String(messageDigest.digest());
    }

    /**
     * Is key in range ]lower, upper]
     * 
     * @param lower exclusive lower bound
     * @param upper inclusive upper bound
     * @return whether key is in range ]lower, upper]
     */
    public boolean isBetween(final NodeKey lower, final NodeKey upper) {
        if (lower.key >= upper.key) {
            return this.key > lower.key || this.key <= upper.key;
        } else {
            return this.key > lower.key && this.key <= upper.key;
        }
    }

    @Override
    public int hashCode() {
        return (int) this.key;
    }

    /**
     * Compares two keys
     * 
     * @return true if same object or key values are the same
     */
    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof NodeKey))
            return false;

        return this == object || this.key == ((NodeKey) object).key;
    }

    @Override
    public String toString() {
        return this.key + "";
    }
}