package dev.n0wst4ndup.mcp.gateway.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
  
  @Override
  public Mono<UserDetails> findByUsername(String username) {
    // TODO Auto-generated method stub
    return null;
  }
}
