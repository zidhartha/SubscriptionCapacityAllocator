package org.example.subscriptioncapacityallocator.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.subscriptioncapacityallocator.model.SubscriptionRequest;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AllocationResult {
    List<SubscriptionRequest> acceptedSubscriptions;
    int totalRequestedAmount;
    int totalFeeRevenue;
}
