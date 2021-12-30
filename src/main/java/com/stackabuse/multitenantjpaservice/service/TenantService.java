package com.stackabuse.multitenantjpaservice.service;

import com.stackabuse.multitenantjpaservice.dto.TenantRegistrationDTO;
import com.stackabuse.multitenantjpaservice.entity.Tenant;

import java.util.List;

/**
 * Tenant Registration and Namespace Management Service
 */
public interface TenantService {

    boolean isTenantExists(String tenantKey);

    /**
     * Tenant Registration Layer
     *
     * Description: This service will register Tenant in the global namespace,
     * create tenant based namespace and migrate required objects to the newly
     * created tenant namespace.
     */
    Tenant registerTenant(TenantRegistrationDTO tenantRegistrationDTO) throws Exception;

    /**
     * Tenant Fetch Layer
     *
     * Description: This service will fetch all the tenants being registered along
     * with the activation and deactivation information.
     */
    List<Tenant> getAllTenants();

    /**
     * Tenant Deactivation Layer
     *
     * Description: This service will deactivate Tenant and restrict access to its
     * namespace contents.
     */
    Boolean deactivateTenant(String tenantKey) throws Exception;

    /**
     * Tenant Activation Layer
     *
     * Description: This service will activate a deactivated tenant being registered and
     * regain all the access to its namespace.
     */
    Boolean activateTenant(String tenantKey) throws Exception;

    /**
     * Tenant Fetch Layer
     *
     * Description: This service will fetch a tenant by tenant key that is being registered along
     * with the activation and deactivation information.
     */
    Tenant findTenant(String key);
}

