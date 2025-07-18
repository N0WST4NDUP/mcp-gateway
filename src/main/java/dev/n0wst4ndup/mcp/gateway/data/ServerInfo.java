package dev.n0wst4ndup.mcp.gateway.data;

import lombok.Getter;

@Getter
public class ServerInfo {
  private final String name;
  private final int clientCount;

  public ServerInfo(String name, int clientCount) {
    this.name = name;
    this.clientCount = clientCount;
  }
}
