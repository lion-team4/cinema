package com.example.cinema.repository;


import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long> {


}
