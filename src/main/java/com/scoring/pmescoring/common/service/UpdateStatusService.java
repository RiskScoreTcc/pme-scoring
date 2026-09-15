package com.scoring.pmescoring.common.service;

public interface UpdateStatusService<ID, Response> {
    Response updateStatus(ID id);
}
