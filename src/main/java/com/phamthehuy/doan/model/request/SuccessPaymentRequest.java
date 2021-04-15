package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SuccessPaymentRequest {
    @NotBlank(message = "Mã thanh toán không được trống")
    @NotNull(message = "Mã thanh toán không được trống")
    private String paymentId;

    @NotBlank(message = "Mã người thanh toán không được trống")
    @NotNull(message = "Mã người thanh toán không được trống")
    private String payerId;

    @NotBlank(message = "Mã người thanh toán không được trống")
    @NotNull(message = "Mã giao dịch không được trống")
    private String token;
}
