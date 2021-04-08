package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class NewsResponse {
    private Integer newId;

    private String title;

    private String content;

    private String image;

    private String author;

    private Date lastUpdatedTime;
}
