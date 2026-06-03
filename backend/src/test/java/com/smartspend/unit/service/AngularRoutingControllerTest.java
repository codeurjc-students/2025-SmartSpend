package com.smartspend.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.smartspend.config.AngularRoutingController;

class AngularRoutingControllerTest {

    @Test
    void shouldForwardToAngularIndex() {
        AngularRoutingController controller = new AngularRoutingController();

        String result = controller.forwardToAngularIndex();

        assertEquals("forward:/index.html", result);
    }
}
