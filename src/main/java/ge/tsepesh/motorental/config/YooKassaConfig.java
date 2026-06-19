package ge.tsepesh.motorental.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@EnableConfigurationProperties(YooKassaProperties.class)
public class YooKassaConfig {

    @Bean
    public RestClient yooKassaRestClient(YooKassaProperties props) {
        return RestClient.builder()
                .baseUrl(props.apiUrl())
                .defaultHeaders(headers -> {
                    headers.setBasicAuth(props.shopId(), props.secretKey());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                })
                .build();
    }
}
