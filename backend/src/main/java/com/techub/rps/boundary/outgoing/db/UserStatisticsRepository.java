package com.techub.rps.boundary.outgoing.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatisticsRepository extends JpaRepository<UserStatisticsEntity, Long> {
    Optional<UserStatisticsEntity> findByUserId(Long userId);
}
