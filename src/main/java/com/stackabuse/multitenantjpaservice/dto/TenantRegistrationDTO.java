package com.stackabuse.multitenantjpaservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class TenantRegistrationDTO {

    @NotNull
    @JsonProperty("tenant_id")
    private long tenantId;

    @NotNull
    @Size(min = 1, max = 8, message = "Tenant key must be between 1 and 8 characters")
    @Pattern(regexp = "^[a-z0-9]{1,8}$", message = "Tenant key must be lowercase alphanumeric ASCII"
            + " value between 1 and 8 characters inclusive")
    private String key;

    private Boolean enabled;
}
