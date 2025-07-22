package dev.n0wst4ndup.mcp.gateway.data;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.InitializeResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import reactor.core.publisher.Mono;

public class ManagedClient {

  private final String sessionId;
  private final String serverName;
  private final McpAsyncClient mcpClient;
  private final boolean isPremium;
  private final AtomicReference<Instant> lastAccessed = new AtomicReference<>();
  private final InitializeResult initResult;

  private ManagedClient(String sessionId, String serverName, McpAsyncClient client, boolean isPremium, InitializeResult initResult) {
    this.sessionId = sessionId;
    this.serverName = serverName;
    this.mcpClient = client;
    this.isPremium = isPremium;
    this.initResult = initResult;
    recordAccess();
  }

  public static Mono<ManagedClient> asyncOf(String sid, McpAsyncClient client, boolean premium) {
    return  client.initialize()
                  .map(result -> new ManagedClient(sid, client.getServerInfo().name(), client, premium, result))
                  .cache();
  }

  public String getSessionId() { return sessionId; }
  public String getServerName() { return serverName; } 
  public boolean isPremium() { return isPremium; }
  public Instant getLastAccessed() { return lastAccessed.get(); }

  public Mono<InitializeResult> initialize() {
    recordAccess();
    return Mono.just(initResult);
  }

  public Mono<Void> close() {
    return mcpClient.closeGracefully();
  }
  
  public Mono<ListToolsResult> listTools() {
    recordAccess();
    return mcpClient.listTools().cache();
  }

  public Mono<CallToolResult> callTool(CallToolRequest callToolRequest) {
    recordAccess();
    return mcpClient.callTool(callToolRequest);
  }

  private final void recordAccess() { 
    lastAccessed.set(Instant.now()); 
  }
}
