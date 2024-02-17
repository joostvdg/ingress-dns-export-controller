package net.joostvdg.ingressdnsexportcontroller.controllers;


import net.joostvdg.ingressdnsexportcontroller.model.DNSEntry;
import net.joostvdg.ingressdnsexportcontroller.model.Service;
import net.joostvdg.ingressdnsexportcontroller.services.InspectService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("export")
public class ExportController {

    private final InspectService inspectService;

    // Controller Constructor to add the InspectService
    public ExportController(InspectService inspectService) {
        this.inspectService = inspectService;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<DNSEntry> exportDns() {
        DNSEntry dnsEntry = new DNSEntry("test", "127.0.0.1", "80", "default", "Service", "MetalLB", "cluster1", "192.168.178.1");
        return ResponseEntity.ok(dnsEntry);
    }

    @GetMapping(path = "/svc", produces = "application/json")
    public ResponseEntity<List<Service>> exportDnsSvc() {
        return ResponseEntity.ok(inspectService.getSafeServicesCopy());
    }
}
