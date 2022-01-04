package com.stackabuse.multitenantjpaservice.repository;

import com.stackabuse.multitenantjpaservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u from User u WHERE u.tenantKey = :tenantKey")
    Optional<List<User>> findByTenantKey(String tenantKey);
}
