package com.example.crawlerapi.startup;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.example.crawler.KeelungSightsCrawler;
import com.example.crawler.Sight;
import com.example.crawlerapi.entity.SightEntity;
import com.example.crawlerapi.repo.SightRepository;

@Component
public class DataLoader implements ApplicationRunner {
    private static final List<String> ZONES = Arrays.asList("七堵", "中山", "中正", "仁愛", "安樂", "信義", "暖暖");
    private final SightRepository repo;
    private final KeelungSightsCrawler crawler = new KeelungSightsCrawler();

    public DataLoader(SightRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repo.count() > 0) {
            System.out.println("[DataLoader] DB has data, skip import.");
            return;
        }
        System.out.println("[DataLoader] Initial import started...");
        int inserted = 0;
        for (String zone : ZONES) {
            try {
                Sight[] items = crawler.getItems(zone);
                if (items == null)
                    continue;
                for (Sight s : items) {
                    try {
                        boolean exists = repo.findBySightNameAndZone(s.getSightName(), zone).isPresent();
                        if (!exists) {
                            repo.save(new SightEntity(s.getSightName(), zone, s.getCategory(),
                                    s.getPhotoURL(), s.getAddress(), s.getDescription()));
                            inserted++;
                        }
                    } catch (DuplicateKeyException ignore) {
                    }
                }
            } catch (Exception ex) {
                System.err.println("[DataLoader] zone " + zone + " error: " + ex.getMessage());
            }
        }
        System.out.println("[DataLoader] Initial import done. Inserted ~ " + inserted + " docs.");
    }
}
