package com.sahnesen.api.sahnesen.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sahnesen.api.sahnesen.entities.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Belirli bir yazıya ait ana fuaye mektuplarını (parentId'si null olanlar)
    // getir
    List<Comment> findByPostIdAndParentIsNullOrderByCreatedAtAsc(Long postId);

    // Veya sadece findByPostId
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}