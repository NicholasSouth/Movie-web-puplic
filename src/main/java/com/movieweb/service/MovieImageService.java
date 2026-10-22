package com.movieweb.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.http.Part;

/* Saves movie posters under pictures/movies. Every upload gets a new unique file name, so a new poster never
 * overwrites a poster that is already there (for example a hand-placed BACKROOM.jpg).
 * Validates size, extension, content type and the file's first bytes.
 */
public class MovieImageService {
    public static final long MAX_SIZE = 5L * 1024L * 1024L;
    /* Only files this service created are ever deleted by deleteQuietly. */
    private static final Pattern OWN_FILE = Pattern.compile("^pictures/movies/movie_[0-9]+_[0-9a-f]{8}\\.(jpg|jpeg|png|webp)$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private final ServletContext servletContext;

    public MovieImageService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* Saves the upload and returns the relative path to store in the database (pictures/movies/...).
     * Throws IllegalArgumentException with a message that is safe to show to the admin.
     */
    public String save(Part part) {
        if (part == null || part.getSize() == 0) {
            throw new IllegalArgumentException("Please choose a poster image.");
        }
        if (part.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("The poster is too large. Maximum size is 5 MB.");
        }
        String submitted = part.getSubmittedFileName();
        String extension = extensionOf(submitted);
        String contentType = part.getContentType() == null ? "" : part.getContentType().toLowerCase(Locale.ROOT);
        if (extension == null || !extensionMatches(extension, contentType)) {
            throw new IllegalArgumentException("The poster must be a JPG, PNG or WebP file.");
        }
        String realDirectory = servletContext == null ? null : servletContext.getRealPath("/pictures/movies");
        if (realDirectory == null) {
            throw new IllegalArgumentException("The poster folder is not available on this server.");
        }
        File directory = new File(realDirectory);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalArgumentException("The poster folder could not be created.");
        }
        String fileName = "movie_" + System.currentTimeMillis() + "_" + randomHex() + "." + extension;
        Path destination = Paths.get(directory.getAbsolutePath(), fileName);
        try (InputStream input = part.getInputStream()) {
            byte[] head = new byte[12];
            int read = input.read(head);
            if (!looksLikeImage(head, read, extension)) {
                throw new IllegalArgumentException("The file is not a valid JPG, PNG or WebP image.");
            }
            Files.copy(new java.io.SequenceInputStream(new java.io.ByteArrayInputStream(head, 0, Math.max(read, 0)), input),
                    destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            servletContext.log("Failed to save movie poster", e);
            try {
                Files.deleteIfExists(destination);
            } catch (IOException ignored) {
                // nothing more to do
            }
            throw new IllegalArgumentException("The poster could not be saved. Please try again.");
        }
        return "pictures/movies/" + fileName;
    }

    /* Deletes an image this service created. Any other path (for example a hand-placed file) is left alone. */
    public void deleteQuietly(String relativePath) {
        if (relativePath == null || !OWN_FILE.matcher(relativePath).matches() || servletContext == null) {
            return;
        }
        String real = servletContext.getRealPath("/" + relativePath);
        if (real != null) {
            try {
                Files.deleteIfExists(Paths.get(real));
            } catch (IOException ignored) {
                // an orphaned file is harmless
            }
        }
    }

    private String extensionOf(String name) {
        if (name == null) {
            return null;
        }
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) {
            return null;
        }
        String ext = name.substring(dot + 1).toLowerCase(Locale.ROOT);
        return ext.equals("jpg") || ext.equals("jpeg") || ext.equals("png") || ext.equals("webp") ? ext : null;
    }

    private boolean extensionMatches(String extension, String contentType) {
        if (contentType.equals("image/jpeg")) return extension.equals("jpg") || extension.equals("jpeg");
        if (contentType.equals("image/png")) return extension.equals("png");
        if (contentType.equals("image/webp")) return extension.equals("webp");
        return false;
    }

    private boolean looksLikeImage(byte[] b, int read, String extension) {
        if (read < 12) {
            return false;
        }
        if (extension.equals("jpg") || extension.equals("jpeg")) {
            return (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
        }
        if (extension.equals("png")) {
            return (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        }
        return b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
            && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
    }

    private String randomHex() {
        byte[] bytes = new byte[4];
        RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte x : bytes) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }
}
