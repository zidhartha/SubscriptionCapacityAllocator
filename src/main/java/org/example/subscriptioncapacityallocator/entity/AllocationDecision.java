package org.example.subscriptioncapacityallocator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "allocation_decision")
@NoArgsConstructor

public class AllocationDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long decisionId;

    @Column(name = "request_id",nullable = false,unique = true)
    private UUID requestId;

    @Column(name  = "max_capacity",nullable = false)
    private int maxCapacity;

    @Column(name = "total_requested_amount",nullable = false)
    private int totalRequestedAmount;

    @Column(name = "total_fee_revenue",nullable = false)
    private int totalFeeRevenue;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "allocationDecision",cascade = CascadeType.ALL)
    private List<AcceptedSubscription> acceptedSubscriptions;
}
