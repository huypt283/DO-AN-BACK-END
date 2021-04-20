package com.phamthehuy.doan.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotNull(message = "Token không được trống")
    String token;

    @NotNull(message = "Email không được trống")
    String email;

    @Size(min = 6, max = 30, message = "Mật khẩu phải có 6-30 kí tự")
    @NotNull(message = "Mật khẩu không được trống")
    String password;
}
