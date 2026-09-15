package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.request.calculatedscore.UpdateCalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import com.scoring.pmescoring.service.CalculatedScoreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CalculatedScoreServiceImpl implements CalculatedScoreService {
    @Override
    @Transactional
    public CalculatedScoreResponse create(CalculatedScoreRequest calculatedScoreRequest) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long aLong) {

    }

    @Override
    @Transactional(readOnly = true)
    public CalculatedScoreResponse findById(Long aLong) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CalculatedScoreResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public CalculatedScoreResponse update(Long aLong, UpdateCalculatedScoreRequest updateCalculatedScoreRequest) {
        return null;
    }
}
