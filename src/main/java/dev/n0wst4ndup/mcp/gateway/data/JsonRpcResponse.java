package dev.n0wst4ndup.mcp.gateway.data;

public class JsonRpcResponse<T> {
  
  private final String jsonrpc = "2.0";
  private final Object id;
  private final T result;

  public JsonRpcResponse(Object id, T result) {
    this.id = id;
    this.result = result;
  }

}
