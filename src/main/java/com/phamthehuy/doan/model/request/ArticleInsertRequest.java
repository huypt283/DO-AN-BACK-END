package com.phamthehuy.doan.model.request;

import com.phamthehuy.doan.model.enums.RoomType;
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

    @Min(value = 100000, message = "Giá phòng nhỏ nhất là 100000 VNĐ")
    @NotNull(message = "Giá phòng không được trống")
    private Integer roomPrice;

    @NotNull
    private RoomType roomType;

    private String description;

    @Min(value = 1, message = "Số ngày/tuần/tháng  nhỏ nhất là 1")
    @NotNull(message = "Số ngày/tuần/tháng không được trống")
    private Integer times;

    @NotNull(message = "Loại thời gian không được trống")
    @NotBlank(message = "Loại thời gian không được trống")
    private String timeType;

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
    @Min(value = 5, message = "Diện tích nhỏ nhất là 5 m2")
    private Double acreage;

    @NotNull(message = "Địa chỉ không được trống")
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