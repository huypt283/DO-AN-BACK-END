package com.phamthehuy.doan.model.response;

import com.phamthehuy.doan.entity.Roommate;
import com.phamthehuy.doan.entity.RoomService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;
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

    private RoomService roomService;

    private Roommate roommate;

    private Double acreage;

    private List<String> images;

    private String video;

    private Map<String, String> customer;

    private Map<String, String> moderator;

    private Map<String, String> location;

    private Boolean deleted;
}
