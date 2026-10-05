package com.scoring.pmescoring.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

//Utilitários compartilhados pelos testes de controller.

final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    static MockMvc standalone(Object controller) {
        return MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    static void bindCurrentRequest(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    static void clearCurrentRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    static <T> Page<T> emptyPage() {
        return new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    }
}
