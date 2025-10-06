package com.example.backend.controller;

import com.example.backend.entity.ImageType;
import com.example.backend.entity.WeeklyWinner;
import com.example.backend.service.WeeklyWinnerService;
import com.example.backend.service.AuthService;
import com.example.backend.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/weekly-winners")
public class WeeklyWinnerController {

    @Autowired
    private WeeklyWinnerService weeklyWinnerService;

    @Autowired
    private AuthService authService;

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private boolean isAuthorized(String token) {
        return authService.validate(token);
    }

    private boolean isAdmin(String token) {
        String role = authService.getRole(token);
        return role != null && role.equals("ADMIN");
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
            String token = extractToken(authHeader);
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
            String token = extractToken(authHeader);
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
            String token = extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(weeklyWinnerService.getCurrentWeekWinners());
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
            String token = extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(weeklyWinnerService.getAllWinners());
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
            String token = extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(weeklyWinnerService.getWinnersByType(type));
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
            String token = extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            LocalDate sundayDate = LocalDate.parse(sundayDateStr);
            return ResponseEntity.ok(weeklyWinnerService.getWinnersForDate(sundayDate));
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
            String token = extractToken(authHeader);
            if (!isAuthorized(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("Unauthorized")
                );
            }
            return ResponseEntity.ok(weeklyWinnerService.getLatestWinners());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error fetching latest winners: " + e.getMessage())
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
            String token = extractToken(authHeader);
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