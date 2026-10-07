package com.botanicalvault.backend.mapper;

import com.botanicalvault.backend.dto.response.SizeResponseDTO;
import com.botanicalvault.backend.dto.response.TaxonomyItemResponseDTO;
import com.botanicalvault.backend.entity.Size;
import com.botanicalvault.backend.entity.TaxonomyItem;

public class TaxonomyMapper {
    public static TaxonomyItemResponseDTO toItemResponse(TaxonomyItem item) {
        return new TaxonomyItemResponseDTO(item.getId(), item.getName());
    }

    public static SizeResponseDTO toSizeResponse(Size size) {
        return new SizeResponseDTO(size.getId(), size.getName(), size.getDescription());
    }
}
