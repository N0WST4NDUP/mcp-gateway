package dev.n0wst4ndup.mcp.gateway.data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import io.micrometer.core.ipc.http.HttpSender.Request;
import io.micrometer.core.ipc.http.HttpSender.Response;
import io.modelcontextprotocol.client.McpAsyncClient;
import reactor.core.publisher.Mono;

public class ManagedClient {

  private final McpAsyncClient mcpClient;
  private final AtomicInteger allocatedCount = new AtomicInteger(0);
  private final ReentrantLock clientLock = new ReentrantLock();

  public ManagedClient(McpAsyncClient client) {
    this.mcpClient = client;
  }

  // 사용자 할당
  public int allocateUser() {
    return allocatedCount.incrementAndGet();
  }

  // 사용자 해제
  public int releaseUser() {
      return allocatedCount.decrementAndGet();
  }

  // 현재 할당된 사용자 수 조회
  public int getAllocatedCount() {
    return allocatedCount.get();
  }
}
