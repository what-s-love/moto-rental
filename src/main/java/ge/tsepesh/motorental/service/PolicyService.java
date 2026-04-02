package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.PolicyDto;
import ge.tsepesh.motorental.model.Policy;
import ge.tsepesh.motorental.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    public Policy getActivePolicy() {
        return policyRepository.findTopByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Active policy not found"));
    }

    private PolicyDto mapToPolicyDto(Policy policy) {
        return PolicyDto.builder()
//                .id(policy.getId())
                .text(policy.getText())
                .version(policy.getVersion())
                .createdAt(policy.getCreatedAt())
//                .isActive(policy.getIsActive())
                .build();
    }
}