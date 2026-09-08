package com.scoring.pmescoring.mapper;

import com.scoring.pmescoring.common.mapper.GenericMapper;
import com.scoring.pmescoring.domain.CalculatedScore;
import com.scoring.pmescoring.dto.request.calculatedscore.CalculatedScoreRequest;
import com.scoring.pmescoring.dto.response.calculatedscore.CalculatedScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class CalculatedScoreMapper implements GenericMapper<CalculatedScoreRequest, CalculatedScoreResponse, CalculatedScore> {
    @Override
    public CalculatedScore toEntity(CalculatedScoreRequest request) {
        return new CalculatedScore();
    }

    @Override
    public CalculatedScoreResponse toResponse(CalculatedScore entity) {
        return new CalculatedScoreResponse(entity.getId(), entity.getFirm().getId(), entity.getAnalyzedByUser().getId() ,entity.getScoreValue(), entity.getRiskBand(), entity.getActive(), entity.getFactorsJson(), entity.getCalculationDate(),
                "Este score é uma estimativa simplificada, não substituindo análise de crédito formal de instituições financeiras.");
    }
}
