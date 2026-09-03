package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // Büyük/küçük harf bağımsız etiket bulma
    Optional<Tag> findByNameIgnoreCase(String name);

    // Auto-complete için harfle başlayan veya içeren etiketler
    @Query("SELECT t FROM Tag t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY t.name ASC")
    List<Tag> searchTags(@Param("query") String query);
}
