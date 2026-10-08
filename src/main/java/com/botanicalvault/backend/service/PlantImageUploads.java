package com.botanicalvault.backend.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.request.PlantImageRequestDTO;
import com.botanicalvault.backend.exception.BusinessException;
import com.botanicalvault.backend.util.PngFiles;
import com.botanicalvault.backend.util.Texts;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PlantImageUploads {
    private static final String FALLBACK_FILENAME = "image.png";

    public List<PendingImage> read(List<PlantImageRequestDTO> images, List<MultipartFile> files) {
        if (files == null || files.size() != images.size()) {
            throw new BusinessException(ErrorCode.IMAGE_FILES_MISMATCH, "Each image needs exactly one file");
        }
        return IntStream.range(0, images.size())
                .mapToObj(index -> toPendingImage(images.get(index), files.get(index)))
                .toList();
    }

    private PendingImage toPendingImage(PlantImageRequestDTO image, MultipartFile file) {
        String filename = filenameOf(file);
        byte[] content = contentOf(file);
        if (!PngFiles.hasPngSignature(content)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_FORMAT, "Only PNG images are accepted: " + filename);
        }
        return new PendingImage(content, filename, image.viewId());
    }

    private static String filenameOf(MultipartFile file) {
        String original = Texts.trimToNull(file.getOriginalFilename());
        if (original == null) {
            return FALLBACK_FILENAME;
        }
        Path name = Path.of(original).getFileName();
        return name == null ? FALLBACK_FILENAME : name.toString();
    }

    private static byte[] contentOf(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to read uploaded file", ex);
        }
    }
}
