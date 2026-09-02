package com.sahnesen.api.sahnesen.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.dto.FollowDTO;
import com.sahnesen.api.sahnesen.entities.Follow;
import com.sahnesen.api.sahnesen.entities.User;
import com.sahnesen.api.sahnesen.enums.BadgeCategory;
import com.sahnesen.api.sahnesen.repository.FollowRepository;
import com.sahnesen.api.sahnesen.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final BadgeService badgeService;

    private static final String FOLLOWERS_COUNT_KEY = "user:followers:count:";
    private static final String FOLLOWING_COUNT_KEY = "user:following:count:";

    @Transactional
    public Follow followUser(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Takip eden kullanıcı bulunamadı."));

        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new RuntimeException("Takip edilen kullanıcı bulunamadı."));

        if (follower.getId().equals(following.getId())) {
            throw new RuntimeException("Kendinizi takip edemezsiniz.");
        }

        if (followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId()).isPresent()) {
            throw new RuntimeException("Zaten bu kullanıcıyı takip ediyorsunuz.");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        Follow savedFollow = followRepository.save(follow);

        // REDIS COUNTER UPDATE (Atomic)
        Long newFollowerCount = redisTemplate.opsForValue().increment(FOLLOWERS_COUNT_KEY + following.getId());
        redisTemplate.opsForValue().increment(FOLLOWING_COUNT_KEY + follower.getId());

        log.info(followerUsername + " artık " + followingUsername + " kullanıcısını takip ediyor.");

        if (newFollowerCount != null) {
            badgeService.checkAndAssignBadges(following.getId(), BadgeCategory.FOLLOWER, newFollowerCount.intValue());
        }

        return savedFollow;
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new RuntimeException("Takip edilecek kullanıcı bulunamadı."));

        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), following.getId())
                .orElseThrow(() -> new RuntimeException("Takip ilişkisi bulunamadı."));

        followRepository.delete(follow);

        // Redis'teki sayaçları güncelle
        redisTemplate.opsForValue().decrement(FOLLOWERS_COUNT_KEY + following.getId());
        redisTemplate.opsForValue().decrement(FOLLOWING_COUNT_KEY + follower.getId());
    }

    public Map<String, Long> getFollowStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        Integer followers = (Integer) redisTemplate.opsForValue().get(FOLLOWERS_COUNT_KEY + user.getId());
        Integer following = (Integer) redisTemplate.opsForValue().get(FOLLOWING_COUNT_KEY + user.getId());

        if (followers == null) {
            long count = followRepository.countByFollowingId(user.getId());
            redisTemplate.opsForValue().set(FOLLOWERS_COUNT_KEY + user.getId(), (int) count);
            followers = (int) count;
        }

        if (following == null) {
            long count = followRepository.countByFollowerId(user.getId());
            redisTemplate.opsForValue().set(FOLLOWING_COUNT_KEY + user.getId(), (int) count);
            following = (int) count;
        }

        return Map.of(
                "followerCount", followers.longValue(),
                "followingCount", following.longValue());
    }

    public boolean isFollowing(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername).orElse(null);
        User following = userRepository.findByUsername(followingUsername).orElse(null);

        if (follower == null || following == null)
            return false;

        return followRepository.existsByFollowerIdAndFollowingId(follower.getId(), following.getId());
    }

    public List<FollowDTO> getFollowersByUsername(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        Page<Follow> followPage = followRepository.findByFollowingId(user.getId(), pageable);

        return followPage.getContent().stream().map(follow -> {
            FollowDTO dto = new FollowDTO();
            dto.setId(follow.getId());
            dto.setUsername(follow.getFollower().getUsername());
            dto.setName(follow.getFollower().getName());
            dto.setSurname(follow.getFollower().getSurname());
            dto.setProfileImg(follow.getFollower().getProfileImg());
            dto.setFollowedAt(follow.getCreatedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    public List<FollowDTO> getFollowingByUsername(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        Page<Follow> followPage = followRepository.findByFollowerId(user.getId(), pageable);

        return followPage.getContent().stream().map(follow -> {
            FollowDTO dto = new FollowDTO();
            dto.setId(follow.getId());
            dto.setUsername(follow.getFollowing().getUsername());
            dto.setName(follow.getFollowing().getName());
            dto.setSurname(follow.getFollowing().getSurname());
            dto.setProfileImg(follow.getFollowing().getProfileImg());
            dto.setFollowedAt(follow.getCreatedAt());
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

}