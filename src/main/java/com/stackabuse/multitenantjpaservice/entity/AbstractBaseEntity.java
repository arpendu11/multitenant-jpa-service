package com.stackabuse.multitenantjpaservice.entity;

import com.stackabuse.multitenantjpaservice.listener.TenantListener;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(TenantListener.class)
public abstract class AbstractBaseEntity implements TenantAware, Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "TENANT_KEY", nullable = false, unique = true)
    @Size(min = 1,
            max = 8,
            message = "Tenant key must be between 1 and 8 characters")
    @Pattern(regexp = "^[a-z0-9]{1,8}$",
            message = "Tenant key must be lowercase alphanumeric ASCII"
                    + " value between 1 and 8 characters inclusive")
    private String tenantKey;

    public AbstractBaseEntity(String tenantKey) {
        this.tenantKey = tenantKey;
    }
}