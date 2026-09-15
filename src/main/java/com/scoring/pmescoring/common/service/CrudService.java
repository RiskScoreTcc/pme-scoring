package com.scoring.pmescoring.common.service;

public interface CrudService<ID, CreateRequest,UpdateRequest, Response>
        extends CreateService<CreateRequest, Response>,
        ReadService<ID, Response>,
        DeleteService<ID>,
        UpdateService<ID, UpdateRequest, Response> {
}
