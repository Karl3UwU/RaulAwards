package com.example.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.entity.Image;
import com.example.backend.repository.ImageRepository;
import com.example.backend.service.AuthService;
import com.example.backend.util.AuthUtil;
import com.example.backend.util.FileUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private AuthService authService;

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        String token = AuthUtil.extractToken(authHeader);
        if (!authService.validate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Image> imageOpt = imageRepository.findById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Image image = imageOpt.get();
        String mimeType = image.getMimeType() != null ? image.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(image.getImageData());
    }

    @GetMapping("/{id}/download")
    @Transactional(readOnly = true)
    public ResponseEntity<?> downloadImage(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {

        String token = AuthUtil.extractToken(authHeader);
        if (!authService.validate(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Image> imageOpt = imageRepository.findById(id);
        if (imageOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Image image = imageOpt.get();
        String mimeType = image.getMimeType() != null ? image.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        
        // Debug logging
        System.out.println("DEBUG: Image MIME type: " + mimeType);
        System.out.println("DEBUG: Image title: " + image.getTitle());
        
        // Get filename with proper extension from mime type
        String filename = FileUtil.getFilenameWithExtension(
            image.getTitle() != null ? image.getTitle() : "image", 
            mimeType
        );
        
        System.out.println("DEBUG: Generated filename: " + filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(image.getImageData());
    }

}


