package org.example.subscriptioncapacityallocator.algorithm;

import org.example.subscriptioncapacityallocator.exceptions.InvalidAllocationInputException;
import org.example.subscriptioncapacityallocator.model.SubscriptionRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllocationAlgorithm {

    public AllocationResult solve(List<SubscriptionRequest> requests, int maxCapacity) {
        validate(requests,maxCapacity);
        int n = requests.size();
        int[] amounts = new int[n];
        int[] fees = new int[n];
        for (int i = 0; i < n; i++) {
            fees[i] = requests.get(i).getFeeRevenue();
            amounts[i] = requests.get(i).getRequestedAmount();
        }
        int[][] dp = new int[n + 1][maxCapacity + 1];
        for (int i = 1; i <= n; i++) {
            for (int c = 0; c <= maxCapacity; c++) {
                dp[i][c] = dp[i - 1][c]; //we skip and do not take this
                if (amounts[i - 1] <= c) {
                    //we take this.
                    dp[i][c] = Math.max(dp[i][c], dp[i - 1][c - amounts[i - 1]] + fees[i - 1]);
                }
            }
        }
        List<SubscriptionRequest> selected = new ArrayList<>();
        int c = maxCapacity;
        for(int i = n;i>0;i--){
            if(dp[i][c] != dp[i-1][c]){ // this item was taken since the consequent dp index has a changed value
                selected.add(requests.get(i - 1));
                c -= amounts[i - 1];
            }
        }
        int totalFee = 0;
        int totalAmount = 0;
        for(SubscriptionRequest sr : selected){
            totalFee += sr.getFeeRevenue();
            totalAmount += sr.getRequestedAmount();
        }

        return new AllocationResult(selected,totalAmount,totalFee);
    }


    public void validate(List<SubscriptionRequest> requests,int maxCapacity){
        if(maxCapacity < 0){
            throw new InvalidAllocationInputException("Subscription maximum capacity must be a valid number");
        }
        if(requests == null){
            throw new InvalidAllocationInputException("Requests must not be null");
        }
        for(SubscriptionRequest sr : requests){
            if(sr.getRequestedAmount() <= 0){
                throw new InvalidAllocationInputException("Requested amount must be valid for: " + sr.getInvestorName());
            }
            if(sr.getFeeRevenue() <= 0){
                throw new InvalidAllocationInputException("Fee amount must be valid for: " + sr.getInvestorName());
            }
        }
    }
}
