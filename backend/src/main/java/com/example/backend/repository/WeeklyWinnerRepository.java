package com.example.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.backend.entity.ImageType;
import com.example.backend.entity.WeeklyWinner;

@Repository
public interface WeeklyWinnerRepository extends JpaRepository<WeeklyWinner, Long> {
    
    // Get the latest 2 entries (current week's winners)
    List<WeeklyWinner> findTop2ByOrderBySundayDateDesc();
    
    // Get all winners ordered by date (newest first)
    List<WeeklyWinner> findAllByOrderBySundayDateDesc();
    
    // Get winners by type ordered by date
    List<WeeklyWinner> findByTypeOrderBySundayDateDesc(ImageType type);
    
    // Get winner for a specific Sunday and type
    Optional<WeeklyWinner> findBySundayDateAndType(LocalDate sundayDate, ImageType type);
    
    // Check if a winner exists for a specific Sunday and type
    boolean existsBySundayDateAndType(LocalDate sundayDate, ImageType type);
    
    // Get all winners for a specific Sunday
    List<WeeklyWinner> findBySundayDate(LocalDate sundayDate);
    
    // Get winners between date range
    List<WeeklyWinner> findBySundayDateBetweenOrderBySundayDateDesc(
        LocalDate startDate, LocalDate endDate);
    
    // Get the latest winner for a specific type
    Optional<WeeklyWinner> findTopByTypeOrderBySundayDateDesc(ImageType type);
    
    // Custom query to get latest winners for both types in one query - FIXED HERE
    @Query("SELECT w FROM WeeklyWinner w WHERE w.sundayDate = " +
           "(SELECT MAX(w2.sundayDate) FROM WeeklyWinner w2)")
    List<WeeklyWinner> findCurrentWeekWinners();
    
    // Custom query to get latest winners without loading image data
    @Query("SELECT w.id, w.sundayDate, w.type, w.image.id, w.image.title, w.image.mimeType " +
           "FROM WeeklyWinner w WHERE w.sundayDate = " +
           "(SELECT MAX(w2.sundayDate) FROM WeeklyWinner w2)")
    List<Object[]> findCurrentWeekWinnersSummary();
}