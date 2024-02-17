package net.joostvdg.ingressdnsexportcontroller;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@SpringBootApplication
@EnableScheduling
public class IngressDnsExportControllerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IngressDnsExportControllerApplication.class, args);
    }

}
