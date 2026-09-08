package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.mapper.WeightConfigurationMapper;
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

    public WeightConfigurationServiceImpl(WeightConfigurationRepository weightConfigurationRepository, WeightConfigurationMapper weightConfigurationMapper) {
        this.weightConfigurationRepository = weightConfigurationRepository;
        this.weightConfigurationMapper = weightConfigurationMapper;
    }

    @Override
    @Transactional
    public WeightConfigurationResponse create(WeightConfigurationRequest weightConfigurationRequest) {
        weightConfigurationRepository.findByActiveTrue().forEach(config -> {
            config.delete();
            weightConfigurationRepository.save(config);
        });

        WeightConfiguration newConfiguration = new WeightConfiguration(weightConfigurationRequest.formulaType(), weightConfigurationRequest.revenueWeight(), weightConfigurationRequest.timeWeight(), weightConfigurationRequest.defaultWeight(), weightConfigurationRequest.maxRevenueReference(), weightConfigurationRequest.maxTimeReferenceMonths());

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
    public WeightConfigurationResponse update(Long id, UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        WeightConfiguration weightConfiguration = weightConfigurationRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new IllegalArgumentException("weight configuration not found with ID: " + id));

        weightConfiguration.delete();
        weightConfigurationRepository.save(weightConfiguration);

        WeightConfiguration weightConfigurationUpdate = new WeightConfiguration(updateWeightConfigurationRequest.formulaType(), updateWeightConfigurationRequest.revenueWeight(), updateWeightConfigurationRequest.timeWeight(), updateWeightConfigurationRequest.defaultWeight(), updateWeightConfigurationRequest.maxRevenueReference(), updateWeightConfigurationRequest.maxTimeReferenceMonths());
        weightConfigurationRepository.save(weightConfigurationUpdate);
        return weightConfigurationMapper.toResponse(weightConfigurationUpdate);
    }
}
