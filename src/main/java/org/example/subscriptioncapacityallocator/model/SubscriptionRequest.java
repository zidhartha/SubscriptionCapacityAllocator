package org.example.subscriptioncapacityallocator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {
    String investorName;
    int requestedAmount;
    int feeRevenue;
}
