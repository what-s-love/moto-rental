package ge.tsepesh.motorental.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = {
            "/css/**",
            "/js/**",
            "/images/**",
            "/webjars/**",
            "/admin/login"
    };

    /**
     * URL, которые не должны попадать в поисковую выдачу: админка (включая пагинацию и редиректы на логин),
     * страницы бронирования/оплаты и JSON-эндпоинты календаря.
     * Для них отдаётся заголовок X-Robots-Tag независимо от шаблона и кода ответа.
     */
    private static final RequestMatcher NO_INDEX_URLS = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher("/admin/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/ride/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/booking/**"),
            PathPatternRequestMatcher.withDefaults().matcher("/calendar/api/**")
    );


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/booking/payment/return")
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )
            .headers(headers -> headers
                .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                        NO_INDEX_URLS, new StaticHeadersWriter("X-Robots-Tag", "noindex, nofollow")))
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").authenticated()
                .requestMatchers(WHITE_LIST_URL).permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}