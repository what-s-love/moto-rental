package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.ClientDto;
import ge.tsepesh.motorental.model.Client;
import ge.tsepesh.motorental.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    @Transactional
    public Client findOrCreate(ClientDto dto) {
        return clientRepository.findByEmailOrPhone(dto.getEmail(), dto.getPhone())
                .map(existing -> {
                    log.info("Found existing client: {}", dto.getEmail());
                    return existing;
                })
                .orElseGet(() -> {
                    Client client = new Client();
                    client.setName(dto.getName());
                    client.setEmail(dto.getEmail());
                    client.setPhone(dto.getPhone());
                    client.setTelegramId(dto.getTelegramId());
                    client.setCreatedAt(LocalDateTime.now());
                    Client saved = clientRepository.save(client);
                    log.info("Created new client: {}", saved.getEmail());
                    return saved;
                });
    }
}