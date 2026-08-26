package org.example.subscriptioncapacityallocator.exceptions;

import java.util.UUID;

public class AllocationDecisionNotFoundException extends RuntimeException {

    public AllocationDecisionNotFoundException(String message){
        super(message);
    }

    public AllocationDecisionNotFoundException(UUID requestId){
        super("No allocation decision found for requestId: " + requestId);
    }
}
