package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.smartspend.security.JwtAuthenticationFilter;
import com.smartspend.security.SmartSpendSecConfig;

class SmartSpendSecConfigTest {

    @Test
    void shouldCreateDaoAuthenticationProviderWithConfiguredDependencies() {
        SmartSpendSecConfig config = new SmartSpendSecConfig();
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        ReflectionTestUtils.setField(config, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(config, "passwordEncoder", passwordEncoder);
        ReflectionTestUtils.setField(config, "jwtAuthenticationFilter", mock(JwtAuthenticationFilter.class));

        AuthenticationProvider provider = config.authenticationProvider();

        assertNotNull(provider);
        assertTrue(provider instanceof DaoAuthenticationProvider);
    }

    @Test
    void shouldReturnAuthenticationManagerFromConfiguration() throws Exception {
        SmartSpendSecConfig config = new SmartSpendSecConfig();
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = mock(AuthenticationManager.class);

        when(authConfig.getAuthenticationManager()).thenReturn(manager);

        AuthenticationManager result = config.authenticationManager(authConfig);

        assertSame(manager, result);
    }
}
