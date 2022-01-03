package com.stackabuse.multitenantjpaservice.repository;

import com.stackabuse.multitenantjpaservice.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByKey(String key);
}
