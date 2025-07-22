package dev.n0wst4ndup.mcp.gateway.handler;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import dev.n0wst4ndup.mcp.gateway.strategy.AdapterStrategy;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdaptersHandler {

  private final McpAsyncClientManager clientManager;
  private final List<AdapterStrategy> strategies;

  public Mono<ServerResponse> getAdapters(ServerRequest request) {
    return  ServerResponse.ok()
                          .body(clientManager.getServers(), ServerInfo.class);
  }
  
  public Mono<ServerResponse> registerAdapter(ServerRequest request) {
    return request.bodyToMono(ServerParam.class)
                  .flatMap(clientManager::createNewClient)
                  .then(ServerResponse.ok()
                    .bodyValue("ok!"));
  }
  
  //TO-DO
  public Mono<ServerResponse> deleteAdapter(ServerRequest request) {
    return  Mono.just(request.pathVariable("server"))
                .flatMap(clientManager::closeClient)
                .then(ServerResponse.ok()
                  .bodyValue("deleted!"));
  }

  public Mono<ServerResponse> stream(ServerRequest request) {
    return  ServerResponse.ok()
                          .contentType(MediaType.TEXT_EVENT_STREAM)
                          .bodyValue("GET!!");
  }

  public Mono<ServerResponse> useAdapter(ServerRequest request) {
    String server = request.pathVariable("server");
    String mcpSessionId = request.headers().firstHeader("Mcp-Session-Id");

    return request.bodyToMono(JSONRPCRequest.class)
                  .flatMap(req -> 
                    strategies.stream()
                    .filter(strategy -> strategy.supports(req.method()))
                    .findFirst()
                    .map(strategy -> strategy.handle(req, clientManager, server, mcpSessionId))
                    .orElse(ServerResponse.ok()
                      .contentType(MediaType.APPLICATION_JSON)
                      .bodyValue(new JSONRPCResponse(req.jsonrpc(), req.id(), null, new JSONRPCError(McpSchema.ErrorCodes.METHOD_NOT_FOUND, "METHOD_NOT_FOUND: ", req.method())))));
  }

  public Mono<ServerResponse> disconnect(ServerRequest request) {
    String mcpSessionId = request.headers().firstHeader("Mcp-Session-Id");

    if (mcpSessionId == null || mcpSessionId.isBlank()) {
      return  ServerResponse.badRequest()
                            .bodyValue("Missing or blank Mcp-Session-Id header");
    }

    log.info("Disconnect Request: {}", request.method());

    return Mono.empty();
  } 

}
