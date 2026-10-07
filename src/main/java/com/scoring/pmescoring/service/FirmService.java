package com.scoring.pmescoring.service;

import com.scoring.pmescoring.common.service.CrudService;
import com.scoring.pmescoring.common.service.ReadFilterService;
import com.scoring.pmescoring.common.service.ReadMetricsService;
import com.scoring.pmescoring.dto.request.firm.FirmFilter;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmDetailResponse;
import com.scoring.pmescoring.dto.response.firm.FirmMetricsResponse;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;


public interface FirmService extends CrudService<Long, FirmRequest, UpdateFirmRequest, FirmResponse>, ReadFilterService<FirmDetailResponse, FirmFilter>, ReadMetricsService<FirmMetricsResponse> {
}
