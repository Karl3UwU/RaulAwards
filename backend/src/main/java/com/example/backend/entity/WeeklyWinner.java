package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "weekly_winners", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"sunday_date", "type"}))
public class WeeklyWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sunday_date", nullable = false)
    private LocalDate sundayDate;  // Make sure this is named sundayDate, not saturdayDate

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImageType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    // Constructors
    public WeeklyWinner() {}

    public WeeklyWinner(LocalDate sundayDate, ImageType type, Image image) {
        this.sundayDate = sundayDate;
        this.type = type;
        this.image = image;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getSundayDate() {  // Make sure this is getSundayDate
        return sundayDate;
    }

    public void setSundayDate(LocalDate sundayDate) {  // Make sure this is setSundayDate
        this.sundayDate = sundayDate;
    }

    public ImageType getType() {
        return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}