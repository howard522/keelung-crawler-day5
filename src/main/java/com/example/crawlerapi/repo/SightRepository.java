package com.example.crawlerapi.repo;
import java.util.List;
 import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.crawlerapi.entity.SightEntity;

public interface SightRepository extends MongoRepository<SightEntity,String>{
  List<SightEntity> findByZoneContaining(String zone);
  Optional<SightEntity> findBySightNameAndZone(String sightName, String zone);
}
