package com.sahnesen.api.sahnesen.services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.CommentRequestDTO;
import com.sahnesen.api.sahnesen.dto.CommentResponseDTO;
import com.sahnesen.api.sahnesen.entities.Comment;
import com.sahnesen.api.sahnesen.entities.Post;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.repository.CommentRepository;
import com.sahnesen.api.sahnesen.repository.PostRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

        private final CommentRepository commentRepository;
        private final PostRepository postRepository;
        private final UserRepository userRepository;

        @Transactional
        public CommentResponseDTO addComment(String username, Long postId, CommentRequestDTO request) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

                Post post = postRepository.findById(postId)
                                .orElseThrow(() -> new RuntimeException("Yazı bulunamadı."));

                LocalDateTime now = LocalDateTime.now();
                boolean isParentComment = (request.getParentId() == null);

                // 1. FUAYE SÜRESİ KONTROLÜ (Sadece ana mektuplar için geçerlidir)
                if (isParentComment) {
                        if (post.getDiscussionEndsAt() != null && now.isAfter(post.getDiscussionEndsAt())) {
                                throw new RuntimeException(
                                                "Bu yazının fuaye süresi dolmuştur. Artık yeni ana mektup yazılamaz, ancak mevcut mektuplara yanıt verebilirsiniz.");
                        }
                }

                Comment parentComment = null;
                // Eğer bu bir alt yanıtsa (reply), parent yorumun varlığını ve geçerliliğini
                // kontrol et
                if (!isParentComment) {
                        parentComment = commentRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Yanıtlanmak istenen ana mektup bulunamadı."));

                        // Alt yanıtların da kendi içinde sonsuz katmanlı olmasını istemiyorsak kontrol
                        // edebiliriz
                        if (parentComment.getParent() != null) {
                                throw new RuntimeException("Alt yanıtlara tekrar yanıt yazılamaz.");
                        }
                }

                // 2. Yorum / Mektup Nesnesini Oluştur
                Comment comment = Comment.builder()
                                .content(request.getContent())
                                .post(post)
                                .user(user)
                                .parent(parentComment)
                                .build();

                Comment savedComment = commentRepository.save(comment);
                return convertToResponse(savedComment);
        }

        // Bir yazıya ait tüm ana mektupları ve onların alt yanıtlarını hiyerarşik getir
        @Transactional(readOnly = true)
        public List<CommentResponseDTO> getCommentsByPostId(Long postId) {
                // Sadece ana mektupları (parent_id is null) çekiyoruz, alt yanıtlar cascade ile
                // gelecek
                List<Comment> rootComments = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);

                return rootComments.stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        private CommentResponseDTO convertToResponse(Comment comment) {
                List<CommentResponseDTO> replyDtos = (comment.getReplies() != null)
                                ? comment.getReplies().stream()
                                                .map(this::convertToResponse)
                                                .collect(Collectors.toList())
                                : Collections.emptyList();

                return CommentResponseDTO.builder()
                                .id(comment.getId())
                                .content(comment.getContent())
                                .createdAt(comment.getCreatedAt())
                                .authorName(comment.getUser().getName())
                                .authorSurname(comment.getUser().getSurname())
                                .authorUsername(comment.getUser().getUsername())
                                .authorProfileImg(comment.getUser().getProfileImg())
                                .replies(replyDtos)
                                .build();
        }
}