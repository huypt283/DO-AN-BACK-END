package com.phamthehuy.doan.model.dto.output;

import com.phamthehuy.doan.model.entity.Advertisement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdvertisementOutputDTO {
    public AdvertisementOutputDTO(Advertisement advertisement){
        this.advertisementId=advertisement.getAdvertisementId();
        this.title=advertisement.getTitle();
        this.content=advertisement.getContent();
        this.image=advertisement.getImage();
        this.staffId=advertisement.getStaff().getStaffId();
    }

    private Integer advertisementId;
    private String title;
    private String content;
    private String image;
    private Integer staffId;
}
