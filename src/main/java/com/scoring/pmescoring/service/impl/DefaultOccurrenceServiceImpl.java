package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import com.scoring.pmescoring.mapper.DefaultOccurrenceMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.service.DefaultOccurrenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefaultOccurrenceServiceImpl implements DefaultOccurrenceService {

    private final DefaultOccurrenceRepository defaultOccurrenceRepository;
    private final FirmRepository firmRepository;
    private final DefaultOccurrenceMapper defaultOccurrenceMapper;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public DefaultOccurrenceResponse create(DefaultOccurrenceRequest defaultOccurrenceRequest) {
        Long firmID = defaultOccurrenceRequest.firmId();
        log.info("Initiating creation of default occurrence for Firm ID: {}", firmID);

        Firm firm = firmRepository.findByIdAndStatus(firmID, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Creation failed. Active firm not found with ID: {}", firmID);
                    return new ResourceNotFoundException("Firm not found with ID: " + firmID);
                });

        DefaultOccurrence defaultOccurrence = new DefaultOccurrence(firm, defaultOccurrenceRequest.dateOccurrence(), defaultOccurrenceRequest.amountDue(), defaultOccurrenceRequest.description());
        defaultOccurrenceRepository.save(defaultOccurrence);

        log.info("Default occurrence created successfully. Firm ID: {}, Amount Due: {}", firmID, defaultOccurrenceRequest.amountDue());
        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Initiating logical deletion for default occurrence ID: {}", id);

        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Deletion failed. Active default occurrence not found with ID: {}", id);
                    return new ResourceNotFoundException("Default Occurrence not found with ID: " + id);
                });

        defaultOccurrence.delete();
        defaultOccurrenceRepository.save(defaultOccurrence);

        log.info("Default occurrence ID: {} successfully marked as deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DefaultOccurrenceResponse findById(Long id) {
        log.info("Fetching default occurrence with ID: {}", id);

        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Fetch failed. Active default occurrence not found with ID: {}", id);
                    return new ResourceNotFoundException("Default Occurrence not found with ID: " + id);
                });

        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DefaultOccurrenceResponse> findAll(Pageable pageable) {
        log.info("Fetching paginated active default occurrences");

        pageable = pageableSanitizer.sanitize(pageable);
        Page<DefaultOccurrence> defaultOccurrencePage = defaultOccurrenceRepository.findByStatus(EntityStatus.ACTIVE, pageable);

        return defaultOccurrencePage.map(defaultOccurrenceMapper::toResponse);
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse update(Long id, UpdateDefaultOccurrenceRequest updateDefaultOccurrenceRequest) {
        log.info("Initiating update for default occurrence ID: {}", id);

        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Update failed. Active default occurrence not found with ID: {}", id);
                    return new ResourceNotFoundException("Default Occurrence not found with ID: " + id);
                });

        defaultOccurrence.update(updateDefaultOccurrenceRequest.amountDue(), updateDefaultOccurrenceRequest.dateOccurrence(), updateDefaultOccurrenceRequest.description());
        defaultOccurrenceRepository.save(defaultOccurrence);

        log.info("Default occurrence ID: {} updated successfully", id);
        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }

    @Override
    @Transactional
    public DefaultOccurrenceResponse updateStatus(Long id) {
        log.info("Initiating resolution status update for default occurrence ID: {}", id);

        DefaultOccurrence defaultOccurrence = defaultOccurrenceRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Status update failed. Active default occurrence not found with ID: {}", id);
                    return new ResourceNotFoundException("Default Occurrence not found with ID: " + id);
                });

        defaultOccurrence.setStatusResolved(true);
        defaultOccurrenceRepository.save(defaultOccurrence);

        log.info("Default occurrence ID: {} successfully marked as resolved", id);
        return defaultOccurrenceMapper.toResponse(defaultOccurrence);
    }
}