package com.sahnesen.api.sahnesen.services;

import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sahnesen.api.sahnesen.entities.Follow;
import com.sahnesen.api.sahnesen.entities.User;
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

    private static final String FOLLOWERS_COUNT_KEY = "user:followers:count:";
    private static final String FOLLOWING_COUNT_KEY = "user:following:count:";

    public Follow followUser(String followerUsername, Long followingId) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Takip eden kullanıcı bulunamadı."));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("Takip edilen kullanıcı bulunamadı."));

        if (follower.getId().equals(followingId)) {
            throw new RuntimeException("Kendinizi takip edemezsiniz.");
        }

        if (followRepository.findByFollowerIdAndFollowingId(follower.getId(), followingId).isPresent()) {
            throw new RuntimeException("Zaten bu kullanıcıyı takip ediyorsunuz.");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        Follow savedFollow = followRepository.save(follow);

        // REDIS COUNTER UPDATE (Atomic)
        // Takip edilenin takipçi sayısını artır
        redisTemplate.opsForValue().increment(FOLLOWERS_COUNT_KEY + followingId);
        // Takip edenin takip ettiklerini artır
        redisTemplate.opsForValue().increment(FOLLOWING_COUNT_KEY + follower.getId());

        log.info(followerUsername + " artık " + following.getUsername() + " kullanıcısını takip ediyor.");

        return savedFollow;
    }

    @Transactional
    public void unfollowUser(String followerUsername, Long followingId) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));
        Follow follow = followRepository.findByFollowerIdAndFollowingId(follower.getId(), followingId)
                .orElseThrow(() -> new RuntimeException("Takip ilişkisi bulunumadı."));

        followRepository.delete(follow);

        // Redis'teki takipçi ve takip edilen sayısını güncelle
        redisTemplate.opsForValue().decrement(FOLLOWERS_COUNT_KEY + followingId);
        redisTemplate.opsForValue().decrement(FOLLOWING_COUNT_KEY + follower.getId());
    }

    public Map<String, Long> getFollowStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı."));

        // Önce Redis'ten almayı dene
        Integer followers = (Integer) redisTemplate.opsForValue().get(FOLLOWERS_COUNT_KEY + user.getId());
        Integer following = (Integer) redisTemplate.opsForValue().get(FOLLOWING_COUNT_KEY + user.getId());

        // Eğer Redis boşsa (Cache Miss), DB'den say ve Redis'i doldur
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

    public boolean isFollowing(String followerUsername, Long followingId) {
        return userRepository.findByUsername(followerUsername)
                .map(user -> followRepository.findByFollowerIdAndFollowingId(user.getId(), followingId).isPresent())
                .orElse(false);
    }

}
