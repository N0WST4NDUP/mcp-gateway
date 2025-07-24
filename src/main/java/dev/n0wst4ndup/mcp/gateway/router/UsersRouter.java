package dev.n0wst4ndup.mcp.gateway.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import dev.n0wst4ndup.mcp.gateway.handler.UsersHandler;

@Configuration
public class UsersRouter {

  @Bean
  public RouterFunction<ServerResponse> usersRoute(UsersHandler handler) {
    return RouterFunctions.route()
            .GET("/", handler::helloWorld)
            .GET("/user", handler::user)
            .build();
  }
}