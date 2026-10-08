package com.botanicalvault.backend.controller;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.exception.ResourceNotFoundException;
import com.botanicalvault.backend.storage.LocalFileUrlSigner;
import com.botanicalvault.backend.storage.LocalImageStorage;
import com.botanicalvault.backend.storage.SignedFileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalFileController {
    private final LocalFileUrlSigner signer;
    private final LocalImageStorage storage;

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> read(
            @PathVariable String key,
            @RequestParam long expires,
            @RequestParam String signature) {
        if (!signer.isValid(new SignedFileRequest(key, expires, signature))) {
            throw new BusinessException(ErrorCode.INVALID_FILE_SIGNATURE, "File link is invalid or expired");
        }
        if (!storage.exists(key)) {
            throw new ResourceNotFoundException("File", key);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(signer.remainingValidity(expires)).cachePrivate())
                .body(storage.read(key));
    }
}
