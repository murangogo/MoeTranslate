package translateapi.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public abstract class DigestEngine {
    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    static class JavaDigestEngine extends DigestEngine {
        private MessageDigest messageDigest;

        JavaDigestEngine(final String algorithm) {
            try {
                this.messageDigest = MessageDigest.getInstance(algorithm);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] digest(final byte[] byteArray) {
            messageDigest.update(byteArray);
            return messageDigest.digest();
        }

        @Override
        public byte[] digest(final File file) throws IOException {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            DigestInputStream dis = null;

            try {
                fis = new FileInputStream(file);
                bis = new BufferedInputStream(fis);
                dis = new DigestInputStream(bis, messageDigest);

                while (dis.read() != -1) {
                }
            } finally {
                close(dis);
                close(bis);
                close(fis);
            }
            return messageDigest.digest();
        }
    }

    /**
     * Creates new MD5 digest.
     */
    public static DigestEngine md5() {
        return new JavaDigestEngine("MD5");
    }

    /**
     * Returns byte-hash of input byte array.
     */
    public abstract byte[] digest(byte[] input);

    /**
     * Returns byte-hash of input string.
     */
    private byte[] digest(final String input) {
        return digest(getBytes(input));
    }

    /**
     * Returns digest of a file. Implementations may not read the whole
     * file into the memory.
     */
    public abstract byte[] digest(final File file) throws IOException;

    /**
     * Returns string hash of input string.
     */
    String digestString(final String input) {
        return toHexString(digest(input));
    }

    String digestString(final File file) throws IOException {
        return toHexString(digest(file));
    }

    /**
     * Returns String bytes using Jodds default encoding.
     */
    private static byte[] getBytes(final String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Converts bytes to hex string.
     */
    private static String toHexString(final byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        int i = 0;
        for (byte b : bytes) {
            chars[i++] = int2hex((b & 0xF0) >> 4);
            chars[i++] = int2hex(b & 0x0F);
        }
        return new String(chars);
    }

    /**
     * Converts integer digit to heck char.
     */
    private static char int2hex(final int i) {
        return HEX_CHARS[i];
    }

    /**
     * Closes silently the closable object. If it is {@link Flushable}, it
     * will be flushed first. No exception will be thrown if an I/O error occurs.
     */
    private static void close(final Closeable closeable) {
        if (closeable != null) {
            if (closeable instanceof Flushable) {
                try {
                    ((Flushable) closeable).flush();
                } catch (IOException ignored) {
                }
            }
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

}