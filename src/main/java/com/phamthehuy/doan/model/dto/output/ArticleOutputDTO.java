package com.phamthehuy.doan.model.dto.output;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ArticleOutputDTO {
    private Integer articleId;
    private Boolean deleted;
    private String title;
    private String content;
    private String image;
    private int roomPrice;
    private String description;
    private String phone;
    private Boolean status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date postTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private Date expiryDate;
    private boolean isVip;
}
