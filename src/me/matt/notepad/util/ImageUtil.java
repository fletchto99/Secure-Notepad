package me.matt.notepad.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import me.matt.notepad.SecureNotepad;

public class ImageUtil {

    public static BufferedImage getBufferedImage(final String resource)
            throws IOException {
        final File imageFile = new File(resource);
        return ImageIO.read(imageFile);
    }

    public static Image getImage(final String resource) {
        try {
            return Toolkit.getDefaultToolkit().getImage(
                    ImageUtil.getResourceURL(resource));
        } catch (final Exception e) {
        }
        return null;
    }

    public static URL getResourceURL(final String path)
            throws MalformedURLException {
        return SecureNotepad.RUNNING_FROM_JAR ? SecureNotepad.class
                .getResource("/" + path) : new File(path).toURI().toURL();
    }

}
