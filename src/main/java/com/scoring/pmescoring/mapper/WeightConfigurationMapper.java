package com.scoring.pmescoring.mapper;

import com.scoring.pmescoring.common.mapper.GenericMapper;
import com.scoring.pmescoring.domain.WeightConfiguration;
import com.scoring.pmescoring.dto.request.weightconfiguration.WeightConfigurationRequest;
import com.scoring.pmescoring.dto.response.weightconfiguration.WeightConfigurationResponse;
import org.springframework.stereotype.Component;

@Component
public class WeightConfigurationMapper implements GenericMapper<WeightConfigurationRequest, WeightConfigurationResponse, WeightConfiguration> {

    @Override
    public WeightConfiguration toEntity(WeightConfigurationRequest request) {
        return new WeightConfiguration(
                request.formulaType(),
                request.revenueWeight(),
                request.timeWeight(),
                request.defaultWeight(),
                request.maxRevenueReference(),
                request.maxTimeReferenceMonths(),
                request.lowRiskThreshold(),
                request.mediumRiskThreshold()
        );
    }

    @Override
    public WeightConfigurationResponse toResponse(WeightConfiguration entity) {
        return new WeightConfigurationResponse(
                entity.getId(),
                entity.getFormulaType(),
                entity.getRevenueWeight(),
                entity.getTimeWeight(),
                entity.getDefaultWeight(),
                entity.getMaxRevenueReference(),
                entity.getMaxTimeReferenceMonths(),
                entity.getLowRiskThreshold(),
                entity.getMediumRiskThreshold(),
                entity.getActive(),
                entity.getCreationDate(),
                entity.getUpdatedByUser().getId()
        );
    }
}