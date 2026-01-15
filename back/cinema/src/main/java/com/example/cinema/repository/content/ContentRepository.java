package com.example.cinema.repository.content;


import com.example.cinema.entity.Content;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import com.example.cinema.entity.Tag;
import com.example.cinema.repository.content.custom.ContentRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content,Long>, ContentRepositoryCustom {

    @Query("SELECT tm.tag FROM TagMap tm JOIN tm.tag WHERE tm.content.contentId = :contentId")
    List<Tag> getTagsByContentId(Long contentId);


    @Modifying
    @Query("""
           delete from Content c
           where c.status = com.example.cinema.type.ContentStatus.DRAFT
             and c.createdAt < :cutoff
           """)
    int deleteOldDrafts(@Param("cutoff") LocalDateTime cutoff);

    // 중복 인코딩/경합 방지용 row lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Content c where c.contentId = :id")
    Optional<Content> findByIdForUpdate(@Param("id") Long id);

}
