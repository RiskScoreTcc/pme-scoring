package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.common.exception.BusinessException;
import com.scoring.pmescoring.common.exception.ResourceNotFoundException;
import com.scoring.pmescoring.common.util.PageableSanitizer;
import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.mapper.WeightConfigurationMapper;
import com.scoring.pmescoring.model.EntityStatus;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.repository.WeightConfigurationRepository;
import com.scoring.pmescoring.service.WeightConfigurationService;
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
public class WeightConfigurationServiceImpl implements WeightConfigurationService {

    private final WeightConfigurationRepository weightConfigurationRepository;
    private final WeightConfigurationMapper weightConfigurationMapper;
    private final UserRepository userRepository;
    private final PageableSanitizer pageableSanitizer;

    @Override
    @Transactional
    public WeightConfigurationResponse create(WeightConfigurationRequest request) {
        Long userID = request.updatedByUserId();
        log.info("Initiating weight configuration creation process requested by User ID: {}", userID);

        if (request.lowRiskThreshold() <= request.mediumRiskThreshold()) {
            log.warn("Business rule violation: Attempted to create configuration where low risk threshold ({}) is not greater than medium risk threshold ({})", request.lowRiskThreshold(), request.mediumRiskThreshold());
            throw new BusinessException("Low risk threshold must be greater than medium risk threshold.");
        }

        User user = userRepository.findByIdAndStatus(userID, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Creation failed. Active user not found with ID: {}", userID);
                    return new ResourceNotFoundException("User not found with ID: " + userID);
                });

        List<WeightConfiguration> activeConfigs = weightConfigurationRepository.findByStatus(EntityStatus.ACTIVE);
        for (WeightConfiguration config : activeConfigs) {
            config.delete();
            weightConfigurationRepository.save(config);
        }
        if (!activeConfigs.isEmpty()) {
            log.info("Deactivated {} previously active weight configurations to maintain single active state", activeConfigs.size());
        }

        WeightConfiguration newConfiguration = new WeightConfiguration(
                request.formulaType(),
                request.revenueWeight(),
                request.timeWeight(),
                request.defaultWeight(),
                request.maxRevenueReference(),
                request.maxTimeReferenceMonths(),
                request.lowRiskThreshold(),
                request.mediumRiskThreshold(),
                user
        );

        WeightConfiguration savedConfiguration = weightConfigurationRepository.save(newConfiguration);
        log.info("New weight configuration successfully created and activated with ID: {}", savedConfiguration.getId());

        return weightConfigurationMapper.toResponse(savedConfiguration);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Initiating logical deletion for weight configuration ID: {}", id);

        WeightConfiguration weightConfiguration = weightConfigurationRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Deletion failed. Active weight configuration not found with ID: {}", id);
                    return new ResourceNotFoundException("weight configuration not found with ID: " + id);
                });

        long activeConfigurationsCount = weightConfigurationRepository.countByStatus(EntityStatus.ACTIVE);
        if (activeConfigurationsCount <= 1) {
            log.warn("Business rule violation: Attempted to delete the only active weight configuration ID: {}", id);
            throw new BusinessException("Cannot delete the only active weight configuration. At least one active configuration must remain in the system.");
        }

        weightConfiguration.delete();
        weightConfigurationRepository.save(weightConfiguration);

        log.info("Weight configuration ID: {} successfully marked as deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public WeightConfigurationResponse findById(Long id) {
        log.info("Fetching weight configuration with ID: {}", id);

        WeightConfiguration weightConfiguration = weightConfigurationRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Fetch failed. Weight configuration not found with ID: {}", id);
                    return new ResourceNotFoundException("weight configuration not found with ID: " + id);
                });

        return weightConfigurationMapper.toResponse(weightConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightConfigurationResponse> findAll(Pageable pageable) {
        log.info("Fetching paginated weight configurations (including inactive)");

        pageable = pageableSanitizer.sanitize(pageable);
        Page<WeightConfiguration> weightConfigurationPage = weightConfigurationRepository.findAll(pageable);

        return weightConfigurationPage.map(weightConfigurationMapper::toResponse);
    }

    @Override
    @Transactional
    public WeightConfigurationResponse update(Long id, UpdateWeightConfigurationRequest request) {
        log.info("Initiating update process for weight configuration ID: {}", id);

        WeightConfiguration existingConfiguration = weightConfigurationRepository.findByIdAndStatus(id, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Update failed. Active weight configuration not found with ID: {}", id);
                    return new ResourceNotFoundException("weight configuration not found with ID: " + id);
                });

        Long userID = request.updatedByUserId();

        var formulaType = request.formulaType() != null ? request.formulaType() : existingConfiguration.getFormulaType();
        var revenueWeight = request.revenueWeight() != null ? request.revenueWeight() : existingConfiguration.getRevenueWeight();
        var timeWeight = request.timeWeight() != null ? request.timeWeight() : existingConfiguration.getTimeWeight();
        var defaultWeight = request.defaultWeight() != null ? request.defaultWeight() : existingConfiguration.getDefaultWeight();
        var maxRevenueReference = request.maxRevenueReference() != null ? request.maxRevenueReference() : existingConfiguration.getMaxRevenueReference();
        var maxTimeReferenceMonths = request.maxTimeReferenceMonths() != null ? request.maxTimeReferenceMonths() : existingConfiguration.getMaxTimeReferenceMonths();
        var lowRiskThreshold = request.lowRiskThreshold() != null ? request.lowRiskThreshold() : existingConfiguration.getLowRiskThreshold();
        var mediumRiskThreshold = request.mediumRiskThreshold() != null ? request.mediumRiskThreshold() : existingConfiguration.getMediumRiskThreshold();

        if (lowRiskThreshold <= mediumRiskThreshold) {
            log.warn("Business rule violation during update: Low risk threshold ({}) is not greater than medium risk threshold ({})", lowRiskThreshold, mediumRiskThreshold);
            throw new BusinessException("Low risk threshold must be greater than medium risk threshold.");
        }

        User user = userRepository.findByIdAndStatus(userID, EntityStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("Update failed. Active user not found with ID: {}", userID);
                    return new ResourceNotFoundException("User not found with ID: " + userID);
                });

        log.info("Deactivating current configuration ID: {} to create a new updated version", existingConfiguration.getId());
        existingConfiguration.delete();
        weightConfigurationRepository.save(existingConfiguration);

        WeightConfiguration weightConfigurationUpdate = new WeightConfiguration(
                formulaType,
                revenueWeight,
                timeWeight,
                defaultWeight,
                maxRevenueReference,
                maxTimeReferenceMonths,
                lowRiskThreshold,
                mediumRiskThreshold,
                user
        );

        weightConfigurationRepository.save(weightConfigurationUpdate);
        log.info("Updated weight configuration successfully created and activated with new ID: {}", weightConfigurationUpdate.getId());

        return weightConfigurationMapper.toResponse(weightConfigurationUpdate);
    }
}