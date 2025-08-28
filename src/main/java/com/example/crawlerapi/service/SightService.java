package com.example.crawlerapi.service;
import java.util.List;
import org.springframework.stereotype.Service;
import com.example.crawlerapi.entity.SightEntity;
import com.example.crawlerapi.repo.SightRepository;

@Service
public class SightService {
  private final SightRepository repo;
  public SightService(SightRepository repo){ this.repo = repo; }
  public List<SightEntity> byZone(String zone){
    return (zone==null||zone.trim().isEmpty()) ? repo.findAll() : repo.findByZoneContaining(zone.trim());
  }
}
