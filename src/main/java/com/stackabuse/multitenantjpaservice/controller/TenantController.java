package com.stackabuse.multitenantjpaservice.controller;

import com.stackabuse.multitenantjpaservice.dto.TenantRegistrationDTO;
import com.stackabuse.multitenantjpaservice.entity.Tenant;
import com.stackabuse.multitenantjpaservice.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;


    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Tenant> registerTenant(
            @Valid @RequestBody TenantRegistrationDTO tenantRegistrationDTO) throws Exception {
        log.info("Registering newly generated tenant..");
        return new ResponseEntity<>(tenantService.registerTenant(tenantRegistrationDTO), HttpStatus.CREATED);
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tenant>> getAllTenants() {
        log.info("Fetching all the registered tenants..");
        return new ResponseEntity<>(tenantService.getAllTenants(), HttpStatus.OK);
    }

    @PutMapping(value = "/{tenantKey}/deactivate")
    public ResponseEntity<?> deactivateTenant(@PathVariable(required = true) String tenantKey) throws Exception {
        log.info("Deactivating a given tenant..");
        if (tenantService.deactivateTenant(tenantKey)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return null;
    }

    @PutMapping(value = "/{tenantKey}/activate")
    public ResponseEntity<?> activateTenant(@PathVariable String tenantKey) throws Exception {
        log.info("Activating a given tenant..");
        if (tenantService.activateTenant(tenantKey)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return null;
    }
}
