package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class ContactCustomerRequest {
    @Size(min = 3, max = 220, message = "Tiêu đề phải có từ 3-220 kí tự")
    @NotNull(message = "Tiêu đề không được trống")
    String title;

    @Size(min = 10, message = "Nội dung phải có ít nhất 10 kí tự")
    @NotNull(message = "Nội dung không được trống")
    String content;
}
