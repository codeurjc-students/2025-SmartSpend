package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.smartspend.user.UserController;
import com.smartspend.user.UserService;

class UserLayerSmokeTest {

    @Test
    void shouldInstantiateUserControllerAndService() {
        UserController controller = new UserController();
        UserService service = new UserService();

        assertNotNull(controller);
        assertNotNull(service);
    }
}
