package com.github.monster860.fastdmm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class contains various utilities.
 */
public class Util {

    /**
     * The Unix separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The system separator character.
     */
    private static final char SYSTEM_SEPARATOR = File.separatorChar;

    /**
     * Attempts to retrieve a {@link InputStream} representation of a file.
     *
     * @param path a path to a file.
     * @return a {@link InputStream} representation of the file passed
     */
    public static InputStream getFile(String path) {
        return FastDMM.class.getClassLoader().getResourceAsStream(path);
    }

    /**
     * Attempts to retrieve a {@code byte[]} representation of a file.
     *
     * @param path a path to a file.
     * @return a {@code byte[]} representation of a file.
     * @throws IOException
     */
    public static byte[] getFileAsBytes(String path) throws IOException {
        InputStream inputStream = getFile(path);
        if (inputStream != null) {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int read;
            byte[] data = new byte[4096];
            while ((read = inputStream.read(data, 0, data.length)) != -1) {
                output.write(data, 0, read);
            }
            inputStream.close();
            return output.toByteArray();
        }
        return null;
    }

    /**
     * Determines if Windows file system is in use.
     *
     * @return true if the system is Windows
     */
    public static boolean isWindowsSystem() {
        return SYSTEM_SEPARATOR == WINDOWS_SEPARATOR;
    }

    /**
     * Converts all separators to the Unix separator of forward slash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToUnix(String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Converts all separators to the Windows separator of backslash.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToWindows(String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    /**
     * Converts all separators to the system separator.
     *
     * @param path the path to be changed, null ignored
     * @return the updated path
     */
    public static String separatorsToSystem(String path) {
        if (path == null) {
            return null;
        }
        if (isWindowsSystem()) {
            return separatorsToWindows(path);
        } else {
            return separatorsToUnix(path);
        }
    }

}
