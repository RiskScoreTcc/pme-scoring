package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.dto.request.weightconfiguration.UpdateWeightConfigurationRequest;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import com.scoring.pmescoring.service.WeightConfigurationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeightConfigurationServiceImpl implements WeightConfigurationService {
    @Override
    @Transactional
    public WeightConfigurationResponse create(WeightConfigurationRequest weightConfigurationRequest) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long aLong) {

    }

    @Override
    @Transactional(readOnly = true)
    public WeightConfigurationResponse findById(Long aLong) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WeightConfigurationResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public WeightConfigurationResponse update(Long aLong, UpdateWeightConfigurationRequest updateWeightConfigurationRequest) {
        return null;
    }
}
