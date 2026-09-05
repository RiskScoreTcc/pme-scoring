package com.scoring.pmescoring.common.service;

public interface UpdateService <ID, UpdateRequest, Response>{
    Response update(ID id, UpdateRequest request);
}
