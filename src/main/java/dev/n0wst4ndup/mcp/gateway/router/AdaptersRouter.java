package dev.n0wst4ndup.mcp.gateway.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.handler.AdaptersHandler;

@Configuration
public class AdaptersRouter {
  @Bean
  public RouterFunction<ServerResponse> adaptersRoute(AdaptersHandler handler) {
    return RouterFunctions.route()
            .GET("/adapters", handler::getAdapters)
            .POST("/adapters", handler::registerAdapter)
            .DELETE("/adapters", handler::deleteAdapter)
            .GET("/adapters/{server}", handler::stream)
            .POST("/adapters/{server}", handler::useAdapter)
            .DELETE("/adapters/{server}", handler::disconnect)
            .build();
  }
}
