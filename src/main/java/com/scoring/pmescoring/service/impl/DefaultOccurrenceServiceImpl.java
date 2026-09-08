package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.mapper.DefaultOccurrenceMapper;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DefaultOccurrenceServiceImpl implements DefaultOccurrenceService {

    private final DefaultOccurrenceRepository defaultOccurrenceRepository;
    private final FirmRepository firmRepository;
    private final DefaultOccurrenceMapper defaultOccurrenceMapper;

    public DefaultOccurrenceServiceImpl(DefaultOccurrenceRepository defaultOccurrenceRepository, FirmRepository firmRepository, DefaultOccurrenceMapper defaultOccurrenceMapper) {
        this.defaultOccurrenceRepository = defaultOccurrenceRepository;
        this.firmRepository = firmRepository;
        this.defaultOccurrenceMapper = defaultOccurrenceMapper;
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse create(DefaultOccurrenceRequest defaultOccurrenceRequest) {
        Long firmID = defaultOccurrenceRequest.firmId();

        Firm firm = firmRepository.findByIdAndActiveTrue(firmID)
                .orElseThrow(() -> new IllegalArgumentException("Firm not found with ID: " + firmID));

        DefaultOccurrence defaultOccurrence = new DefaultOccurrence(firm, defaultOccurrenceRequest.dateOccurrence(), defaultOccurrenceRequest.amountDue(), defaultOccurrenceRequest.description());
        defaultOccurrenceRepository.save(defaultOccurrence);

        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Default Occurrence not found with ID: " + id));

        defaultOccurrence.delete();
        defaultOccurrenceRepository.save(defaultOccurrence);
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultOccurrenceResponse findById(Long id) {
        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Default Occurrence not found with ID: " + id));

        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DefaultOccurrenceResponse> findAll(Pageable pageable) {
        Page<DefaultOccurrence> defaultOccurrencePage = defaultOccurrenceRepository.findByActiveTrue(pageable);
        return defaultOccurrencePage.map(defaultOccurrenceMapper::toResponse);
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse update(Long id, UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Default Occurrence not found with ID: " + id));

        defaultOccurrence.update(updateDefaultOccurrenceRequest.amountDue(), updateDefaultOccurrenceRequest.dateOccurrence(), updateDefaultOccurrenceRequest.description());
        defaultOccurrenceRepository.save(defaultOccurrence);

        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse updateStatus(Long id) {
        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Default Occurrence not found with ID: " + id));

        defaultOccurrence.setStatusResolved(true);

        defaultOccurrenceRepository.save(defaultOccurrence);
        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }
}
