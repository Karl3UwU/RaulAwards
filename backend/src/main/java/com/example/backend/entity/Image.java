package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] imageData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;

    @Column(nullable = false)
    private LocalDateTime uploadDate;

    @Column
    private String title;

    @Column
    private String mimeType;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }

    // Constructors
    public Image() {}

    public Image(byte[] imageData, ImageType type) {
        this.imageData = imageData;
        this.type = type;
    }

    public Image(byte[] imageData, ImageType type, String title, String mimeType) {
        this.imageData = imageData;
        this.type = type;
        this.title = title;
        this.mimeType = mimeType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public ImageType getType() {
        return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}