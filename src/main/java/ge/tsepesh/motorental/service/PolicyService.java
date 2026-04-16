package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.PolicyCreateDto;
import ge.tsepesh.motorental.dto.PolicyDto;
import ge.tsepesh.motorental.model.Policy;
import ge.tsepesh.motorental.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;

    public Policy getActivePolicy() {
        return policyRepository.findTopByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Active policy not found"));
    }

    @Transactional
    public Policy createNewPolicyVersion(PolicyCreateDto dto) {
        // Deactivate current active policy
        policyRepository.deactivateAllPolicies();

        // Create new active policy
        Policy newPolicy = new Policy();
        newPolicy.setText(dto.getText());
        newPolicy.setVersion(dto.getVersion());
        newPolicy.setCreatedAt(LocalDateTime.now());
        newPolicy.setIsActive(true);

        return policyRepository.save(newPolicy);
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