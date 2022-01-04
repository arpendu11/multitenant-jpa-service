package com.stackabuse.multitenantjpaservice.entity;

public interface TenantAware {

    String getTenantKey();

    void setTenantKey(String tenantKey);
}
