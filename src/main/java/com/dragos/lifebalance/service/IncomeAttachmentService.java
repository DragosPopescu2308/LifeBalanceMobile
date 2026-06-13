package com.dragos.lifebalance.service;

import com.dragos.lifebalance.dto.IncomeAttachmentResponseDto;
import com.dragos.lifebalance.entity.Income;
import com.dragos.lifebalance.entity.IncomeAttachment;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.repository.IncomeAttachmentRepository;
import com.dragos.lifebalance.repository.IncomeRepository;
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
public class IncomeAttachmentService {

    private final IncomeRepository incomeRepository;
    private final IncomeAttachmentRepository attachmentRepository;
    private final Path uploadRoot;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public IncomeAttachmentService(
            IncomeRepository incomeRepository,
            IncomeAttachmentRepository attachmentRepository,
            @Value("${app.upload-dir:uploads}") String uploadDir
    ) {
        this.incomeRepository = incomeRepository;
        this.attachmentRepository = attachmentRepository;
        this.uploadRoot = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    @Transactional
    public IncomeAttachmentResponseDto upload(
            User user,
            Integer incomeId,
            MultipartFile file
    ) {
        validateFile(file);

        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new NotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Income not found");
        }

        Path incomeDirectory = uploadRoot
                .resolve("income-attachments")
                .resolve(String.valueOf(user.getId()))
                .resolve(String.valueOf(incomeId));

        try {
            Files.createDirectories(incomeDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        String originalName = safeFilename(file.getOriginalFilename());
        String extension = guessExtension(originalName, file.getContentType());
        String storedName = UUID.randomUUID() + extension;

        Path targetPath = incomeDirectory
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

        IncomeAttachment attachment = new IncomeAttachment();

        attachment.setIncome(income);
        attachment.setOriginalFilename(originalName);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setUploadedAt(LocalDateTime.now());

        Path relativePath = uploadRoot.relativize(targetPath);

        attachment.setFilePath(
                relativePath.toString().replace("\\", "/")
        );

        IncomeAttachment savedAttachment =
                attachmentRepository.save(attachment);

        return mapToDto(savedAttachment);
    }

    @Transactional
    public List<IncomeAttachmentResponseDto> listByIncome(
            User user,
            Integer incomeId
    ) {
        Income income = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new NotFoundException("Income not found"));

        if (!income.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Income not found");
        }

        List<IncomeAttachment> attachments =
                attachmentRepository.findByIncome_Id(incomeId);

        List<IncomeAttachmentResponseDto> result = new ArrayList<>();

        for (IncomeAttachment attachment : attachments) {
            result.add(mapToDto(attachment));
        }

        return result;
    }

    @Transactional
    public void delete(User user, Integer attachmentId) {
        IncomeAttachment attachment = getForUser(user, attachmentId);

        Path physicalPath = uploadRoot
                .resolve(attachment.getFilePath())
                .normalize();

        try {
            Files.deleteIfExists(physicalPath);
        } catch (IOException ignored) {
        }

        attachmentRepository.delete(attachment);
    }

    @Transactional
    public IncomeAttachment getForUser(
            User user,
            Integer attachmentId
    ) {
        IncomeAttachment attachment = attachmentRepository
                .findByIdWithIncomeAndUser(attachmentId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));

        if (!attachment.getIncome().getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Attachment not found");
        }

        return attachment;
    }

    public Path resolvePhysicalPath(User user, Integer attachmentId) {
        IncomeAttachment attachment = getForUser(user, attachmentId);

        return uploadRoot
                .resolve(attachment.getFilePath())
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

    private IncomeAttachmentResponseDto mapToDto(
            IncomeAttachment attachment
    ) {
        IncomeAttachmentResponseDto dto =
                new IncomeAttachmentResponseDto();

        dto.setId(attachment.getId());
        dto.setIncomeId(attachment.getIncome().getId());
        dto.setFilePath(attachment.getFilePath());
        dto.setOriginalFilename(attachment.getOriginalFilename());
        dto.setContentType(attachment.getContentType());
        dto.setFileSize(attachment.getFileSize());
        dto.setUploadedAt(attachment.getUploadedAt());

        return dto;
    }
}