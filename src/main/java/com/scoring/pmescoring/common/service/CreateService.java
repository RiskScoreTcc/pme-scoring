package com.scoring.pmescoring.common.service;

public interface CreateService<CreateRequest, Response> {
    Response create(CreateRequest request);
}
