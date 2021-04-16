package com.phamthehuy.doan.model.request;

import com.phamthehuy.doan.validation.ValidBirthday;
import com.phamthehuy.doan.validation.ValidNumber;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Getter
@Setter
public class StaffUpdateRequest {
    @Size(min = 3, max = 50, message = "Tên phải có từ 3 đến 50 kí tự")
    @NotNull(message = "Tên không được trống")
    private String name;

    @Size(min = 9, max = 12, message = "Số CMND phải có từ 9-12 kí tự")
    @NotNull(message = "Số CMND không được trống")
    @ValidNumber(message = "Số CMND phải là số")
    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    @ValidBirthday(message = "Ngày sinh không hợp lệ")
    private Date birthday;

    @NotNull(message = "Giới tính không được trống")
    private Boolean gender;

    @NotNull(message = "Vai trò không được trống")
    private Boolean role;

    @NotBlank(message = "Địa chỉ không được trống")
    @NotNull(message = "Địa chỉ không được trống")
    private String address;

    @Size(min = 9, max = 11, message = "Số điện thoại phải có 9-11 kí tự")
    @NotNull(message = "Số điện thoại không được trống")
    @ValidNumber(message = "Số điện thoại phải là số")
    private String phone;
}
