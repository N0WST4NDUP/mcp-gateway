package dev.n0wst4ndup.mcp.gateway.data;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.spec.McpSchema.InitializeResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Getter
@Slf4j
public class ManagedClient {

  private final String sessionId;
  private final String serverName;
  private final McpAsyncClient mcpClient;
  private final boolean premium;
  private final AtomicInteger allocatedCount = new AtomicInteger(0);
  private final ReentrantLock clientLock = new ReentrantLock();
  private final InitializeResult initResult;

  private ManagedClient(String sessionId, String serverName, McpAsyncClient client, boolean premium, InitializeResult initResult) {
    this.sessionId = sessionId;
    this.serverName = serverName;
    this.mcpClient = client;
    this.premium = premium;
    this.initResult = initResult;
  }

  public static Mono<ManagedClient> asyncOf(String sid, McpAsyncClient client, boolean premium) {
    log.info("클라이언트 init");
    return  client.initialize()
                  .map(result -> new ManagedClient(sid, client.getServerInfo().name(), client, premium, result));
  }
}
