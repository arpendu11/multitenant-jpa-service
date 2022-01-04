package com.stackabuse.multitenantjpaservice.listener;

import com.stackabuse.multitenantjpaservice.entity.TenantAware;
import com.stackabuse.multitenantjpaservice.util.TenantContext;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

public class TenantListener {

    @PreUpdate
    @PreRemove
    @PrePersist
    public void setTenant(TenantAware entity) {
        final String tenantId = TenantContext.getTenantId();
        entity.setTenantKey(tenantId);
    }
}
