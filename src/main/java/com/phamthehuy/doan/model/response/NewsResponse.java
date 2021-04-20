package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class NewsResponse {
    private Integer newId;

    private String title;

    private String slug;

    private String content;

    private String image;

    private String author;

    private Date timeUpdated;

    private Boolean deleted;
}
