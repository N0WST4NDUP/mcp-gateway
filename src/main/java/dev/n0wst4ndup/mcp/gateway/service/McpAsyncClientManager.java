package dev.n0wst4ndup.mcp.gateway.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

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
                    servers.computeIfAbsent(client.getServerInfo().name(), k -> new CopyOnWriteArrayList<>()).add(client);
                    log.info("Client created successfully: {}", client.getServerInfo().name());
                  })
                  .doOnError(error -> log.error("Failed to create client: {}, error: {}", client.getServerInfo().name(), error))
                  .then();
  }

  public Mono<Void> closeClient(String serverName) {
    List<McpAsyncClient> clients = servers.getOrDefault(serverName, List.of());

    return  Flux.fromIterable(clients)
                .flatMap(client -> client.closeGracefully()
                  .doOnSuccess(result -> log.info("Client closed gracefully: {}", serverName))
                  .doOnError(error -> log.error("Failed to close client gracefully: {}, error: {}", serverName, error.getMessage()))
                )
                .then();
  }
  
}
