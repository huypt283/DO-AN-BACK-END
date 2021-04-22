package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ChangeAvatarRequest {
    @NotNull(message = "Ảnh đại diện không được trống")
    @NotBlank(message = "Ảnh đại diện không được trống")
    private String image;
}
