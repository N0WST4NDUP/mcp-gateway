package dev.n0wst4ndup.mcp.gateway.strategy;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class InitializedStrategy implements AdapterStrategy {
  @Override
  public boolean supports(String method) {
    return McpSchema.METHOD_NOTIFICATION_INITIALIZED.equals(method);
  }
  @Override
  public Mono<ServerResponse> handle(JSONRPCRequest request, McpAsyncClientManager clientManager, String server, String mcpSessionId) {
    log.info("Init Notification: {}", request);
    return Mono.empty();
  }
}