package org.example.subscriptioncapacityallocator.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// This is basically a dto for the whole request,the SubscriptionRequestDto is a subset of this.
@Getter
@Setter
public class OptimizeRequestDto {
    @PositiveOrZero(message = "Maximum capacity must be non negative")
    private int maxCapacity;

    @NotNull(message="Available subscriptions must not be null")
    @Valid
    private List<SubscriptionRequestDto> availableSubscriptions;

}
