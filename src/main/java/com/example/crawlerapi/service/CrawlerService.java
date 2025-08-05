package com.example.crawlerapi.service;

import org.springframework.stereotype.Service;

import com.example.crawler.KeelungSightsCrawler;
import com.example.crawler.Sight;

@Service
public class CrawlerService {
    private final KeelungSightsCrawler crawler = new KeelungSightsCrawler();

    public Sight[] fetchByZone(String zone) throws Exception {
        return crawler.getItems(zone);
    }
}
