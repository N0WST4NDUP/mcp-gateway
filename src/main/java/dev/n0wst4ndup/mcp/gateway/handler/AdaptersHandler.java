package dev.n0wst4ndup.mcp.gateway.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdaptersHandler {

  private final McpAsyncClientManager clientManager;

  public Mono<ServerResponse> getAdapters(ServerRequest request) {
    return  ServerResponse.ok()
                          .body(clientManager.getServers(), ServerInfo.class);
  }
  
  public Mono<ServerResponse> registerAdapter(ServerRequest request) {
    return request.bodyToMono(ServerParam.class)
                  .flatMap(clientManager::createNewClient)
                  .then(ServerResponse.ok()
                                      .bodyValue("ok!"));
  }

  public Mono<ServerResponse> stream(ServerRequest request) {
    Flux<String> updatesFlux = Flux.create(emitter -> {
      emitter.next("{\"status\":\"progress\",\"percent\":10}");
      emitter.next("{\"status\":\"progress\",\"percent\":20}");
      emitter.next("{\"status\":\"progress\",\"percent\":30}");
      emitter.next("{\"status\":\"progress\",\"percent\":40}");
      emitter.next("{\"status\":\"progress\",\"percent\":50}");
      emitter.next("{\"status\":\"progress\",\"percent\":60}");
      emitter.next("{\"status\":\"progress\",\"percent\":70}");
      emitter.next("{\"status\":\"progress\",\"percent\":80}");
      emitter.next("{\"status\":\"progress\",\"percent\":90}");
      emitter.next("{\"status\":\"done\"}");
      emitter.complete();
    });

    return  ServerResponse.ok()
                          .contentType(MediaType.TEXT_EVENT_STREAM)
                          .body(updatesFlux, String.class);
  }

  public Mono<ServerResponse> useAdapter(ServerRequest request) {
    String server = request.pathVariable("server");
    log.info("useAdapter 호출됨 - server: {}", server);

    if (request.headers().header("Mcp-Session-Id").isEmpty()) {
      return clientManager.connect(server)
                          .doOnNext(client -> log.info("Connected: sessionId={}, initResult={}", client.getSessionId(), client.getInitResult()))
                          .flatMap(client ->  ServerResponse.ok()
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .bodyValue(client.getInitResult()));
    } else {
      return request.bodyToMono(CallToolRequest.class)
                    .doOnNext(req -> log.info("CallToolRequest received: {}", req))
                    .flatMap(req ->  clientManager.test(server, req)
                                                  .doOnNext(result -> log.info("CallToolResult: {}", result)))
                    .flatMap(result ->  ServerResponse.ok()
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .bodyValue(result));
    }
  }

  public Mono<ServerResponse> deleteAdapter(ServerRequest request) {
    return  Mono.just(request.pathVariable("server"))
                .flatMap(clientManager::closeClient)
                .then(ServerResponse.ok()
                                    .bodyValue("deleted!"));
  }
}
