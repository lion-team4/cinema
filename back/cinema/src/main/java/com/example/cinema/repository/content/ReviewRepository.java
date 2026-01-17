package com.example.cinema.repository.content;

import com.example.cinema.entity.Content;
import com.example.cinema.entity.Review;
import com.example.cinema.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByWatchHistory(WatchHistory watchHistory);

    Page<Review> findByContent(Content content, Pageable pageable);
}
