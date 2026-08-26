package org.example.subscriptioncapacityallocator;


import org.example.subscriptioncapacityallocator.algorithm.AllocationAlgorithm;
import org.example.subscriptioncapacityallocator.algorithm.AllocationResult;
import org.example.subscriptioncapacityallocator.exceptions.InvalidAllocationInputException;
import org.example.subscriptioncapacityallocator.model.SubscriptionRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AllocationAlgorithmTest {

    private final AllocationAlgorithm algorithm = new AllocationAlgorithm();

    @Test
    void selectsOptimalCombinationWithinCapacity() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 5, 120),
                new SubscriptionRequest("Investor B", 10, 200),
                new SubscriptionRequest("Investor C", 3, 80),
                new SubscriptionRequest("Investor D", 8, 160)
        );

        AllocationResult result = algorithm.solve(requests, 15);

        assertThat(result.getTotalRequestedAmount()).isEqualTo(15);
        assertThat(result.getTotalFeeRevenue()).isEqualTo(320);
        assertThat(result.getAcceptedSubscriptions())
                .extracting(SubscriptionRequest::getInvestorName)
                .containsExactlyInAnyOrder("Investor A", "Investor B");
    }

    @Test
    void returnsEmptyResultWhenListIsEmpty() {
        AllocationResult result = algorithm.solve(List.of(), 15);

        assertThat(result.getAcceptedSubscriptions()).isEmpty();
        assertThat(result.getTotalFeeRevenue()).isZero();
        assertThat(result.getTotalRequestedAmount()).isZero();
    }

    @Test
    void returnsEmptyResultWhenNothingFits() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 50, 100)
        );

        AllocationResult result = algorithm.solve(requests, 10);

        assertThat(result.getAcceptedSubscriptions()).isEmpty();
        assertThat(result.getTotalFeeRevenue()).isZero();
    }

    @Test
    void selectsSingleItemExactlyMatchingCapacity() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 15, 100)
        );

        AllocationResult result = algorithm.solve(requests, 15);

        assertThat(result.getAcceptedSubscriptions()).hasSize(1);
        assertThat(result.getTotalFeeRevenue()).isEqualTo(100);
        assertThat(result.getTotalRequestedAmount()).isEqualTo(15);
    }

    @Test
    void handlesZeroCapacity() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 5, 100)
        );

        AllocationResult result = algorithm.solve(requests, 0);

        assertThat(result.getAcceptedSubscriptions()).isEmpty();
    }

    @Test
    void provesRealOptimizationNotGreedy() {
        // Best single fee is B (fee 100), but A+C together beat it (60+60=120)
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 6, 60),
                new SubscriptionRequest("Investor B", 10, 100),
                new SubscriptionRequest("Investor C", 4, 60)
        );

        AllocationResult result = algorithm.solve(requests, 10);

        assertThat(result.getTotalFeeRevenue()).isEqualTo(120);
        assertThat(result.getAcceptedSubscriptions())
                .extracting(SubscriptionRequest::getInvestorName)
                .containsExactlyInAnyOrder("Investor A", "Investor C");
    }

    @Test
    void allItemsFitWithinCapacity() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 2, 10),
                new SubscriptionRequest("Investor B", 3, 20)
        );

        AllocationResult result = algorithm.solve(requests, 100);

        assertThat(result.getAcceptedSubscriptions()).hasSize(2);
        assertThat(result.getTotalRequestedAmount()).isEqualTo(5);
        assertThat(result.getTotalFeeRevenue()).isEqualTo(30);
    }

    @Test
    void throwsOnNullRequestList() {
        assertThrows(InvalidAllocationInputException.class,
                () -> algorithm.solve(null, 15));
    }

    @Test
    void throwsOnNegativeCapacity() {
        assertThrows(InvalidAllocationInputException.class,
                () -> algorithm.solve(List.of(), -5));
    }

    @Test
    void throwsOnNonPositiveRequestedAmount() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 0, 100)
        );
        assertThrows(InvalidAllocationInputException.class,
                () -> algorithm.solve(requests, 15));
    }

    @Test
    void throwsOnNegativeFeeRevenue() {
        List<SubscriptionRequest> requests = List.of(
                new SubscriptionRequest("Investor A", 5, -1)
        );
        assertThrows(InvalidAllocationInputException.class,
                () -> algorithm.solve(requests, 15));
    }
}
