package dev.n0wst4ndup.mcp.gateway.service;

import java.time.Duration;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import dev.n0wst4ndup.mcp.gateway.data.ManagedClient;
import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
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
    return  Flux.fromIterable(clients.values())
                .flatMap(mono -> mono)
                .map(client -> new ServerInfo(client.getServerName(), client.getLastAccessed()));
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

    return  Mono.empty();
  }

  public Mono<Void> closeClient(String server) {
    return  Flux.fromIterable(clients.values())
                .flatMap(mono -> mono)
                .filter(client -> client.getServerName().equals(server))
                .flatMap(client -> client.close())
                .then();
  }

  public Mono<ManagedClient> connect(String server) {
    return  Flux.fromIterable(clients.values())
                .flatMap(mono -> mono)
                .filter(client -> client.getServerName().equals(server))
                .reduce((c1, c2) -> c1.getLastAccessed().isBefore(c2.getLastAccessed()) ? c1 : c2)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No client for session: " + server)));
  }
  
  public Mono<ManagedClient> connected(String sessionId) {
    Mono<ManagedClient> mono = clients.get(sessionId);
    
    if (mono == null) {
      return Mono.error(new NoSuchElementException("No client for session: " + sessionId));
    }

    return mono;
  }
}
