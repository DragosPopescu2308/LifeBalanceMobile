package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.ExpenseReceiptResponseDto;
import com.dragos.lifebalance.entity.Expense;
import com.dragos.lifebalance.entity.ExpenseReceipt;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.ExpenseReceiptRepository;
import com.dragos.lifebalance.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ExpenseReceiptService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseReceiptRepository receiptRepository;
    private final Path uploadRoot;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public ExpenseReceiptService(
            ExpenseRepository expenseRepository,
            ExpenseReceiptRepository receiptRepository,
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.expenseRepository = expenseRepository;
        this.receiptRepository = receiptRepository;
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public ExpenseReceiptResponseDto upload(
            User user,
            Integer expenseId,
            MultipartFile file
    ) {
        validateFile(file);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Expense not found");
        }

        Path expenseDirectory = uploadRoot
                .resolve("receipts")
                .resolve(String.valueOf(user.getId()))
                .resolve(String.valueOf(expenseId));

        try {
            Files.createDirectories(expenseDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        String originalName = safeFilename(file.getOriginalFilename());
        String extension = guessExtension(originalName, file.getContentType());
        String storedName = UUID.randomUUID() + extension;

        Path targetPath = expenseDirectory
                .resolve(storedName)
                .normalize();

        try {
            Files.copy(
                    file.getInputStream(),
                    targetPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("Could not save file", e);
        }

        ExpenseReceipt receipt = new ExpenseReceipt();

        receipt.setExpense(expense);
        receipt.setOriginalFilename(originalName);
        receipt.setContentType(file.getContentType());
        receipt.setFileSize(file.getSize());
        receipt.setUploadedAt(LocalDateTime.now());

        Path relativePath = uploadRoot.relativize(targetPath);

        receipt.setFilePath(
                relativePath.toString().replace("\\", "/")
        );

        ExpenseReceipt savedReceipt =
                receiptRepository.save(receipt);

        return mapToDto(savedReceipt);
    }

    @Transactional
    public List<ExpenseReceiptResponseDto> listByExpense(
            User user,
            Integer expenseId
    ) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new NotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Expense not found");
        }

        List<ExpenseReceipt> receipts =
                receiptRepository.findByExpense_Id(expenseId);

        List<ExpenseReceiptResponseDto> result = new ArrayList<>();

        for (ExpenseReceipt receipt : receipts) {
            result.add(mapToDto(receipt));
        }

        return result;
    }

    @Transactional
    public void delete(User user, Integer receiptId) {
        ExpenseReceipt receipt = getReceiptForUser(user, receiptId);

        Path physicalPath = uploadRoot
                .resolve(receipt.getFilePath())
                .normalize();

        try {
            Files.deleteIfExists(physicalPath);
        } catch (IOException ignored) {
        }

        receiptRepository.delete(receipt);
    }

    @Transactional
    public ExpenseReceipt getReceiptForUser(User user, Integer receiptId) {
        ExpenseReceipt receipt = receiptRepository
                .findByIdWithExpenseAndUser(receiptId)
                .orElseThrow(() -> new NotFoundException("Receipt not found"));

        if (!receipt.getExpense().getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Receipt not found");
        }

        return receipt;
    }

    public Path resolvePhysicalPath(User user, Integer receiptId) {
        ExpenseReceipt receipt = getReceiptForUser(user, receiptId);

        return uploadRoot
                .resolve(receipt.getFilePath())
                .normalize();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String contentType = file.getContentType();

        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Unsupported file type. Allowed: jpg, png, pdf"
            );
        }
    }

    private String safeFilename(String name) {
        if (name == null || name.isBlank()) {
            return "file";
        }

        name = name.replace("\\", "/");

        int index = name.lastIndexOf("/");

        if (index >= 0) {
            name = name.substring(index + 1);
        }

        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");

        if (name.length() > 200) {
            name = name.substring(name.length() - 200);
        }

        return name;
    }

    private String guessExtension(String originalName, String contentType) {
        int dot = originalName.lastIndexOf('.');

        if (dot >= 0 && dot < originalName.length() - 1) {
            return originalName.substring(dot).toLowerCase();
        }

        if ("image/jpeg".equals(contentType)) {
            return ".jpg";
        }

        if ("image/png".equals(contentType)) {
            return ".png";
        }

        if ("application/pdf".equals(contentType)) {
            return ".pdf";
        }

        return "";
    }

    private ExpenseReceiptResponseDto mapToDto(ExpenseReceipt receipt) {
        ExpenseReceiptResponseDto dto = new ExpenseReceiptResponseDto();

        dto.setId(receipt.getId());
        dto.setExpenseId(receipt.getExpense().getId());
        dto.setFilePath(receipt.getFilePath());
        dto.setOriginalFilename(receipt.getOriginalFilename());
        dto.setContentType(receipt.getContentType());
        dto.setFileSize(receipt.getFileSize());
        dto.setUploadedAt(receipt.getUploadedAt());

        return dto;
    }
}