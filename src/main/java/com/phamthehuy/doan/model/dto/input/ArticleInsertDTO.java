package com.phamthehuy.doan.model.dto.input;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ArticleInsertDTO {

    private String title;
    private String content;
    private String image;
    private int roomPrice;
    private String description;
    private String phone;
    private Boolean status;
    //   private Date postTime= DateHeper.now();
    private Date expiryDate;
    private boolean isVip;

}