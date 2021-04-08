package com.phamthehuy.doan.model.response;

import com.phamthehuy.doan.entity.Roommate;
import com.phamthehuy.doan.entity.RoomService;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class ArticleResponse {
    private Integer articleId;

    private String title;

    private String slug;

    private Integer roomPrice;

    private String description;

    private Date createTime;

    private Date lastUpdateTime;

    private Date expDate;

    private Boolean vip;

    private String status;

    private String city;

    private String district;

    private String ward;

    private RoomService roomService;

    private Roommate roommate;

    private Integer acreage;

    private String image;

    private String video;

    private Map<String, String> customer;

    private Map<String, String> moderator;

    private Map<String, String> location;
}
