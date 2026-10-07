package com.botanicalvault.backend.exception;

import com.botanicalvault.backend.constants.ErrorCode;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, resource + " not found: " + id);
    }
}
