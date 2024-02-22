package net.joostvdg.ingressdnsexportcontroller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {TestKubernetesClientConfig.class})
class IngressDnsExportControllerApplicationTests {

  @Test
  void contextLoads() {}
}
