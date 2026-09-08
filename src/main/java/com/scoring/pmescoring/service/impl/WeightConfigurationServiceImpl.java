package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.domain.User;
import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.mapper.WeightConfigurationMapper;
import com.scoring.pmescoring.repository.UserRepository;
import com.scoring.pmescoring.repository.WeightConfigurationRepository;
import com.scoring.pmescoring.service.WeightConfigurationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightConfigurationServiceImpl implements WeightConfigurationService {

    private final WeightConfigurationRepository weightConfigurationRepository;
    private final WeightConfigurationMapper weightConfigurationMapper;
    private final UserRepository userRepository;

    public WeightConfigurationServiceImpl(WeightConfigurationRepository weightConfigurationRepository, WeightConfigurationMapper weightConfigurationMapper, UserRepository userRepository) {
        this.weightConfigurationRepository = weightConfigurationRepository;
        this.weightConfigurationMapper = weightConfigurationMapper;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public WeightConfigurationResponse create(WeightConfigurationRequest request) {
        Long UserID = request.updatedByUserId();

        if (request.lowRiskThreshold() <= request.mediumRiskThreshold()) {
            throw new IllegalArgumentException("Low risk threshold must be greater than medium risk threshold.");
        }

        User user = userRepository.findByIdAndActiveTrue(UserID)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + UserID));

        weightConfigurationRepository.findByActiveTrue().forEach(config -> {
            config.delete();
            weightConfigurationRepository.save(config);
        });

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

        return weightConfigurationMapper.toResponse(savedConfiguration);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WeightConfiguration weightConfiguration = weightConfigurationRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("weight configuration not found with ID: " + id));

        long activeConfigurationsCount = weightConfigurationRepository.countByActiveTrue();
        if (activeConfigurationsCount <= 1) {
            throw new IllegalStateException("Cannot delete the only active weight configuration. At least one active configuration must remain in the system.");
        }
        weightConfiguration.delete();

        weightConfigurationRepository.save(weightConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public WeightConfigurationResponse findById(Long id) {
        WeightConfiguration weightConfiguration = weightConfigurationRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("weight configuration not found with ID: " + id));
        return weightConfigurationMapper.toResponse(weightConfiguration);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightConfigurationResponse> findAll(Pageable pageable) {
        Page<WeightConfiguration> weightConfigurationPage = weightConfigurationRepository.findAll(pageable);
        return weightConfigurationPage.map(weightConfigurationMapper::toResponse);
    }

    @Override
    @Transactional
    public WeightConfigurationResponse update(Long id, UpdateWeightConfigurationRequest request) {
        WeightConfiguration existingConfiguration = weightConfigurationRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("weight configuration not found with ID: " + id));
        Long UserID = request.updatedByUserId();

        var formulaType = request.formulaType() != null ? request.formulaType() : existingConfiguration.getFormulaType();
        var revenueWeight = request.revenueWeight() != null ? request.revenueWeight() : existingConfiguration.getRevenueWeight();
        var timeWeight = request.timeWeight() != null ? request.timeWeight() : existingConfiguration.getTimeWeight();
        var defaultWeight = request.defaultWeight() != null ? request.defaultWeight() : existingConfiguration.getDefaultWeight();
        var maxRevenueReference = request.maxRevenueReference() != null ? request.maxRevenueReference() : existingConfiguration.getMaxRevenueReference();
        var maxTimeReferenceMonths = request.maxTimeReferenceMonths() != null ? request.maxTimeReferenceMonths() : existingConfiguration.getMaxTimeReferenceMonths();
        var lowRiskThreshold = request.lowRiskThreshold() != null ? request.lowRiskThreshold() : existingConfiguration.getLowRiskThreshold();
        var mediumRiskThreshold = request.mediumRiskThreshold() != null ? request.mediumRiskThreshold() : existingConfiguration.getMediumRiskThreshold();

        if (lowRiskThreshold <= mediumRiskThreshold) {
            throw new IllegalArgumentException("Low risk threshold must be greater than medium risk threshold.");
        }

        User user = userRepository.findByIdAndActiveTrue(UserID)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + UserID));


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
        return weightConfigurationMapper.toResponse(weightConfigurationUpdate);
    }
}