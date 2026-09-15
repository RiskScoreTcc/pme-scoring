package com.scoring.pmescoring.common.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReadService <ID, Response>{
    Response findById(ID id);
    Page<Response> findAll(Pageable pageable);
}
