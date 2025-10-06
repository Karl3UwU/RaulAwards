package com.example.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.Image;
import com.example.backend.entity.ImageType;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    
    // Find all images by type
    List<Image> findByType(ImageType type);
    
    // Find all images ordered by upload date (newest first)
    List<Image> findAllByOrderByUploadDateDesc();
    
    // Find images by type ordered by upload date
    List<Image> findByTypeOrderByUploadDateDesc(ImageType type);
}