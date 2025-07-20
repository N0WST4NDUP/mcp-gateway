package dev.n0wst4ndup.mcp.gateway.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dev.n0wst4ndup.mcp.gateway.data.ManagedClient;
import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpAsyncClientManager {

  private final Map<String, Mono<ManagedClient>> clients = new ConcurrentHashMap<>();

  // TO-DO
  public Flux<ServerInfo> getServers() {
    return  Flux.fromIterable(clients.entrySet())
                .map(entry -> new ServerInfo(entry.getKey(), 0));
  }
  
  public Mono<Void> createNewClient(ServerParam serverParam) {
    String sessionId = UUID.randomUUID().toString();

    ServerParameters serverParameters = ServerParameters.builder(serverParam.getCommand())
                                                        .args(serverParam.getArgs())
                                                        .env(serverParam.getEnv())
                                                        .build();

    StdioClientTransport transport = new StdioClientTransport(serverParameters);

    McpAsyncClient client = McpClient.async(transport)
                                      .requestTimeout(Duration.ofSeconds(30))
                                      .build();

    clients.put(sessionId, ManagedClient.asyncOf(sessionId, client, serverParam.isPremium()));
    log.info("클라이언트 스토어에 추가: {}", clients.get(sessionId));
    return  Mono.empty();
  }

  public Mono<Void> closeClient(String serverName) {

    return Mono.when();
  }

  public Mono<ManagedClient> connect(String server) {
    return  Flux.fromIterable(clients.values())
                .flatMap(mono -> mono)
                .filter(client -> client.getServerName().equals(server))
                .reduce((c1, c2) -> c1.getAllocatedCount().get() <= c2.getAllocatedCount().get() ? c1 : c2);
  }
  
  public Mono<CallToolResult> test(String sessionId, CallToolRequest toolReq) {
    Mono<ManagedClient> mono = clients.get(sessionId);
    
    if (mono == null) { // 키에 해당하는 클라이언트가 없는 경우 예외 처리
      return Mono.error(new NoSuchElementException("No client for session: " + sessionId));
    }

    return mono.flatMap(client -> client.getMcpClient().callTool(toolReq));
  }
}
