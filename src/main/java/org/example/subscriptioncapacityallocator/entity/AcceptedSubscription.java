package org.example.subscriptioncapacityallocator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name= "accepted_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class AcceptedSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "investor_name",nullable = false)
    private String investorName;

    @Column(name = "requested_amount",nullable = false)
    private int requestedAmount;

    @Column(name="fee_revenue",nullable = false)
    private int feeRevenue;

    @ManyToOne
    @JoinColumn(name="decision_id",nullable = false)
    private AllocationDecision allocationDecision;
}
