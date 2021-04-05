package com.phamthehuy.doan.model.dto.output;

import com.phamthehuy.doan.model.entity.Roommate;
import com.phamthehuy.doan.model.entity.Service;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ArticleOutputDTO {
    private Integer articleId;

    private String title;

    private String image;

    private Integer roomPrice;

    private String description;

    private Long createTime;

    private Long lastUpdateTime;

    private Long expDate;

    private Boolean vip;

    private String status;

    private Service service;

    private Roommate roommate;

    private Integer acreage;

    private String address;

    private String video;

    private Map<String, String> customer;

    private Map<String, String> moderator;

    private Map<String, String> location;
    //private Ward ward;
}
