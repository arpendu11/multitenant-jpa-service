package com.stackabuse.multitenantjpaservice.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "key")
@ToString(of = {"id", "tenantId", "key"})
@Entity
@Table(name = "TENANT", schema="public", indexes = {
        @Index(name = "tenant_unq", unique = true, columnList = "key")
})
public class Tenant implements TimeAudit {

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    @Column(name = "TENANT_ID", nullable = false)
    private Long tenantId;

    @Column(name = "KEY", nullable = false, unique = true)
    @Size(min = 1,
            max = 8,
            message = "Tenant key must be between 1 and 8 characters")
    @Pattern(regexp = "^[a-z0-9]{1,8}$",
            message = "Tenant key must be lowercase alphanumeric ASCII"
                    + " value between 1 and 8 characters inclusive")
    private String key;

    @Size(max = 30)
    @Column(name = "PASSWORD")
    private String password;

    @Size(max = 256)
    @Column(name = "URL")
    private String url;

    @Column(name = "ENABLED")
    private Boolean enabled;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_ON")
    private Long createdOn;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "LAST_UPDATED_ON")
    private Long lastUpdatedOn;

}
