package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.ConsentDto;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Client;
import ge.tsepesh.motorental.model.Consent;
import ge.tsepesh.motorental.model.Policy;
import ge.tsepesh.motorental.repository.ConsentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsentService {
    private final ConsentRepository consentRepository;
    @Transactional
    public Consent createConsent(Client client, Policy policy, Booking booking) {
        LocalDateTime now = LocalDateTime.now();
        String hash = computeHash(client.getId(), policy.getId(), policy.getVersion(), now);
        Consent consent = new Consent();
        consent.setClient(client);
        consent.setPolicy(policy);
        consent.setBooking(booking);
        consent.setHash(hash);
        consent.setCreatedAt(now);
        Consent saved = consentRepository.save(consent);
        log.info("Consent recorded: client={}, policy={}, hash={}",
                client.getId(), policy.getVersion(), hash);
        return saved;
    }
    private String computeHash(Integer clientId, Integer policyId,
                               String version, LocalDateTime timestamp) {
        String raw = clientId + ":" + policyId + ":" + version + ":" + timestamp;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }

    private ConsentDto mapToConsentDto(Consent consent) {
        return ConsentDto.builder()
                .id(consent.getId())
                .clientId(consent.getClient().getId())
                .policyId(consent.getPolicy().getId())
                .bookingId(consent.getBooking().getId())
                .hash(consent.getHash())
                .createdAt(consent.getCreatedAt())
                .build();
    }
}