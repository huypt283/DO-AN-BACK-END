package com.phamthehuy.doan.model.request;

import com.phamthehuy.doan.validation.ValidBirthday;
import com.phamthehuy.doan.validation.ValidNumber;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Validated
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
    @NotNull(message = "Số điện thoại không được trống")
    @ValidNumber(message = "Số điện thoại phải là số")
    private String phone;

    @Size(min = 9, max = 12, message = "Số CMND phải có 9-12 kí tự")
    @NotNull(message = "Số CMND không được trống")
    @ValidNumber(message = "Số CMND phải là số")
    private String cardId;

    @NotNull(message = "Ngày sinh không được trống")
    @ValidBirthday(message = "Ngày sinh không hợp lệ")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;
}
