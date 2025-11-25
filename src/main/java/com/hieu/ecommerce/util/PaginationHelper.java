package com.hieu.ecommerce.util;

import com.hieu.ecommerce.model.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public class PaginationHelper {

    public static <T> PageResponse<T> toPaginatedResponse(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
