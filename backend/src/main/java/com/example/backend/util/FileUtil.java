package com.example.backend.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
    
    // Map of file extensions to MIME types
    private static final Map<String, String> EXTENSION_TO_MIME = new HashMap<>();
    
    // Map of MIME types to file extensions
    private static final Map<String, String> MIME_TO_EXTENSION = new HashMap<>();
    
    static {
        // Initialize extension to MIME mapping
        EXTENSION_TO_MIME.put("jpg", "image/jpeg");
        EXTENSION_TO_MIME.put("jpeg", "image/jpeg");
        EXTENSION_TO_MIME.put("jfif", "image/jpeg"); // JFIF is JPEG variant
        EXTENSION_TO_MIME.put("png", "image/png");
        EXTENSION_TO_MIME.put("gif", "image/gif");
        EXTENSION_TO_MIME.put("webp", "image/webp");
        EXTENSION_TO_MIME.put("svg", "image/svg+xml");
        EXTENSION_TO_MIME.put("heic", "image/heic");
        EXTENSION_TO_MIME.put("heif", "image/heif");
        EXTENSION_TO_MIME.put("avif", "image/avif");
        EXTENSION_TO_MIME.put("tiff", "image/tiff");
        EXTENSION_TO_MIME.put("tif", "image/tiff");
        EXTENSION_TO_MIME.put("bmp", "image/bmp");
        EXTENSION_TO_MIME.put("ico", "image/x-icon");
        EXTENSION_TO_MIME.put("cr2", "image/x-canon-cr2");
        EXTENSION_TO_MIME.put("crw", "image/x-canon-crw");
        EXTENSION_TO_MIME.put("nef", "image/x-nikon-nef");
        EXTENSION_TO_MIME.put("arw", "image/x-sony-arw");
        EXTENSION_TO_MIME.put("dng", "image/x-adobe-dng");
        EXTENSION_TO_MIME.put("pcx", "image/x-pcx");
        EXTENSION_TO_MIME.put("ppm", "image/x-portable-pixmap");
        EXTENSION_TO_MIME.put("pgm", "image/x-portable-graymap");
        EXTENSION_TO_MIME.put("pbm", "image/x-portable-bitmap");
        
        // Initialize MIME to extension mapping
        MIME_TO_EXTENSION.put("image/jpeg", "jpg");
        MIME_TO_EXTENSION.put("image/jpg", "jpg");
        MIME_TO_EXTENSION.put("image/png", "png");
        MIME_TO_EXTENSION.put("image/gif", "gif");
        MIME_TO_EXTENSION.put("image/webp", "webp");
        MIME_TO_EXTENSION.put("image/svg+xml", "svg");
        MIME_TO_EXTENSION.put("image/svg", "svg");
        MIME_TO_EXTENSION.put("image/heic", "heic");
        MIME_TO_EXTENSION.put("image/heif", "heic");
        MIME_TO_EXTENSION.put("image/avif", "avif");
        MIME_TO_EXTENSION.put("image/tiff", "tiff");
        MIME_TO_EXTENSION.put("image/tif", "tiff");
        MIME_TO_EXTENSION.put("image/bmp", "bmp");
        MIME_TO_EXTENSION.put("image/x-ms-bmp", "bmp");
        MIME_TO_EXTENSION.put("image/x-icon", "ico");
        MIME_TO_EXTENSION.put("image/vnd.microsoft.icon", "ico");
        MIME_TO_EXTENSION.put("image/x-canon-cr2", "cr2");
        MIME_TO_EXTENSION.put("image/x-canon-crw", "crw");
        MIME_TO_EXTENSION.put("image/x-nikon-nef", "nef");
        MIME_TO_EXTENSION.put("image/x-sony-arw", "arw");
        MIME_TO_EXTENSION.put("image/x-adobe-dng", "dng");
        MIME_TO_EXTENSION.put("image/x-pcx", "pcx");
        MIME_TO_EXTENSION.put("image/x-portable-pixmap", "ppm");
        MIME_TO_EXTENSION.put("image/x-portable-graymap", "pgm");
        MIME_TO_EXTENSION.put("image/x-portable-bitmap", "pbm");
    }
    
    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Get MIME type from file extension
     */
    public static String getMimeTypeFromExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return "application/octet-stream";
        }
        
        return EXTENSION_TO_MIME.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }
    
    /**
     * Get MIME type from MultipartFile, with fallback to extension-based detection
     */
    public static String getMimeTypeFromFile(MultipartFile file) {
        // First try to get MIME type from the file itself
        String mimeType = file.getContentType();
        
        if (mimeType != null && !mimeType.equals("application/octet-stream")) {
            return mimeType;
        }
        
        // Fallback to extension-based detection
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getFileExtension(filename);
            return getMimeTypeFromExtension(extension);
        }
        
        return "application/octet-stream";
    }
    
    /**
     * Get file extension from MIME type
     */
    public static String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "jpg";
        }
        
        return MIME_TO_EXTENSION.getOrDefault(mimeType.toLowerCase(), "jpg");
    }
    
    /**
     * Get filename with proper extension based on MIME type
     */
    public static String getFilenameWithExtension(String baseName, String mimeType) {
        String extension = getExtensionFromMimeType(mimeType);
        return baseName + "." + extension;
    }
    
    /**
     * Check if file extension is a valid image format
     */
    public static boolean isImageExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return false;
        }
        
        return EXTENSION_TO_MIME.containsKey(extension.toLowerCase());
    }
    
    /**
     * Check if MIME type is a valid image format
     */
    public static boolean isImageMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        
        return MIME_TO_EXTENSION.containsKey(mimeType.toLowerCase());
    }
}
