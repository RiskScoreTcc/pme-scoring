package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirmServiceImpl implements FirmService {

    private final FirmRepository firmRepository;
    private final FirmMapper firmMapper;
    private final UserRepository userRepository;
    private final CalculatedScoreRepository calculatedScoreRepository;
    private final DefaultOccurrenceRepository defaultOccurrenceRepository;

    public FirmServiceImpl(FirmRepository firmRepository,
                           FirmMapper firmMapper,
                           UserRepository userRepository,
                           CalculatedScoreRepository calculatedScoreRepository,
                           DefaultOccurrenceRepository defaultOccurrenceRepository) {
        this.firmRepository = firmRepository;
        this.firmMapper = firmMapper;
        this.userRepository = userRepository;
        this.calculatedScoreRepository = calculatedScoreRepository;
        this.defaultOccurrenceRepository = defaultOccurrenceRepository;
    }

    @Override
    @Transactional
    public FirmResponse create(FirmRequest firmRequest) {
        Long idUser = firmRequest.userId();
        boolean existsFirm = firmRepository.existsByCnpjAndStatus(firmRequest.cnpj(), EntityStatus.ACTIVE);

        if (existsFirm) {
            throw new BusinessException("Firm already exists with this cnpj.");
        }
        User user = userRepository.findByIdAndStatus(idUser, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + idUser));

        Firm firm = new Firm(user, firmRequest.cnpj(), firmRequest.registeredCompanyName(), firmRequest.averageRevenue(), firmRequest.ageInMonths(), firmRequest.numberOfEmployees());
        Firm savedFirm = firmRepository.save(firm);

        return firmMapper.toResponse(savedFirm);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Firm not found with ID: " + id));

        firm.delete();
        firmRepository.save(firm);

        for (CalculatedScore score : calculatedScoreRepository.findByFirmIdAndStatus(id, EntityStatus.ACTIVE)) {
            score.delete();
            calculatedScoreRepository.save(score);
        }

        for (DefaultOccurrence occurrence : defaultOccurrenceRepository.findByFirmIdAndStatus(id, EntityStatus.ACTIVE)) {
            occurrence.delete();
            defaultOccurrenceRepository.save(occurrence);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FirmResponse findById(Long id) {
        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Firm not found with ID: " + id));

        return firmMapper.toResponse(firm);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FirmResponse> findAll(Pageable pageable) {
        Page<Firm> firmPage = firmRepository.findByStatus(EntityStatus.ACTIVE, pageable);
        return firmPage.map(firmMapper::toResponse);
    }

    @Override
    @Transactional
    public FirmResponse update(Long id, UpdateFirmRequest updateFirmRequest) {
        Firm firm = firmRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Firm not found with ID: " + id));

        if (!firm.getCnpj().equals(updateFirmRequest.cnpj())) {
            if (firmRepository.existsByCnpjAndStatus(updateFirmRequest.cnpj(), EntityStatus.ACTIVE)) {
                throw new BusinessException("This Cnpj is already in use by another user.");
            }
        }
        firm.update(updateFirmRequest.cnpj(), updateFirmRequest.ageInMonths(), updateFirmRequest.averageRevenue(), updateFirmRequest.numberOfEmployees(), updateFirmRequest.registeredCompanyName());

        firmRepository.save(firm);
        return firmMapper.toResponse(firm);
    }
}