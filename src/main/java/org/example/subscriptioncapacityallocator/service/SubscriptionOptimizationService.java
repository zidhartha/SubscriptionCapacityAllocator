package org.example.subscriptioncapacityallocator.service;


import lombok.RequiredArgsConstructor;
import org.example.subscriptioncapacityallocator.algorithm.AllocationAlgorithm;
import org.example.subscriptioncapacityallocator.algorithm.AllocationResult;
import org.example.subscriptioncapacityallocator.dto.OptimizeRequestDto;
import org.example.subscriptioncapacityallocator.dto.OptimizeResponseDto;
import org.example.subscriptioncapacityallocator.dto.SubscriptionRequestDto;
import org.example.subscriptioncapacityallocator.entity.AcceptedSubscription;
import org.example.subscriptioncapacityallocator.entity.AllocationDecision;
import org.example.subscriptioncapacityallocator.exceptions.AllocationDecisionNotFoundException;
import org.example.subscriptioncapacityallocator.model.SubscriptionRequest;
import org.example.subscriptioncapacityallocator.repository.AllocationDecisionRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionOptimizationService {
    private final AllocationAlgorithm allocationAlgorithm;
    private final AllocationDecisionRepository allocationDecisionRepository;

    public OptimizeResponseDto optimize(OptimizeRequestDto dto){
        //Firstly we must map the incoming dtos to model entities in order to run the algorithm on them.
        List<SubscriptionRequest> requests = dto.getAvailableSubscriptions().stream().map(
                r -> new SubscriptionRequest(r.getInvestorName(),r.getRequestedAmount(),r.getFeeRevenue())
        ).toList();

        //now we run the algorithm
        AllocationResult result = allocationAlgorithm.solve(requests,dto.getMaxCapacity());

        //the logic for building the entity in order to persist it later
        AllocationDecision decision = new AllocationDecision();
        decision.setRequestId(UUID.randomUUID());
        decision.setMaxCapacity(dto.getMaxCapacity());
        decision.setTotalFeeRevenue(result.getTotalFeeRevenue());
        decision.setTotalRequestedAmount(result.getTotalRequestedAmount());
        decision.setCreatedAt(LocalDateTime.now());

        List<AcceptedSubscription> acceptedSubscriptions =
                    result.getAcceptedSubscriptions().stream().map(
                            r -> {
                                AcceptedSubscription accepted = new AcceptedSubscription();
                                accepted.setAllocationDecision(decision);
                                accepted.setFeeRevenue(r.getFeeRevenue());
                                accepted.setRequestedAmount(r.getRequestedAmount());
                                accepted.setInvestorName(r.getInvestorName());
                                return accepted;
                            }
                    ).toList();
        //save it to db
        decision.setAcceptedSubscriptions(acceptedSubscriptions);
        AllocationDecision saved = allocationDecisionRepository.save(decision);

        return toDto(saved);
    }

    public OptimizeResponseDto getByRequestId(UUID requestId){
        AllocationDecision decision = allocationDecisionRepository.findByRequestId(requestId)
                .orElseThrow(() -> new AllocationDecisionNotFoundException(requestId));
        return toDto(decision);

    }

    public Page<OptimizeResponseDto> getAll(Pageable pageable){
        return allocationDecisionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toDto);
    }

    private OptimizeResponseDto toDto(AllocationDecision decision){
        List<SubscriptionRequestDto> accepted = decision.getAcceptedSubscriptions().stream().map(
                s -> new SubscriptionRequestDto(s.getInvestorName(),s.getRequestedAmount(),s.getFeeRevenue()
                )
        ).toList();
        return new OptimizeResponseDto(
                decision.getRequestId(),
                accepted,
                decision.getTotalRequestedAmount(),
                decision.getTotalFeeRevenue(),
                decision.getCreatedAt()
        );
    }
}
