package com.movieweb.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.Part;
import javax.servlet.ServletContext;

import com.movieweb.model.Users;

public class ImageService{
	private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;
	private static final long MAX_BANNER_SIZE = 10L * 1024L * 1024L;
	private static final String[] ALLOWED_EXTENSIONS = {
	    "jpg",
	    "jpeg",
	    "png",
	    "webp"
	};
	private static final String[] ALLOWED_CONTENT_TYPES =
	{
	    "image/jpeg",
	    "image/png",
	    "image/webp"
	};
	private ServletContext servletContext; 
	public ImageService( ServletContext servletContext) { 
		this.servletContext = servletContext; 
	}
	
	/*Save an uploaded avatar.*/
	public String saveAvatar(
	        Users user,
	        Part imagePart)
	{
	    if (imagePart == null
	            || imagePart.getSize() == 0)
	    {
	        return null;
	    }
	    return saveImage(
	            user,
	            imagePart,
	            "avatars",
	            MAX_AVATAR_SIZE);
	}

	/*Save an uploaded banner.*/
	public String saveBanner(
	        Users user,
	        Part imagePart)
	{
	    if (imagePart == null
	            || imagePart.getSize() == 0)
	    {
	        return null;
	    }
	    return saveImage(
	            user,
	            imagePart,
	            "banners",
	            MAX_BANNER_SIZE);
	}

	/*General image-saving method.*/
	private String saveImage(
	        Users user,
	        Part imagePart,
	        String folderName,
	        long maxFileSize)
	{
	    if (user == null || imagePart == null)
	    {
	        return null;
	    }
	
	    /*Check file size.*/
	    if (imagePart.getSize() > maxFileSize)
	    {
	        return null;
	    }
	
	    /*Check content type.*/
	    String contentType = imagePart.getContentType();
	    if (!isAllowedContentType(contentType))
	    {
	        return null;
	    }
	
	    /*Get the original filename only to determine its extension.
	    Don't use og name*/
	    String submittedFileName = imagePart.getSubmittedFileName();
	    if (submittedFileName == null || submittedFileName.isEmpty())
	    {
	        return null;
	    }
	    String extension = getExtension(submittedFileName);
	    if (extension == null || !isAllowedExtension(extension))
	    {
	        return null;
	    }
	
	    /*Make sure the extension agrees with the content type.*/
	    if (!extensionMatchesContentType(extension, contentType))
	    {
	        return null;
	    }
	
	    /*Username is permanent, so it is safe to use it as the base filename after sanitizing it.*/
	    String safeUsername = sanitizeFileName(user.getUsername());
	    if (safeUsername.isEmpty())
	    {
	        return null;
	    }
	    String fileName =
	            safeUsername
	            + "."
	            + extension;
	
	    /*Get the actual deployed web application's pictures directory.*/
	    String realDirectory = getRealDirectory(folderName);
	    if (realDirectory == null)
	    {
	        return null;
	    }
	    File directory = new File(realDirectory);
	
	    /*Create the directory if necessary.*/
	    if (!directory.exists()
	            && !directory.mkdirs())
	    {
	        return null;
	    }
	
	    /*Full destination path.*/
	    Path destination = Paths.get(directory.getAbsolutePath(), fileName);
	    try
	    {
	        try (InputStream input = imagePart.getInputStream())
	        {
	            /*Check the real file signature (magic bytes) before touching anything on disk.*/
	            byte[] head = new byte[12];
	            int read = readFully(input, head);
	            if (!looksLikeImage(head, read, extension))
	            {
	                return null;
	            }
	
	            /*Delete any previous image belonging to this user.*/
	            deleteOldImages(directory, safeUsername);
	
	            /*Save the new image.*/
	            Files.copy(
	                    new java.io.SequenceInputStream(
	                            new java.io.ByteArrayInputStream(head, 0, read),
	                            input),
	                    destination,
	                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	        }
	
	        /*Return the relative path that should be stored in the database.*/
	        return "pictures/"
	                + folderName
	                + "/"
	                + fileName;
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	        /*If saving failed, remove the partially-created file.*/
	        try
	        {
	            Files.deleteIfExists(destination);
	        }
	        catch (IOException ignored){        	
	        }
	        return null;
	    }
	}

	/*Read up to buffer.length bytes; returns the number of bytes read.*/
	private int readFully(InputStream input, byte[] buffer) throws IOException
	{
	    int total = 0;
	    while (total < buffer.length)
	    {
	        int n = input.read(buffer, total, buffer.length - total);
	        if (n < 0)
	        {
	            break;
	        }
	        total += n;
	    }
	    return total;
	}

	/*Magic-byte check: the content must really be a JPEG, PNG or WebP image.*/
	private boolean looksLikeImage(byte[] b, int read, String extension)
	{
	    if (read < 12)
	    {
	        return false;
	    }
	    if (extension.equals("jpg") || extension.equals("jpeg"))
	    {
	        return (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
	    }
	    if (extension.equals("png"))
	    {
	        return (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
	    }
	    return b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
	            && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
	}

	/*Get the physical directory inside the deployed web application.*/
	private String getRealDirectory(String folderName)
	{
		if (servletContext == null)
	    {
	        return null;
	    }
	    return servletContext.getRealPath("/pictures/" + folderName);
	}

	/*Check whether the MIME type is allowed.*/
	private boolean isAllowedContentType(String contentType)
	{
	    if (contentType == null)
	    {
	        return false;
	    }
	    for (String allowed : ALLOWED_CONTENT_TYPES)
	    {
	        if (allowed.equalsIgnoreCase(contentType))
	        {
	            return true;
	        }
	    }
	    return false;
	}

	/*Check whether the file extension is allowed.*/
	private boolean isAllowedExtension(String extension)
	{
	    for (String allowed : ALLOWED_EXTENSIONS)
	    {
	        if (allowed.equalsIgnoreCase(extension))
	        {
	            return true;
	        }
	    }
	    return false;
	}

	/*Make sure the extension and MIME type agree.*/
	private boolean extensionMatchesContentType(
	        String extension,
	        String contentType)
	{
	    if (contentType == null)
	    {
	        return false;
	    }
	    if (contentType.equalsIgnoreCase("image/jpeg"))
	    {
	        return extension.equals("jpg") || extension.equals("jpeg");
	    }
	    if (contentType.equalsIgnoreCase("image/png"))
	    {
	        return extension.equals("png");
	    }
	    if (contentType.equalsIgnoreCase("image/webp"))
	    {
	        return extension.equals("webp");
	    }
	    return false;
	}

	/*Extract the extension from the uploaded filename. */
	private String getExtension(
	        String fileName)
	{
	    int dotIndex = fileName.lastIndexOf('.');
	    if (dotIndex < 0 || dotIndex == fileName.length() - 1)
	    {
	        return null;
	    }
	    return fileName.substring(dotIndex + 1).toLowerCase();
	}

	/*Remove characters that should not appear in a generated filename.*/
	private String sanitizeFileName(
	        String fileName)
	{
	    if (fileName == null)
	    {
	        return "";
	    }
	    return fileName.replaceAll("[^a-zA-Z0-9_-]", "_");
	}

	/*Delete all existing image extensions for this username.*/
	private void deleteOldImages(
	        File directory,
	        String safeUsername)
	{
	    for (String extension : ALLOWED_EXTENSIONS)
	    {
	        File oldFile = new File(
			                        directory,
			                        safeUsername
			                        + "."
			                        + extension);
	        if (oldFile.exists())
	        {
	            oldFile.delete();
	        }
	    }
	}
}