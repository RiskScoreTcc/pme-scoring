package com.scoring.pmescoring.mapper;

import com.scoring.pmescoring.common.mapper.GenericMapper;
import com.scoring.pmescoring.domain.DefaultOccurrence;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import org.springframework.stereotype.Component;

@Component
public class DefaultOccurrenceMapper implements GenericMapper<DefaultOccurrenceRequest, DefaultOccurrenceResponse, DefaultOccurrence> {
    @Override
    public DefaultOccurrence toEntity(DefaultOccurrenceRequest request) {
        return new DefaultOccurrence(request.dateOccurrence(), request.amountDue(), request.description());
    }

    @Override
    public DefaultOccurrenceResponse toResponse(DefaultOccurrence entity) {
        return new DefaultOccurrenceResponse(entity.getId(), entity.getFirm().getId(), entity.getDateOccurrence(), entity.getAmountDue(), entity.getStatusResolved(), entity.getDescription(), entity.getStatus(), entity.getCreationDate());
    }
}
