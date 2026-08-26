package org.example.subscriptioncapacityallocator.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OptimizeResponseDto {
    private UUID requestId;
    private List<SubscriptionRequestDto> acceptedSubscriptions;
    private int totalRequestedAmount;
    private int totalFeeRevenue;
    private LocalDateTime createdAt;

}
