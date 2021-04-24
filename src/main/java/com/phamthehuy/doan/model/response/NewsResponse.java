package com.phamthehuy.doan.model.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class NewsResponse {
    private Integer newId;

    private String title;

    private String slug;

    private String content;

    private List<String> images;

    private String author;

    private Date timeUpdated;

    private Boolean deleted;
}
