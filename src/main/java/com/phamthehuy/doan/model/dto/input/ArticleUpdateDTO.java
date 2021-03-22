package com.phamthehuy.doan.model.dto.input;

import duantn.backend.model.entity.DateHeper;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ArticleUpdateDTO {
    private Integer articleId;
    private String title;
    private String content;
    private String image;
    private int roomPrice;
    private String description;
    private String phone;
    private Boolean status;
    private Date postTime= DateHeper.now();
    private Date expiryDate;
    private boolean isVip;

}
