package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.domain.Firm;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.mapper.FirmMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.CalculatedScoreRepository;
import com.scoring.pmescoring.repository.DefaultOccurrenceRepository;
import com.scoring.pmescoring.repository.FirmRepository;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.service.FirmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirmServiceImpl implements FirmService {

    private final FirmRepository firmRepository;
    private final FirmMapper firmMapper;
    private final UserRepository userRepository;
    private final CalculatedScoreRepository calculatedScoreRepository;
    private final DefaultOccurrenceRepository defaultOccurrenceRepository;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public FirmResponse create(FirmRequest firmRequest) {
        Long idUser = firmRequest.userId();
        log.info("Initiating firm creation process for CNPJ: {}, requested by User ID: {}", firmRequest.cnpj(), idUser);

        boolean existsFirm = firmRepository.existsByCnpjAndStatus(firmRequest.cnpj(), EntityStatus.ACTIVE);

        if (existsFirm) {
            log.warn("Business rule violation: Attempted to register an already existing active CNPJ: {}", firmRequest.cnpj());
            throw new BusinessException("Firm already exists with this cnpj.");
        }

        User user = userRepository.findByIdAndStatus(idUser, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Firm creation failed. Active user not found with ID: {}", idUser);
                    return new ResourceNotFoundException("User not found with ID: " + idUser);
                });

        Firm firm = new Firm(user, firmRequest.cnpj(), firmRequest.registeredCompanyName(), firmRequest.averageRevenue(), firmRequest.ageInMonths(), firmRequest.numberOfEmployees());
        Firm savedFirm = firmRepository.save(firm);

        log.info("Firm created successfully with ID: {}, CNPJ: {}", savedFirm.getId(), savedFirm.getCnpj());
        return firmMapper.toResponse(savedFirm);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Initiating logical deletion for Firm ID: {} and its cascading dependencies", id);

        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Deletion failed. Active firm not found with ID: {}", id);
                    return new ResourceNotFoundException("Firm not found with ID: " + id);
                });

        firm.delete();
        firmRepository.save(firm);

        List<CalculatedScore> scores = calculatedScoreRepository.findByFirmIdAndStatus(id, EntityStatus.ACTIVE);
        for (CalculatedScore score : scores) {
            score.delete();
            calculatedScoreRepository.save(score);
        }
        if (!scores.isEmpty()) {
            log.info("Successfully marked {} related calculated scores as deleted for Firm ID: {}", scores.size(), id);
        }

        List<DefaultOccurrence> occurrences = defaultOccurrenceRepository.findByFirmIdAndStatus(id, EntityStatus.ACTIVE);
        for (DefaultOccurrence occurrence : occurrences) {
            occurrence.delete();
            defaultOccurrenceRepository.save(occurrence);
        }
        if (!occurrences.isEmpty()) {
            log.info("Successfully marked {} related default occurrences as deleted for Firm ID: {}", occurrences.size(), id);
        }

        log.info("Firm ID: {} cascading deletion completed successfully", id);
    }

    @Override
    @Transactional(readOnly = true)
    public FirmResponse findById(Long id) {
        log.info("Fetching firm with ID: {}", id);

        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Fetch failed. Active firm not found with ID: {}", id);
                    return new ResourceNotFoundException("Firm not found with ID: " + id);
                });

        return firmMapper.toResponse(firm);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FirmResponse> findAll(Pageable pageable) {
        log.info("Fetching paginated active firms");

        pageable = pageableSanitizer.sanitize(pageable);
        Page<Firm> firmPage = firmRepository.findByStatus(EntityStatus.ACTIVE, pageable);

        return firmPage.map(firmMapper::toResponse);
    }

    @Override
    @Transactional
    public FirmResponse update(Long id, UpdateFirmRequest updateFirmRequest) {
        log.info("Initiating update process for Firm ID: {}", id);

        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Update failed. Active firm not found with ID: {}", id);
                    return new ResourceNotFoundException("Firm not found with ID: " + id);
                });

        if (!firm.getCnpj().equals(updateFirmRequest.cnpj())) {
            if (firmRepository.existsByCnpjAndStatus(updateFirmRequest.cnpj(), EntityStatus.ACTIVE)) {
                log.warn("Business rule violation: Attempted to update Firm ID: {} with an already registered CNPJ: {}", id, updateFirmRequest.cnpj());
                throw new BusinessException("This Cnpj is already in use by another user.");
            }
        }

        firm.update(updateFirmRequest.cnpj(), updateFirmRequest.ageInMonths(), updateFirmRequest.averageRevenue(), updateFirmRequest.numberOfEmployees(), updateFirmRequest.registeredCompanyName());
        firmRepository.save(firm);

        log.info("Firm ID: {} updated successfully", id);
        return firmMapper.toResponse(firm);
    }
}