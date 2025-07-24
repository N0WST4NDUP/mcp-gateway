package dev.n0wst4ndup.mcp.gateway.handler;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UsersHandler {
  
  public Mono<ServerResponse> helloWorld(ServerRequest request) {
    log.info("일단 헬로로 접속!");
    return  ServerResponse.ok()
                          .bodyValue("Hello, World!");
  }

  public Mono<ServerResponse> user(ServerRequest request) {
    log.info("유저 정보 요청");
    return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .cast(OAuth2AuthenticationToken.class)
            .map(auth -> auth.getPrincipal().getAttributes())
            .flatMap(attributes -> ServerResponse.ok().bodyValue(attributes));
  }
}
