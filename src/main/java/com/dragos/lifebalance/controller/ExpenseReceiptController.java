package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.ExpenseReceiptResponseDto;
import com.dragos.lifebalance.entity.ExpenseReceipt;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.service.ExpenseReceiptService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExpenseReceiptController {

    private final ExpenseReceiptService receiptService;

    public ExpenseReceiptController(
            ExpenseReceiptService receiptService
    ) {
        this.receiptService = receiptService;
    }

    @PostMapping("/expenses/{expenseId}/receipts")
    public ResponseEntity<ExpenseReceiptResponseDto> upload(
            Authentication authentication,
            @PathVariable Integer expenseId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = (User) authentication.getPrincipal();

        ExpenseReceiptResponseDto saved =
                receiptService.upload(user, expenseId, file);

        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/expenses/{expenseId}/receipts")
    public ResponseEntity<List<ExpenseReceiptResponseDto>> list(
            Authentication authentication,
            @PathVariable Integer expenseId
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                receiptService.listByExpense(user, expenseId)
        );
    }

    @DeleteMapping("/receipts/{receiptId}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer receiptId
    ) {
        User user = (User) authentication.getPrincipal();

        receiptService.delete(user, receiptId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/receipts/{receiptId}/file")
    public ResponseEntity<Resource> download(
            Authentication authentication,
            @PathVariable Integer receiptId
    ) {
        User user = (User) authentication.getPrincipal();

        ExpenseReceipt receipt =
                receiptService.getReceiptForUser(user, receiptId);

        Path path = receiptService.resolvePhysicalPath(user, receiptId);

        Resource resource = new FileSystemResource(path);

        if (!resource.exists()) {
            throw new NotFoundException("File not found");
        }

        String contentType = receipt.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + receipt.getOriginalFilename() + "\""
                )
                .body(resource);
    }
}