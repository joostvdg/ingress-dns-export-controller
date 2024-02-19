package net.joostvdg.ingressdnsexportcontroller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IngressDnsExportControllerApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngressDnsExportControllerApplication.class, args);
  }
}
