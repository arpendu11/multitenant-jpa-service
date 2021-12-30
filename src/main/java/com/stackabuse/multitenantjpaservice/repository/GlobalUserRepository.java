package com.stackabuse.multitenantjpaservice.repository;

import com.stackabuse.multitenantjpaservice.entity.GlobalUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GlobalUserRepository extends JpaRepository<GlobalUser, UUID> {
}
