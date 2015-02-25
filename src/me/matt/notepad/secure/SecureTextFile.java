package me.matt.notepad.secure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import me.matt.notepad.util.StringUtils;

public class SecureTextFile {

    public static String load(final File file, final String password)
            throws Exception {
        final FileInputStream fis = new FileInputStream(file);
        final byte[] bytearray = SecureTextFile.read(fis);
        fis.close();
        String toParse = null;
        try {
            toParse = SecurityUtility.decrypt(
                    StringUtils.newStringUtf8(bytearray), password);
        } catch (final Exception e) {
            e.printStackTrace();
            toParse = null;
        }
        return toParse == null ? null : toParse;
    }

    private static byte[] read(final InputStream is) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            final byte[] temp = new byte[4096];
            int read;
            while ((read = is.read(temp)) != -1) {
                buffer.write(temp, 0, read);
            }
        } catch (final IOException ignored) {
            buffer = null;
        } finally {
            try {
                if (is != null) {
                    buffer.close();
                    is.close();
                }
            } catch (final IOException ignored) {
            }
        }
        return buffer == null ? null : buffer.toByteArray();
    }

    public static void save(final File file, final String text,
            final String password) throws IOException {
        final String toStore = text;
        if (!file.exists()) {
            file.createNewFile();
        }
        final FileWriter fw = new FileWriter(file);
        try {
            fw.write(SecurityUtility.encrypt(toStore, password));
        } catch (final Exception e) {
            e.printStackTrace();
        }
        fw.flush();
        fw.close();
    }

}
