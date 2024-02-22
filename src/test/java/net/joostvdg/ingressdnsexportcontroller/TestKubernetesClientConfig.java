package net.joostvdg.ingressdnsexportcontroller;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import java.io.IOException;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TestKubernetesClientConfig {

  @Bean("apiClient.stub")
  public ApiClient apiClient() {
    ApiClient client = null;
    try {
      client = Config.defaultClient();
    } catch (IOException e) {
      // TODO: must be a more elagant way to handle this, so we can still build/test without a k8s
      // cluster
      e.printStackTrace();
    }
    io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
    return client;
  }

  @Primary
  @Bean("coreV1Api.stub")
  public CoreV1Api coreV1Api(ApiClient apiClient) {
    return Mockito.mock(CoreV1Api.class);
  }
}
