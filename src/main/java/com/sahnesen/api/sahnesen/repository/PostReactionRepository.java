package com.sahnesen.api.sahnesen.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.PostReaction;
import com.sahnesen.api.sahnesen.enums.PostType;
import com.sahnesen.api.sahnesen.enums.ReactionType;

public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
        // Username üzerinden reaksiyon bulma
        Optional<PostReaction> findByUser_UsernameAndPostIdAndReactionType(String username, Long postId,
                        ReactionType reactionType);

        // Username üzerinden reaksiyon varlık kontrolü
        boolean existsByUser_UsernameAndPostIdAndReactionType(String username, Long postId, ReactionType reactionType);

        long countByPostIdAndReactionType(Long postId, ReactionType reactionType);

        // Kullanıcının belirli bir reaksiyona sahip postlarını sayfalı getirir
        Page<PostReaction> findByUser_UsernameAndReactionType(String username, ReactionType reactionType,
                        Pageable pageable);

        // Kullanıcının reaksiyon gösterdiği postları türe (PostType) göre filtreleyerek
        // doğrudan Post olarak getirir
        @Query("SELECT r.post FROM PostReaction r WHERE r.user.username = :username AND r.reactionType = :reactionType AND (:postType IS NULL OR r.post.postType = :postType)")
        Page<Post> findLikedPostsByUsernameWithFilter(
                        @Param("username") String username,
                        @Param("reactionType") ReactionType reactionType,
                        @Param("postType") PostType postType,
                        Pageable pageable);

        // Belirli bir kullanıcının belirli bir reaksiyon türündeki (örn: LIKE) toplam
        // sayısını döner
        long countByUser_UsernameAndReactionType(String username, ReactionType reactionType);

        // Eğer filtrelenmiş (postType kırılımlı) sayım istenirse:
        long countByUser_UsernameAndReactionTypeAndPost_PostType(String username, ReactionType reactionType,
                        PostType postType);
}
