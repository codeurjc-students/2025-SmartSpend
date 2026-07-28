package com.smartspend.version;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VersionController {

    @Value("${app.version}")
    private String appVersion;

    @GetMapping("/version")
    public ResponseEntity<VersionDto> getVersion() {
        return ResponseEntity.ok(new VersionDto(appVersion));
    }
}
