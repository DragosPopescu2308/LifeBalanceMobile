package com.dragos.lifebalance.controller;

import com.dragos.lifebalance.dto.IncomeAttachmentResponseDto;
import com.dragos.lifebalance.entity.IncomeAttachment;
import com.dragos.lifebalance.entity.User;
import com.dragos.lifebalance.exceptions.NotFoundException;
import com.dragos.lifebalance.service.IncomeAttachmentService;
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
public class IncomeAttachmentController {

    private final IncomeAttachmentService service;

    public IncomeAttachmentController(
            IncomeAttachmentService service
    ) {
        this.service = service;
    }

    @PostMapping("/incomes/{incomeId}/attachments")
    public ResponseEntity<IncomeAttachmentResponseDto> upload(
            Authentication authentication,
            @PathVariable Integer incomeId,
            @RequestParam("file") MultipartFile file
    ) {
        User user = (User) authentication.getPrincipal();

        IncomeAttachmentResponseDto response =
                service.upload(user, incomeId, file);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/incomes/{incomeId}/attachments")
    public ResponseEntity<List<IncomeAttachmentResponseDto>> list(
            Authentication authentication,
            @PathVariable Integer incomeId
    ) {
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                service.listByIncome(user, incomeId)
        );
    }

    @DeleteMapping("/income-attachments/{attachmentId}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Integer attachmentId
    ) {
        User user = (User) authentication.getPrincipal();

        service.delete(user, attachmentId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/income-attachments/{attachmentId}/file")
    public ResponseEntity<Resource> file(
            Authentication authentication,
            @PathVariable Integer attachmentId
    ) {
        User user = (User) authentication.getPrincipal();

        IncomeAttachment attachment =
                service.getForUser(user, attachmentId);

        Path path = service.resolvePhysicalPath(user, attachmentId);

        Resource resource = new FileSystemResource(path);

        if (!resource.exists()) {
            throw new NotFoundException("File not found");
        }

        String contentType = attachment.getContentType();

        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + attachment.getOriginalFilename() + "\""
                )
                .body(resource);
    }
}