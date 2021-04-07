package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RoommateRequest {
    @NotNull(message = "Giới tính không được trống")
    private Boolean gender;

    @NotNull(message = "Số lượng người ở chung không được null")
    @Min(value = 3, message = "Số lượng người ở chung nhỏ nhất là 1")
    private Integer quantity;

    private String description;
}
