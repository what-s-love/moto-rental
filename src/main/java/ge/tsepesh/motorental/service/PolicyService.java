package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.policy.PolicyAdminDto;
import ge.tsepesh.motorental.dto.policy.PolicyCreateDto;
import ge.tsepesh.motorental.dto.policy.PolicyDto;
import ge.tsepesh.motorental.model.Policy;
import ge.tsepesh.motorental.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;

    public Policy getActivePolicy() {
        return policyRepository.findTopByIsActiveTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new IllegalStateException("Active policy not found"));
    }

    public PolicyDto getChosenPolicyDto(Integer id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Policy not found"));
        return mapToPolicyDto(policy);
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

    public List<PolicyAdminDto> getAllPolicies() {
        return policyRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToPolicyAdminDto)
                .toList();
    }

    private PolicyDto mapToPolicyDto(Policy policy) {
        return PolicyDto.builder()
                .id(policy.getId())
                .text(policy.getText())
                .version(policy.getVersion())
                .createdAt(policy.getCreatedAt())
                .isActive(policy.getIsActive())
                .build();
    }

    private PolicyAdminDto mapToPolicyAdminDto(Policy policy) {
        return PolicyAdminDto.builder()
                .id(policy.getId())
                .version(policy.getVersion())
                .createdAt(policy.getCreatedAt())
                .isActive(policy.getIsActive())
                .build();
    }
}