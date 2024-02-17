package net.joostvdg.ingressdnsexportcontroller.services;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.dynamic.DynamicKubernetesApi;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class InspectService {
    private final List<V1Service> services = Collections.synchronizedList(new ArrayList<>());
    private net.joostvdg.ingressdnsexportcontroller.model.Service istioService;


    @PostConstruct
    private void init() {
        // This method will be called once dependency injection is done to perform any initialization.
        fetchServices(); // Initial fetch to populate the list right away.
    }

    @Scheduled(fixedRate = 30000)
    public void fetchVirtualServices() {
        ApiClient apiClient = null;
        try {
            apiClient = ClientBuilder.standard().build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DynamicKubernetesApi dynamicApi = new DynamicKubernetesApi("networking.istio.io", "v1beta1","virtualservices", apiClient);

        var virtualServices = dynamicApi.list().getObject();
        if (virtualServices == null) {
            System.out.println("No VirtualServices found.");
            return;
        }
        for (var virtualService : virtualServices.getItems()) {
            String kind = virtualService.getKind();
            String name = virtualService.getMetadata().getName();
            String namespace = virtualService.getMetadata().getNamespace();
            String hosts = virtualService.getRaw().get("spec").getAsJsonObject().get("hosts").toString();
            System.out.println("Found VirtualService: " + name + " in namespace: " + namespace + " with hosts: " + hosts);
        }
    }
    @Scheduled(fixedRate = 30000)
    public void fetchServices() {
        try {
            ApiClient client = null;
            try {
                client = Config.defaultClient();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Configuration.setDefaultApiClient(client);

            CoreV1Api api = new CoreV1Api();
            V1ServiceList serviceList = api.listServiceForAllNamespaces().execute();
            synchronized (services) {
                services.clear();
                services.addAll(serviceList.getItems());
            }

            for (V1Service service : services) {
                if (service.getMetadata().getName().equals("istio-ingressgateway")) {
                    String namespace = service.getMetadata().getNamespace();
                    String name = service.getMetadata().getName();
                    String clusterIP = service.getSpec().getClusterIP();
                    String type = service.getSpec().getType();
                    String externalIp = "";
                    if (service.getStatus() != null && service.getStatus().getLoadBalancer() != null && service.getStatus().getLoadBalancer().getIngress() != null && !service.getStatus().getLoadBalancer().getIngress().isEmpty()) {
                        externalIp = service.getStatus().getLoadBalancer().getIngress().getFirst().getIp();
                    }
                    System.out.println("Found istio-ingressgateway: " + name + " in namespace: " + namespace + " with clusterIP: " + clusterIP + " and externalIP: " + externalIp);
                    istioService = new net.joostvdg.ingressdnsexportcontroller.model.Service(name, namespace, clusterIP, externalIp);
                }
            }

            // TODO: replace with proper logging
            System.out.println("Services updated.");
        } catch (ApiException e) {
            // TODO: replace with proper logging
            System.err.println("Exception when calling CoreV1Api#listServiceForAllNamespaces");
            e.printStackTrace();
        }
    }

    public List<net.joostvdg.ingressdnsexportcontroller.model.Service> getSafeServicesCopy() {
        var servicesCopy = new ArrayList<net.joostvdg.ingressdnsexportcontroller.model.Service>();
        synchronized (services) {
            for (V1Service service : services) {
                String namespace = service.getMetadata().getNamespace();
                String name = service.getMetadata().getName();
                String clusterIP = service.getSpec().getClusterIP();
                String type = service.getSpec().getType();
                String externalIp = "";
                if (service.getSpec().getExternalIPs() != null) {
                    externalIp = service.getSpec().getExternalIPs().toString();
                }
                servicesCopy.add(new net.joostvdg.ingressdnsexportcontroller.model.Service(name, namespace, clusterIP, externalIp));
            }
        }
        return servicesCopy;
    }
}
