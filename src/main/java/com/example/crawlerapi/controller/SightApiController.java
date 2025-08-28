package com.example.crawlerapi.controller;
import java.util.List;

import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crawlerapi.entity.SightEntity;
import com.example.crawlerapi.service.SightService;

@CrossOrigin(origins="*")
@RestController
public class SightApiController {
  private final SightService service;
  public SightApiController(SightService service){ this.service = service; }

  @GetMapping("/SightAPI")
  public ResponseEntity<List<SightEntity>> getSights(@RequestParam(required=false) String zone){
    try{ return ResponseEntity.ok(service.byZone(zone)); }
    catch(Exception e){ return ResponseEntity.status(500).build(); }
  }
}
