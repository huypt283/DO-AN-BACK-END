package com.phamthehuy.doan.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BlockArticleRequest {
    @Size(min = 10, message = "Lý do phải có ít nhất 10 kí tự")
    @NotNull(message = "Lý do không được trống")
    private String reason;
}
