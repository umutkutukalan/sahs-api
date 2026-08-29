package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.BookmarkCollection;

public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, Long> {
    List<BookmarkCollection> findByUserId(Long userId);

    Optional<BookmarkCollection> findByUserIdAndIsDefaultTrue(Long userId);
}
