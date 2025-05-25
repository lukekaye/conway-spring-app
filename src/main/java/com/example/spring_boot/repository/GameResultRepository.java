package com.example.spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.spring_boot.entity.GameResultEntity;

public interface GameResultRepository extends JpaRepository<GameResultEntity, Long>{
    
}
