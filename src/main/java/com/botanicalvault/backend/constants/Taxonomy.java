package com.botanicalvault.backend.constants;

public enum Taxonomy {
    ORIGIN("Origin", true),
    PLANT_TYPE("Plant type", false),
    SIZE("Size", false),
    IMAGE_VIEW("Image view", false);

    private final String resourceName;
    private final boolean allowsUnassign;

    Taxonomy(String resourceName, boolean allowsUnassign) {
        this.resourceName = resourceName;
        this.allowsUnassign = allowsUnassign;
    }

    public String resourceName() {
        return resourceName;
    }

    public boolean allowsUnassign() {
        return allowsUnassign;
    }
}
