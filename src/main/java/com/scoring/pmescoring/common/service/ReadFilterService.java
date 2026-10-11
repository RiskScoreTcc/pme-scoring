package com.scoring.pmescoring.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReadFilterService<Response, Filter> {

    Page<Response> findAllFilter (Filter filter, Pageable pageable);
}
