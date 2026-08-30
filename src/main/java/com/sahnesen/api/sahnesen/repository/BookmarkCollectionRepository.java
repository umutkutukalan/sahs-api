package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sahnesen.api.sahnesen.entities.BookmarkCollection;

public interface BookmarkCollectionRepository extends JpaRepository<BookmarkCollection, Long> {

    // Kullanıcının username'ine göre koleksiyonlarını getir
    List<BookmarkCollection> findByUser_Username(String username);

    // Kullanıcının username'ine ve varsayılan durumuna göre koleksiyon bul
    Optional<BookmarkCollection> findByUser_UsernameAndIsDefaultTrue(String username);
}