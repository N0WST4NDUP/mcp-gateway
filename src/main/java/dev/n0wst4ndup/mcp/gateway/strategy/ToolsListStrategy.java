package dev.n0wst4ndup.mcp.gateway.strategy;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ToolsListStrategy implements AdapterStrategy {
  @Override
  public boolean supports(String method) {
    return McpSchema.METHOD_TOOLS_LIST.equals(method);
  }
  @Override
  public Mono<ServerResponse> handle(JSONRPCRequest request, McpAsyncClientManager clientManager, String server, String mcpSessionId) {
    log.info("Tools/List Request: {}", request);
    if (mcpSessionId == null || mcpSessionId.isBlank()) {
      return  ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new JSONRPCResponse(request.jsonrpc(), request.id(), null, new JSONRPCError(McpSchema.ErrorCodes.INVALID_REQUEST, "INVALID_REQUEST: ", "No Session Id.")));
    }
    return clientManager.connected(mcpSessionId)
                        .flatMap(client -> 
                          client.listTools()
                          .flatMap(result -> 
                            ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new JSONRPCResponse(request.jsonrpc(), request.id(), result, null))))
                        .onErrorResume(e ->
                          ServerResponse.ok()
                          .contentType(MediaType.APPLICATION_JSON)
                          .bodyValue(new JSONRPCResponse(request.jsonrpc(), request.id(), null, new JSONRPCError(McpSchema.ErrorCodes.INVALID_PARAMS, "INVALID_PARAMS: ", e.getMessage())))
                        );
  }
}
