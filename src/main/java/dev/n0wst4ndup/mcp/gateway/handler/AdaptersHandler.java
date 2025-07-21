package dev.n0wst4ndup.mcp.gateway.handler;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.data.ServerInfo;
import dev.n0wst4ndup.mcp.gateway.data.ServerParam;
import dev.n0wst4ndup.mcp.gateway.service.McpAsyncClientManager;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCRequest;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCResponse.JSONRPCError;
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
    if (request.headers().header("Mcp-Session-Id").isEmpty()) {
      String server = request.pathVariable("server");

      return request.bodyToMono(JSONRPCRequest.class)
                    .doOnNext(req -> log.info("Init Request received: {}", req))
                    .flatMap( req -> clientManager.connect(server)
                                                  .flatMap(client -> {
                                                    JSONRPCResponse response = new JSONRPCResponse(req.jsonrpc(), req.id(), client.getInitResult(), null);

                                                    return  ServerResponse.ok()
                                                                          .contentType(MediaType.APPLICATION_JSON)
                                                                          .header("Mcp-Session-Id", client.getSessionId())
                                                                          .bodyValue(response);
                                                  }));
    } else {
      String sessionId = request.headers().header("Mcp-Session-Id").getFirst();

      return request.bodyToMono(JSONRPCRequest.class)
                    .doOnNext(req -> log.info("Use Adapter Request received: {}", req))
                    .flatMap( req -> clientManager.test(sessionId)
                                                  .flatMap(client -> {
                                                    return switch(req.method()) {
                                                      case McpSchema.METHOD_TOOLS_LIST -> {
                                                        yield client.listTools().map(result -> new JSONRPCResponse(req.jsonrpc(), req.id(), result, null));
                                                      }
                                                      case McpSchema.METHOD_TOOLS_CALL -> {
                                                         
                                                        yield null;
                                                      }
                                                        // client.callTool(null).map(result -> new JSONRPCResponse(req.jsonrpc(), req.id(), result, null));
                                                      default -> 
                                                        Mono.just(new JSONRPCResponse(req.jsonrpc(), req.id(), null, new JSONRPCError(McpSchema.ErrorCodes.METHOD_NOT_FOUND, "METHOD_NOT_FOUND: ", null)));
                                                    };
                                                  }))
                    .flatMap( response -> ServerResponse.ok()
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .bodyValue(response));
    }
  }

  public Mono<ServerResponse> deleteAdapter(ServerRequest request) {
    return  Mono.just(request.pathVariable("server"))
                .flatMap(clientManager::closeClient)
                .then(ServerResponse.ok()
                                    .bodyValue("deleted!"));
  }
}
