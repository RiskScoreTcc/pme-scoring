package com.scoring.pmescoring.service;

import com.scoring.pmescoring.common.service.CrudService;
import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.model.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface FirmService extends CrudService<Long, FirmRequest, UpdateFirmRequest, FirmResponse> {
    Page<FirmResponse> searchNameOrCnpj(String query, Pageable pageable);
}
