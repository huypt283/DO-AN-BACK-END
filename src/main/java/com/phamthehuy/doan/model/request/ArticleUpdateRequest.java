package com.phamthehuy.doan.model.request;

import com.phamthehuy.doan.model.enums.RoomType;
import com.phamthehuy.doan.validation.ValidAddress;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ArticleUpdateRequest {
    @Size(min = 5, max = 220, message = "Tiêu đề phải có 5-220 kí tự")
    @NotNull(message = "Tiêu đề không được trống")
    private String title;

    @Min(value = 100000, message = "Giá phòng nhỏ nhất là 100000 đồng")
    @NotNull(message = "Giá phòng không được trống")
    private Integer roomPrice;

    @NotNull
    private RoomType roomType;

    private String description;

    @NotNull(message = "Vip không được trống")
    private Boolean vip;

    @NotNull(message = "Giá nước không được trống")
    @Min(value = 1000, message = "Giá nước nhỏ nhất là 1000 đồng")
    private Integer waterPrice;

    @NotNull(message = "Giá điện không được trống")
    @Min(value = 1000, message = "Giá điện nhỏ nhất là 1000 đồng")
    private Integer electricPrice;

    @Min(value = 1000, message = "Giá wifi nhỏ nhất là 1000 đồng")
    private Integer wifiPrice;

    @NotNull(message = "Diện tích không được trống")
    @Min(value = 5, message = "Diện tích nhỏ nhất là 5m2")
    private Double acreage;

    @NotNull(message = "Địa chỉ không được trống")
    @ValidAddress
    private String address;

    @NotBlank(message = "Ảnh không được trống")
    @NotNull(message = "Ảnh không được trống")
    private String images;

    private String video;

    @Valid
    private RoommateRequest roommateRequest;

    @NotNull(message = "Phường không được trống")
    @Min(value = 1, message = "Mã phường không hợp lệ")
    private Integer wardId;
}
