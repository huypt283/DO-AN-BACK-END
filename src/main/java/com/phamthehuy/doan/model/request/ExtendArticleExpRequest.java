package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ExtendArticleExpRequest {
    @NotNull
    @Min(value = 1, message = "Số ngày/tuần/tháng nhỏ nhất là 1")
    private Integer times;

    @NotNull(message = "Loại thời gian không được trống")
    @NotBlank(message = "Loại thời gian không được trống")
    private String timeType;
}
