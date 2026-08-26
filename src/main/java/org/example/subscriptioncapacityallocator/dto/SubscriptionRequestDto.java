package org.example.subscriptioncapacityallocator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequestDto {
    @NotBlank(message = "Investor name must not be blank")
    private String investorName;
    @Positive(message="Requested amount must be positive")
    private int requestedAmount;
    @Positive(message="Fee revenue must be positive")
    private int feeRevenue;

}
