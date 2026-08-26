package org.example.subscriptioncapacityallocator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.subscriptioncapacityallocator.dto.OptimizeRequestDto;
import org.example.subscriptioncapacityallocator.dto.OptimizeResponseDto;
import org.example.subscriptioncapacityallocator.service.SubscriptionOptimizationService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionOptimizationService subscriptionOptimizationService;

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeResponseDto> optimize(@Valid @RequestBody OptimizeRequestDto dto){
        OptimizeResponseDto optimizeResponseDto = subscriptionOptimizationService.optimize(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(optimizeResponseDto);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<OptimizeResponseDto> getByRequestId(@PathVariable UUID requestId){
        return ResponseEntity.ok(subscriptionOptimizationService.getByRequestId(requestId));
    }
    @GetMapping
    public ResponseEntity<Page<OptimizeResponseDto>> getAll(Pageable pageable){
        Page<OptimizeResponseDto> response = subscriptionOptimizationService.getAll(pageable);
        return ResponseEntity.ok(response);
    }

}
