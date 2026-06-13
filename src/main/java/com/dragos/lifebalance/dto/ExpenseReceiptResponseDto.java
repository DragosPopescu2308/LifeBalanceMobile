package com.dragos.lifebalance.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseReceiptResponseDto {

    private Integer id;
    private Integer expenseId;
    private String filePath;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadedAt;
}