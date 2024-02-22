package net.joostvdg.ingressdnsexportcontroller.services;

import com.google.gson.JsonElement;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import net.joostvdg.ingressdnsexportcontroller.model.DNSEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class InspectService {

  private final List<V1Service> services = Collections.synchronizedList(new ArrayList<>());

  private final List<DNSEntry> dnsEntries = Collections.synchronizedList(new ArrayList<>());

  private final Logger logger = LoggerFactory.getLogger(InspectService.class);
  private net.joostvdg.ingressdnsexportcontroller.model.Service istioService;

  @Value("${cluster.name}")
  private String clusterName;

  @Value("${cluster.url}")
  private String clusterApiServerIp;

  private final CoreV1Api coreV1Api;

  private final ApiClient apiClient;

  public InspectService(CoreV1Api coreV1Api, ApiClient apiClient) {
    this.coreV1Api = coreV1Api;
    this.apiClient = apiClient;
  }

  @PostConstruct
  private void init() {
    // This method will be called once dependency injection is done to perform any initialization.
    printClusterInfo();
    fetchServices(); // Initial fetch to populate the list right away.
    fetchVirtualServices(); // Initial fetch to populate the list right away.
  }

  private void printClusterInfo() {
    logger.info("Cluster Name: " + clusterName);
    logger.info("Cluster API Server IP: " + clusterApiServerIp);
  }

  @Scheduled(fixedRate = 30000)
  public void fetchVirtualServices() {
    DynamicKubernetesApi dynamicApi =
        new DynamicKubernetesApi("networking.istio.io", "v1beta1", "virtualservices", apiClient);

    var virtualServices = dynamicApi.list().getObject();
    if (virtualServices == null) {
      System.out.println("No VirtualServices found (NULL.)");
      return;
    } else if (virtualServices.getItems().isEmpty()) {
      logger.info("No VirtualServices found (EMPTY.)");
    } else {
      logger.info("Found " + virtualServices.getItems().size() + " VirtualServices.");
    }

    for (var virtualService : virtualServices.getItems()) {

      String kind = virtualService.getKind();
      String name = virtualService.getMetadata().getName();
      String namespace = virtualService.getMetadata().getNamespace();
      JsonElement hosts = virtualService.getRaw().get("spec").getAsJsonObject().get("hosts");

      if (istioService != null) {
        // create DNS entry for each virtual service
        for (JsonElement host : hosts.getAsJsonArray()) {
          var dnsEntry =
              new DNSEntry(
                  host.getAsString(),
                  istioService.getExternalIP(),
                  "80", // TODO: what do we do with ports?
                  namespace,
                  kind,
                  "Istio",
                  clusterName,
                  clusterApiServerIp);
          dnsEntries.add(dnsEntry);
        }
      }

      logger.debug(
          "Found VirtualService: "
              + name
              + " in namespace: "
              + namespace
              + " with hosts: "
              + hosts);
    }
  }

  @Scheduled(fixedRate = 30000)
  public void fetchServices() {

    V1ServiceList serviceList = null;
    try {
      serviceList = coreV1Api.listServiceForAllNamespaces().execute();
    } catch (ApiException ex) {
      logger.error("Exception when calling CoreV1Api#listServiceForAllNamespaces", ex);
      throw new RuntimeException(ex);
    }
    synchronized (services) {
      services.clear();
      services.addAll(serviceList.getItems());
    }

    for (V1Service service : services) {
      if (Objects.equals(
          Objects.requireNonNull(service.getMetadata()).getName(), "istio-ingressgateway")) {
        String namespace = service.getMetadata().getNamespace();
        String name = service.getMetadata().getName();
        String clusterIP = Objects.requireNonNull(service.getSpec()).getClusterIP();
        String externalIp = "";
        if (service.getStatus() != null
            && service.getStatus().getLoadBalancer() != null
            && service.getStatus().getLoadBalancer().getIngress() != null
            && !service.getStatus().getLoadBalancer().getIngress().isEmpty()) {
          externalIp = service.getStatus().getLoadBalancer().getIngress().getFirst().getIp();
        }
        logger.info(
            "Found istio-ingressgateway: "
                + name
                + " in namespace: "
                + namespace
                + " with clusterIP: "
                + clusterIP
                + " and externalIP: "
                + externalIp);
        istioService =
            new net.joostvdg.ingressdnsexportcontroller.model.Service(
                name, namespace, clusterIP, externalIp);
      }
    }

    logger.info("Services updated.");
  }

  public List<net.joostvdg.ingressdnsexportcontroller.model.Service> getSafeServicesCopy() {
    var servicesCopy = new ArrayList<net.joostvdg.ingressdnsexportcontroller.model.Service>();
    synchronized (services) {
      for (V1Service service : services) {
        String namespace = Objects.requireNonNull(service.getMetadata()).getNamespace();
        String name = service.getMetadata().getName();
        String clusterIP = Objects.requireNonNull(service.getSpec()).getClusterIP();
        String externalIp = "";
        if (service.getSpec().getExternalIPs() != null) {
          externalIp = service.getSpec().getExternalIPs().toString();
        }
        servicesCopy.add(
            new net.joostvdg.ingressdnsexportcontroller.model.Service(
                name, namespace, clusterIP, externalIp));
      }
    }
    return servicesCopy;
  }

  public List<DNSEntry> getSafeDnsEntriesCopy() {
    List<DNSEntry> dnsEntriesCopy;
    synchronized (dnsEntries) {
      dnsEntriesCopy = new ArrayList<DNSEntry>(dnsEntries);
    }
    return dnsEntriesCopy;
  }
}
