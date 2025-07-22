package dev.n0wst4ndup.mcp.gateway.data;

import java.time.Instant;

import lombok.Getter;

@Getter
public class ServerInfo {
  private final String name;
  private final Instant lastAccessed;

  public ServerInfo(String name, Instant lastAccessed) {
    this.name = name;
    this.lastAccessed = lastAccessed;
  }
}
