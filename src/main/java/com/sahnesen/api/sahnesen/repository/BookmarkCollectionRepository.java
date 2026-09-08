package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.BookmarkCollection;
import com.sahnesen.api.sahnesen.entities.PostBookmark;
import com.sahnesen.api.sahnesen.enums.PostType;

public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, Long> {

    // Kullanıcının username'ine göre koleksiyonlarını getir
    List<BookmarkCollection> findByUser_Username(String username);

    // Kullanıcının username'ine ve varsayılan durumuna göre koleksiyon bul
    Optional<BookmarkCollection> findByUser_UsernameAndIsDefaultTrue(String username);
}