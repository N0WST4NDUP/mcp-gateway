package dev.n0wst4ndup.mcp.gateway.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

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

  private final Map<String, List<McpAsyncClient>> servers = new ConcurrentHashMap<>();

  public Flux<ServerInfo> getServers() {
    return  Flux.fromIterable(servers.entrySet())
                .map(entry -> new ServerInfo(entry.getKey(), entry.getValue().size()));
  }
  
  public Mono<Void> createNewClient(ServerParam serverParam) {
    ServerParameters serverParameters = ServerParameters.builder(serverParam.getCommand())
                                                        .args(serverParam.getArgs())
                                                        .env(serverParam.getEnv())
                                                        .build();

    StdioClientTransport transport = new StdioClientTransport(serverParameters);

    McpAsyncClient client = McpClient.async(transport)
                                      .requestTimeout(Duration.ofSeconds(30))
                                      .build();

    return  client.initialize()
                  .doOnSuccess(result -> {
                    servers.computeIfAbsent(client.getServerInfo().name(), k -> Collections.synchronizedList(new ArrayList<>())).add(client);
                    log.info("Client created successfully: {}", client.getServerInfo().name());
                  })
                  .doOnError(error -> log.error("Failed to create client: {}, error: {}", client.getServerInfo().name(), error))
                  .then();
  }

  public Mono<Void> closeClient(String serverName) {
    List<McpAsyncClient> clients = servers.get(serverName);

    if (clients == null) return Mono.empty();

    List<McpAsyncClient> toClose;
    synchronized (clients) {
        toClose = new ArrayList<>(clients);
    }

    return  Flux.fromIterable(toClose)
                .flatMap(client -> client.closeGracefully()
                                          .doOnSuccess(v -> {
                                            clients.remove(client);
                                            
                                            if (clients.isEmpty()) {
                                              servers.remove(serverName);
                                            }
                                          }))
                .then();
  }
  
}
