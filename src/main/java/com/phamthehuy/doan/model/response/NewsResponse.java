package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsResponse {
    private Integer newId;

    private String title;

    private String content;

    private String image;

    private String author;

    private Long updateTime;
}
