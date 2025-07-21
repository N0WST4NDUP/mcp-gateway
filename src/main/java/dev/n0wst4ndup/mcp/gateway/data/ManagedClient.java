package dev.n0wst4ndup.mcp.gateway.data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

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
  private final AtomicInteger allocatedCount = new AtomicInteger(0);
  private final ReentrantLock clientLock = new ReentrantLock();
  private final InitializeResult initResult;

  private ManagedClient(String sessionId, String serverName, McpAsyncClient client, boolean isPremium, InitializeResult initResult) {
    this.sessionId = sessionId;
    this.serverName = serverName;
    this.mcpClient = client;
    this.isPremium = isPremium;
    this.initResult = initResult;
  }

  public static Mono<ManagedClient> asyncOf(String sid, McpAsyncClient client, boolean premium) {
    return  client.initialize()
                  .map(result -> new ManagedClient(sid, client.getServerInfo().name(), client, premium, result));
  }

  public String getSessionId() { return sessionId; }
  public String getServerName() { return serverName; } 
  public boolean isPremium() { return isPremium; }
  public int getAllocatedCount() { return allocatedCount.get(); }
  public ReentrantLock getLock() { return clientLock; }

  public Mono<InitializeResult> initialize() {
    return Mono.just(initResult);
  }

  public Mono<Void> close() {
    return mcpClient.closeGracefully();
  }
  
  public Mono<ListToolsResult> listTools() {
    return mcpClient.listTools();
  }

  public Mono<CallToolResult> callTool(CallToolRequest callToolRequest) {
    return mcpClient.callTool(callToolRequest);
  }
}
