package com.scoring.pmescoring.common.util;

import org.springframework.data.domain.Pageable;

public interface PageableSanitizer {
    Pageable sanitize(Pageable pageable);
    Pageable sanitize(Pageable pageable, int maxPageSize);
}
