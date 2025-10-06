package com.example.backend.dto;

import com.example.backend.entity.ImageType;

public class WeeklyWinnerDto {
    private Long id;
    private String sundayDate;
    private ImageType type;
    private ImageSummaryDto image;

    public WeeklyWinnerDto() {}

    public WeeklyWinnerDto(Long id, String sundayDate, ImageType type, ImageSummaryDto image) {
        this.id = id;
        this.sundayDate = sundayDate;
        this.type = type;
        this.image = image;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSundayDate() {
        return sundayDate;
    }

    public void setSundayDate(String sundayDate) {
        this.sundayDate = sundayDate;
    }

    public ImageType getType() {
        return type;
    }

    public void setType(ImageType type) {
        this.type = type;
    }

    public ImageSummaryDto getImage() {
        return image;
    }

    public void setImage(ImageSummaryDto image) {
        this.image = image;
    }
}


