package com.scoring.pmescoring.service;

import com.scoring.pmescoring.common.service.CrudService;
import com.scoring.pmescoring.common.service.UpdateStatusService;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceFilter;
import com.scoring.pmescoring.dto.request.defaultoccurrence.DefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.request.defaultoccurrence.UpdateDefaultOccurrenceRequest;
import com.scoring.pmescoring.dto.response.defaultoccurrence.DefaultOccurrenceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DefaultOccurrenceService extends CrudService<Long, DefaultOccurrenceRequest, UpdateDefaultOccurrenceRequest, DefaultOccurrenceResponse>,
        UpdateStatusService<Long, DefaultOccurrenceResponse> {
    Page<DefaultOccurrenceResponse> findAllByFilter(DefaultOccurrenceFilter filter, Pageable pageable);
}
