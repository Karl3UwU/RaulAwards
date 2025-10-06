package com.example.backend.service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.entity.Image;
import com.example.backend.entity.ImageType;
import com.example.backend.entity.WeeklyWinner;
import com.example.backend.repository.ImageRepository;
import com.example.backend.repository.WeeklyWinnerRepository;
import com.example.backend.util.FileUtil;

@Service
@Transactional
public class WeeklyWinnerService {

    @Autowired
    private WeeklyWinnerRepository weeklyWinnerRepository;

    @Autowired
    private ImageRepository imageRepository;

    /**
     * Validates if the given date is a Sunday
     */
    public void validateSundayDate(LocalDate date) {
        if (date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException(
                "The provided date (" + date + ") is not a Sunday. It's a " + date.getDayOfWeek()
            );
        }
    }

    /**
     * Creates a new weekly winner entry
     */
    public WeeklyWinner createWeeklyWinner(LocalDate sundayDate, ImageType type, 
                                          MultipartFile imageFile, String title) throws IOException {
        // Validate it's a Sunday
        validateSundayDate(sundayDate);

        // Check if entry already exists
        if (weeklyWinnerRepository.existsBySundayDateAndType(sundayDate, type)) {
            throw new IllegalStateException(
                "A winner entry for " + type + " already exists for Sunday " + sundayDate
            );
        }

        // Create and save the image
        byte[] imageBytes = imageFile.getBytes();
        String detectedMimeType = FileUtil.getMimeTypeFromFile(imageFile);
        
        System.out.println("DEBUG: Image file size: " + imageBytes.length + " bytes");
        System.out.println("DEBUG: Original content type: " + imageFile.getContentType());
        System.out.println("DEBUG: Detected MIME type: " + detectedMimeType);
        System.out.println("DEBUG: Original filename: " + imageFile.getOriginalFilename());
        
        Image image = new Image();
        image.setImageData(imageBytes);
        image.setType(type);
        image.setTitle(title != null ? title : "Winner for " + sundayDate);
        image.setMimeType(detectedMimeType);
        Image savedImage = imageRepository.save(image);
        
        System.out.println("DEBUG: Saved image ID: " + savedImage.getId());

        // Create and save the weekly winner
        WeeklyWinner weeklyWinner = new WeeklyWinner(sundayDate, type, savedImage);
        return weeklyWinnerRepository.save(weeklyWinner);
    }

    /**
     * Updates an existing weekly winner or creates a new one if it doesn't exist
     */
    public WeeklyWinner updateOrCreateWeeklyWinner(LocalDate sundayDate, ImageType type,
                                                   MultipartFile imageFile, String title) throws IOException {
        // Validate it's a Sunday
        validateSundayDate(sundayDate);

        Optional<WeeklyWinner> existingWinner = weeklyWinnerRepository.findBySundayDateAndType(sundayDate, type);

        if (existingWinner.isPresent()) {
            // Update existing entry
            WeeklyWinner winner = existingWinner.get();
            Image existingImage = winner.getImage();

            // Update the image data
            existingImage.setImageData(imageFile.getBytes());
            existingImage.setMimeType(imageFile.getContentType());
            if (title != null) {
                existingImage.setTitle(title);
            }
            imageRepository.save(existingImage);
            
            return winner;
        } else {
            // Create new entry
            Image image = new Image();
            image.setImageData(imageFile.getBytes());
            image.setType(type);
            image.setTitle(title != null ? title : "Winner for " + sundayDate);
            image.setMimeType(imageFile.getContentType());
            Image savedImage = imageRepository.save(image);

            WeeklyWinner weeklyWinner = new WeeklyWinner(sundayDate, type, savedImage);
            return weeklyWinnerRepository.save(weeklyWinner);
        }
    }

    /**
     * Check if a winner was updated or created
     */
    public boolean wasExistingWinner(LocalDate sundayDate, ImageType type) {
        return weeklyWinnerRepository.existsBySundayDateAndType(sundayDate, type);
    }

    /**
     * Get current week winners - always returns the latest Sunday period
     */
    @Transactional(readOnly = true)
    public List<WeeklyWinner> getCurrentWeekWinners() {
        // Get the current/latest Sunday
        LocalDate currentSunday = getCurrentSunday();
        
        // Get winners for the current Sunday (may be empty if no winners yet)
        List<WeeklyWinner> winners = weeklyWinnerRepository.findBySundayDate(currentSunday);
        
        // Force loading of image data within the transaction
        for (WeeklyWinner winner : winners) {
            if (winner.getImage() != null) {
                // Access the image data to ensure it's loaded
                try {
                    byte[] data = winner.getImage().getImageData();
                    System.out.println("DEBUG: Loaded image data for winner " + winner.getId() + ", size: " + (data != null ? data.length : "null"));
                } catch (Exception e) {
                    System.err.println("DEBUG: Error loading image data for winner " + winner.getId() + ": " + e.getMessage());
                }
            }
        }
        
        return winners;
    }
    
    /**
     * Get the current/latest Sunday date
     */
    private LocalDate getCurrentSunday() {
        LocalDate today = LocalDate.now();
        
        // If today is Sunday, return today
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return today;
        }
        
        // Otherwise, find the most recent Sunday
        int daysSinceSunday = today.getDayOfWeek().getValue() % 7;
        return today.minusDays(daysSinceSunday);
    }

    /**
     * Get all winners ordered by date
     */
    public List<WeeklyWinner> getAllWinners() {
        return weeklyWinnerRepository.findAllByOrderBySundayDateDesc();
    }

    /**
     * Get winners by type
     */
    public List<WeeklyWinner> getWinnersByType(ImageType type) {
        return weeklyWinnerRepository.findByTypeOrderBySundayDateDesc(type);
    }

    /**
     * Delete a weekly winner entry
     */
    public void deleteWeeklyWinner(LocalDate sundayDate, ImageType type) {
        Optional<WeeklyWinner> winner = weeklyWinnerRepository.findBySundayDateAndType(sundayDate, type);
        
        if (winner.isPresent()) {
            WeeklyWinner winnerToDelete = winner.get();
            Image imageToDelete = winnerToDelete.getImage();
            
            // Delete winner first (due to foreign key)
            weeklyWinnerRepository.delete(winnerToDelete);
            
            // Then delete the image
            imageRepository.delete(imageToDelete);
        } else {
            throw new IllegalArgumentException(
                "No winner found for " + type + " on " + sundayDate
            );
        }
    }

    /**
     * Get winners for a specific Sunday
     */
    public List<WeeklyWinner> getWinnersForDate(LocalDate sundayDate) {
        // Don't validate Sunday requirement for read operations - just return empty if not Sunday
        if (sundayDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            return new ArrayList<>();
        }
        return weeklyWinnerRepository.findBySundayDate(sundayDate);
    }

    /**
     * Get the latest 2 winners (current week)
     */
    public List<WeeklyWinner> getLatestWinners() {
        return weeklyWinnerRepository.findTop2ByOrderBySundayDateDesc();
    }

    /**
     * Update winner title only (no image change)
     */
    public void updateWinnerTitle(LocalDate sundayDate, ImageType type, String title) {
        Optional<WeeklyWinner> winnerOpt = weeklyWinnerRepository.findBySundayDateAndType(sundayDate, type);
        
        if (winnerOpt.isPresent()) {
            WeeklyWinner winner = winnerOpt.get();
            Image image = winner.getImage();
            image.setTitle(title != null ? title : "Winner for " + sundayDate);
            imageRepository.save(image);
        } else {
            throw new IllegalArgumentException(
                "No winner found for " + type + " on " + sundayDate
            );
        }
    }
}