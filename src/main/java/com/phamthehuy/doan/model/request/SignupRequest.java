package com.phamthehuy.doan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @Size(min = 3, max = 50, message = "Tên phải có 3-50 kí tự")
    @NotNull(message = "Tên không được trống")
    private String name;

    @NotNull(message = "Giới tính không được trống")
    private Boolean gender;

    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được trống")
    @NotNull(message = "Email không được null")
    private String email;

    @Size(min = 6, max = 30, message = "Mật khẩu phải có 6-30 kí tự")
    @NotNull(message = "Mật khẩu không được trống")
    private String password;

    @Size(min = 9, max = 11, message = "Số điện thoại phải có 9-11 số")
    @NotNull(message = "Số điện thoại không được trống")
    private String phone;
}
