package com.botanicalvault.backend;

import com.botanicalvault.backend.repository.UsageCount;

public record TestUsage(Long id, long count) implements UsageCount {

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public long getCount() {
        return count;
    }
}
