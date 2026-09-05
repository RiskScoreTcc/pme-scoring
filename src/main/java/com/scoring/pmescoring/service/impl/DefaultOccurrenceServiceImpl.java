package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultOccurrenceServiceImpl implements DefaultOccurrenceService {
    @Override
    @Transactional
    public DefaultOccurrenceResponse create(DefaultOccurrenceRequest defaultOccurrenceRequest) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long aLong) {

    }

    @Override
    @Transactional(readOnly = true)
    public DefaultOccurrenceResponse findById(Long aLong) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DefaultOccurrenceResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse update(Long aLong, UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        return null;
    }
}
