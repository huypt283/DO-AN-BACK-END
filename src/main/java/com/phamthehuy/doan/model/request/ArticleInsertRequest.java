package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
public class ArticleInsertRequest {
    @Size(min = 3, max = 220, message = "Tiêu đề phải có từ 3-220 kí tự")
    @NotNull(message = "Tiêu đề không được trống")
    private String title;

    @Min(value = 1000, message = "Giá phòng nhỏ nhất là 1000 đồng")
    @NotNull(message = "Giá phòng đăng không được trống")
    private int roomPrice;

    private String description;

    @Min(value = 1, message = "Số ngày đăng nhỏ nhất là 1")
    @NotNull(message = "Số ngày/tuần/tháng đăng không được trống")
    private Integer days;

    @NotNull(message = "type không được null")
    @NotBlank(message = "type không được trống")
    private String timeType;

    @NotNull(message = "Vip không được trống")
    private Boolean vip;

    @Min(value = 1000, message = "Giá nước nhỏ nhất là 1000 đồng")
    private Integer waterPrice;

    @Min(value = 1000, message = "Giá điện nhỏ nhất là 1000 đồng")
    private Integer electricPrice;

    @Min(value = 1000, message = "Giá wifi nhỏ nhất là 1000 đồng")
    private Integer wifiPrice;

    @NotNull(message = "Diện tích không được null")
    @Min(value = 5, message = "Diện tích nhỏ nhất là 5 m2")
    private Integer acreage;

    @NotNull(message = "Địa chỉ không được null")
    @Size(min = 3, message = "Địa chỉ phải có ít nhất 3 kí tự")
    private String address;

    @NotBlank(message = "Ảnh không được trống")
    @NotNull(message = "Ảnh không được trống")
    private String images;

    private String video;

    private RoommateRequest roommateRequest;

    @NotNull(message = "Phường không được trống")
    private Integer wardId;
}