package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
public class CustomerUpdateRequest {
    @Size(min = 3, max = 50, message = "Tên phải có 3-50 kí tự")
    @NotNull(message = "Tên không được trống")
    private String name;

    @NotNull(message = "Giới tính không được trống")
    private Boolean gender;

    private String address;

    @Size(min = 9, max = 11, message = "Số điện thoại phải có 9-11 kí tự")
    @NotNull(message = "Số điện thoại không được null")
    private String phone;

    @Size(min = 9, max = 12, message = "Số CMND phải có 9-12 kí tự")
    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    private Date birthday;
}
