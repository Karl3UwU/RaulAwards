package com.example.backend.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.backend.dto.ImageSummaryDto;
import com.example.backend.dto.WeeklyWinnerDto;
import com.example.backend.entity.ImageType;
import com.example.backend.entity.WeeklyWinner;
import com.example.backend.service.AuthService;
import com.example.backend.service.WeeklyWinnerService;
import com.example.backend.util.ApiResponse;
import com.example.backend.util.AuthUtil;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/weekly-winners")
public class WeeklyWinnerController {

    @Autowired
    private WeeklyWinnerService weeklyWinnerService;

    @Autowired
    private AuthService authService;

    private boolean isAuthorized(String token) {
        return authService.validate(token);
    }

    private boolean isAdmin(String token) {
        String role = authService.getRole(token);
        return role != null && role.equals("ADMIN");
    }

    private WeeklyWinnerDto toDto(WeeklyWinner w) {
        try {
            return new WeeklyWinnerDto(
                w.getId(),
                w.getSundayDate().toString(),
                w.getType(),
                new ImageSummaryDto(w.getImage().getId(), w.getImage().getTitle())
            );
        } catch (Exception e) {
            // If there's an issue accessing the image (e.g., LOB stream error),
            // create a DTO with minimal image info
            System.err.println("Error creating DTO for winner " + w.getId() + ": " + e.getMessage());
            return new WeeklyWinnerDto(
                w.getId(),
                w.getSundayDate().toString(),
                w.getType(),
                new ImageSummaryDto(w.getImage().getId(), "Image unavailable")
            );
        }
    }

    /**
     * Create a new weekly winner entry
     */
    @PostMapping("/create")
    public ResponseEntity<?> createWeeklyWinner(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("sundayDate") String sundayDateStr,
            @RequestParam("type") ImageType type,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "title", required = false) String title) {

        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden: admin role required")
                );
            }
            
            // Validate file size (100MB limit)
            long maxFileSize = 100 * 1024 * 1024; // 100MB in bytes
            if (imageFile.getSize() > maxFileSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                    ApiResponse.error("File too large. Maximum size allowed is 100MB. Your file size: " + 
                        String.format("%.2f MB", imageFile.getSize() / (1024.0 * 1024.0)))
                );
            }
            
            // Validate file is not empty
            if (imageFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Image file is empty")
                );
            }
            
            LocalDate sundayDate = LocalDate.parse(sundayDateStr);
            
            // Validate that the date is a Sunday
            if (sundayDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Invalid date: " + sundayDateStr + " is not a Sunday. Only Sunday dates are allowed for weekly winners.")
                );
            }
            
            WeeklyWinner winner = weeklyWinnerService.createWeeklyWinner(sundayDate, type, imageFile, title);

            Map<String, Object> response = ApiResponse.successBuilder()
                .message("Weekly winner created successfully")
                .data("winnerId", winner.getId())
                .data("imageId", winner.getImage().getId())
                .data("sundayDate", sundayDate.toString())
                .data("type", type.toString())
                .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error creating weekly winner: " + e.getMessage())
            );
        }
    }

    /**
     * Update or create a weekly winner entry
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateWeeklyWinner(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("sundayDate") String sundayDateStr,
            @RequestParam("type") ImageType type,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "title", required = false) String title) {

        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden: admin role required")
                );
            }
            
            // Validate file size (100MB limit)
            long maxFileSize = 100 * 1024 * 1024; // 100MB in bytes
            if (imageFile.getSize() > maxFileSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                    ApiResponse.error("File too large. Maximum size allowed is 100MB. Your file size: " + 
                        String.format("%.2f MB", imageFile.getSize() / (1024.0 * 1024.0)))
                );
            }
            
            // Validate file is not empty
            if (imageFile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Image file is empty")
                );
            }
            
            LocalDate sundayDate = LocalDate.parse(sundayDateStr);
            
            // Validate that the date is a Sunday
            if (sundayDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Invalid date: " + sundayDateStr + " is not a Sunday. Only Sunday dates are allowed for weekly winners.")
                );
            }
            
            boolean wasExisting = weeklyWinnerService.wasExistingWinner(sundayDate, type);
            
            WeeklyWinner winner = weeklyWinnerService.updateOrCreateWeeklyWinner(
                sundayDate, type, imageFile, title
            );

            String message = wasExisting 
                ? "Weekly winner updated successfully" 
                : "Weekly winner created successfully (no existing entry found)";

            Map<String, Object> response = ApiResponse.successBuilder()
                .message(message)
                .data("winnerId", winner.getId())
                .data("imageId", winner.getImage().getId())
                .data("sundayDate", sundayDate.toString())
                .data("type", type.toString())
                .data("action", wasExisting ? "updated" : "created")
                .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error updating weekly winner: " + e.getMessage())
            );
        }
    }

    /**
     * Get current week winners
     */
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentWeekWinners(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(
                weeklyWinnerService.getCurrentWeekWinners()
                    .stream().map(this::toDto).collect(Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching current week winners: " + e.getMessage())
            );
        }
    }

    /**
     * Get all winners
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllWinners(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(
                weeklyWinnerService.getAllWinners()
                    .stream().map(this::toDto).collect(Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching winners: " + e.getMessage())
            );
        }
    }

    /**
     * Get winners by type
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<?> getWinnersByType(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable ImageType type) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(
                weeklyWinnerService.getWinnersByType(type)
                    .stream().map(this::toDto).collect(Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching winners by type: " + e.getMessage())
            );
        }
    }

    /**
     * Get winners for a specific Sunday
     */
    @GetMapping("/by-date")
    public ResponseEntity<?> getWinnersForDate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("sundayDate") String sundayDateStr) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            LocalDate sundayDate = LocalDate.parse(sundayDateStr);
            return ResponseEntity.ok(
                weeklyWinnerService.getWinnersForDate(sundayDate)
                    .stream().map(this::toDto).collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching winners: " + e.getMessage())
            );
        }
    }

    /**
     * Get latest 2 winners
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestWinners(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(
                weeklyWinnerService.getLatestWinners()
                    .stream().map(this::toDto).collect(Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching latest winners: " + e.getMessage())
            );
        }
    }

    /**
     * Archive endpoint - list all Sundays between start and end with presence flags
     */
    @GetMapping("/archive")
    public ResponseEntity<?> getArchive(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("start") String startStr,
            @RequestParam("end") String endStr) {
        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }

            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);

            if (start.getDayOfWeek() != DayOfWeek.SUNDAY) {
                start = start.minusDays((start.getDayOfWeek().getValue()) % 7);
            }
            if (end.getDayOfWeek() != DayOfWeek.SUNDAY) {
                end = end.minusDays((end.getDayOfWeek().getValue()) % 7);
            }

            Map<LocalDate, Map<String, Boolean>> byDate = new HashMap<>();
            for (WeeklyWinner w : weeklyWinnerService.getAllWinners()) {
                if (!w.getSundayDate().isBefore(start) && !w.getSundayDate().isAfter(end)) {
                    byDate.computeIfAbsent(w.getSundayDate(), d -> new HashMap<>())
                          .put(w.getType().name().toLowerCase(), true);
                }
            }

            List<Map<String, Object>> archive = new ArrayList<>();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusWeeks(1)) {
                Map<String, Object> row = new HashMap<>();
                row.put("sundayDate", d.toString());
                Map<String, Boolean> flags = byDate.getOrDefault(d, Collections.emptyMap());
                row.put("overall", flags.getOrDefault("overall", false));
                row.put("raul", flags.getOrDefault("raul", false));
                archive.add(row);
            }

            archive.sort((a, b) -> ((String)b.get("sundayDate")).compareTo((String)a.get("sundayDate")));
            return ResponseEntity.ok(archive);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error building archive: " + e.getMessage())
            );
        }
    }

    /**
     * Delete a weekly winner
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteWeeklyWinner(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam("sundayDate") String sundayDateStr,
            @RequestParam("type") ImageType type) {

        try {
            String token = AuthUtil.extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            if (!isAdmin(token)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error("Forbidden: admin role required")
                );
            }
            LocalDate sundayDate = LocalDate.parse(sundayDateStr);
            
            // Validate that the date is a Sunday
            if (sundayDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.error("Invalid date: " + sundayDateStr + " is not a Sunday. Only Sunday dates are allowed for weekly winners.")
                );
            }
            
            weeklyWinnerService.deleteWeeklyWinner(sundayDate, type);
            
            return ResponseEntity.ok(
                ApiResponse.success("Weekly winner deleted successfully")
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error deleting weekly winner: " + e.getMessage())
            );
        }
    }
}