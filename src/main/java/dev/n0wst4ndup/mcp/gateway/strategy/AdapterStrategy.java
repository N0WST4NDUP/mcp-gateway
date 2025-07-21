package dev.n0wst4ndup.mcp.gateway.strategy;

import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import reactor.core.publisher.Mono;

public interface AdapterStrategy {
  boolean supports(String method);
  Mono<ServerResponse> handle(JSONRPCRequest request, McpAsyncClientManager clientManager, String server, String mcpSessionId);
}