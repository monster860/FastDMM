package com.github.monster860.fastdmm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Util {

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

}
