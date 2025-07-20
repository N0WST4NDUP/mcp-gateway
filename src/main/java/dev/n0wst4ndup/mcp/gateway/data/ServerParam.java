package dev.n0wst4ndup.mcp.gateway.data;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ServerParam {
  private boolean premium;
  private String command;
  private List<String> args;
  private Map<String, String> env;
}
