package com.example.crawlerapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.crawler.Sight;
import com.example.crawlerapi.service.CrawlerService;

@CrossOrigin(origins = "*")  // 允许来自任意 origin 的跨域请求
@RestController
public class SightApiController {

    @Autowired
    private CrawlerService service;

    @GetMapping("/SightAPI")
    public ResponseEntity<Sight[]> getSights(@RequestParam String zone) {
        try {
            Sight[] sights = service.fetchByZone(zone);
            return ResponseEntity.ok(sights);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
