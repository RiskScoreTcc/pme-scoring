package com.scoring.pmescoring.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class DefaultPageableSanitizer implements PageableSanitizer {

    private static final int DEFAULT_MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_MIN_PAGE_SIZE = 1;

    @Override
    public Pageable sanitize(Pageable pageable) {
        return sanitize(pageable, DEFAULT_MAX_PAGE_SIZE);
    }

    @Override
    public Pageable sanitize(Pageable pageable, int maxPageSize) {
        if (pageable == null) {
            return PageRequest.of(0, 10);
        }

        int safePage = Math.max(0, pageable.getPageNumber());
        int safeSize = Math.clamp(pageable.getPageSize(), DEFAULT_MIN_PAGE_SIZE, maxPageSize);

        return PageRequest.of(safePage, safeSize, pageable.getSort());
    }
}