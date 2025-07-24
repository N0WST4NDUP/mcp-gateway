package dev.n0wst4ndup.mcp.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  
  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    http
      .csrf(csrf -> csrf.disable())
      .authorizeExchange(exchange ->
        exchange
        .pathMatchers("/").permitAll()
        .anyExchange().authenticated())
      .oauth2Login(Customizer.withDefaults());
    
    return http.build();
  }

}
