package com.smartspend.unit.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

import com.smartspend.security.WebConfig;

class WebConfigTest {

    @Test
    void shouldRegisterSpaForwardRoutes() {
        WebConfig config = new WebConfig();
        ViewControllerRegistry registry = mock(ViewControllerRegistry.class);
        ViewControllerRegistration rootRegistration = mock(ViewControllerRegistration.class);
        ViewControllerRegistration oneLevelRegistration = mock(ViewControllerRegistration.class);
        ViewControllerRegistration twoLevelRegistration = mock(ViewControllerRegistration.class);

        when(registry.addViewController("/")).thenReturn(rootRegistration);
        when(registry.addViewController("/{path:[^\\.]*}")).thenReturn(oneLevelRegistration);
        when(registry.addViewController("/{path1:.*}/{path2:[^\\.]*}")).thenReturn(twoLevelRegistration);

        config.addViewControllers(registry);

        verify(registry).addViewController("/");
        verify(registry).addViewController("/{path:[^\\.]*}");
        verify(registry).addViewController("/{path1:.*}/{path2:[^\\.]*}");
        verify(rootRegistration).setViewName("forward:/index.html");
        verify(oneLevelRegistration).setViewName("forward:/index.html");
        verify(twoLevelRegistration).setViewName("forward:/index.html");
    }
}
