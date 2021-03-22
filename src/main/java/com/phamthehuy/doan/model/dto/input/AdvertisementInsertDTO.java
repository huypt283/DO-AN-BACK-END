package com.phamthehuy.doan.model.dto.input;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementInsertDTO {
    private String title;
    private String content;
    private String image;
    private Integer staffId;
}
