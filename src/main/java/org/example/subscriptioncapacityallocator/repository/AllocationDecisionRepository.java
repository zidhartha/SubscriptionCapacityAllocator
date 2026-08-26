package org.example.subscriptioncapacityallocator.repository;

import org.example.subscriptioncapacityallocator.entity.AllocationDecision;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllocationDecisionRepository extends JpaRepository<AllocationDecision,Long> {
    Optional<AllocationDecision> findByRequestId(UUID requestId);
    Page<AllocationDecision> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
