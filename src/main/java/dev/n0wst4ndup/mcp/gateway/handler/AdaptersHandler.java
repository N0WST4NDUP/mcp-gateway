package dev.n0wst4ndup.mcp.gateway.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AdaptersHandler {

  private final McpAsyncClientManager clientManager;

  public Mono<ServerResponse> getAdapters(ServerRequest request) {
    return ServerResponse.ok().body(clientManager.getServers(), ServerInfo.class);
  }
  
  public Mono<ServerResponse> registerAdapter(ServerRequest request) {
    return request.bodyToMono(ServerParam.class)
                  .flatMap(clientManager::createNewClient)
                  .then(ServerResponse.ok().bodyValue("ok!"));
  }

  public Mono<ServerResponse> deleteAdapter(ServerRequest request) {
    return  Mono.just(request.pathVariable("name"))
                .flatMap(clientManager::closeClient)
                .then(ServerResponse.ok().bodyValue("deleted!"));
  }
}
