package net.joostvdg.ingressdnsexportcontroller.model;

public class Service {
  private String name;
  private String namespace;
  private String clusterIP;
  private String externalIP;

  public Service(String name, String namespace, String clusterIP, String externalIP) {
    this.name = name;
    this.namespace = namespace;
    this.clusterIP = clusterIP;
    this.externalIP = externalIP;
  }

  public String getName() {
    return name;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getClusterIP() {
    return clusterIP;
  }

  public String getExternalIP() {
    return externalIP;
  }
}
