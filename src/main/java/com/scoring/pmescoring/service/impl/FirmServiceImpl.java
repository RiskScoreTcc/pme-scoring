package com.scoring.pmescoring.service.impl;

import com.scoring.pmescoring.dto.request.firm.FirmRequest;
import com.scoring.pmescoring.dto.request.firm.UpdateFirmRequest;
import com.scoring.pmescoring.dto.response.firm.FirmResponse;
import com.scoring.pmescoring.service.FirmService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FirmServiceImpl implements FirmService {

    @Override
    @Transactional
    public FirmResponse create(FirmRequest firmRequest) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long aLong) {

    }

    @Override
    @Transactional(readOnly = true)
    public FirmResponse findById(Long aLong) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FirmResponse> findAll(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public FirmResponse update(Long aLong, UpdateFirmRequest updateFirmRequest) {
        return null;
    }
}
