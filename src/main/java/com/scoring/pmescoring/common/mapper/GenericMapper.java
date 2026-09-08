package com.scoring.pmescoring.common.mapper;

import java.util.List;

public interface GenericMapper<CreateRequest, Response, Entity>{

    Entity toEntity(CreateRequest request);

    Response toResponse(Entity entity);

}
