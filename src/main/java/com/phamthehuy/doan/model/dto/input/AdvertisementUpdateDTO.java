package com.phamthehuy.doan.model.dto.input;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementUpdateDTO {
    private Integer advertisementId;
    private String title;
    private String content;
    private String image;
}
