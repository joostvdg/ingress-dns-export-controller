/* (C)2024 */
package net.joostvdg.ingressdnsexportcontroller.controllers;

import java.util.List;
import net.joostvdg.ingressdnsexportcontroller.model.DNSEntry;
import net.joostvdg.ingressdnsexportcontroller.model.Service;
import net.joostvdg.ingressdnsexportcontroller.services.InspectService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("export")
public class ExportController {

  private final InspectService inspectService;

  // Controller Constructor to add the InspectService
  public ExportController(InspectService inspectService) {
    this.inspectService = inspectService;
  }

  @GetMapping(produces = "application/json")
  public ResponseEntity<List<DNSEntry>> exportDns() {
    return ResponseEntity.ok(inspectService.getSafeDnsEntriesCopy());
  }

  @GetMapping(path = "/svc", produces = "application/json")
  public ResponseEntity<List<Service>> exportDnsSvc() {
    return ResponseEntity.ok(inspectService.getSafeServicesCopy());
  }
}
