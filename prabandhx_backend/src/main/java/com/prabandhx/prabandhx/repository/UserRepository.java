package com.prabandhx.prabandhx.repository;

import com.prabandhx.prabandhx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    // ===== NEW METHODS FOR LEADERBOARD =====
    
    @Query("SELECT u FROM User u ORDER BY u.totalPoints DESC")
    List<User> findTopByOrderByTotalPointsDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u ORDER BY u.currentStreak DESC")
    List<User> findTopByOrderByCurrentStreakDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.currentStreak > 0 ORDER BY u.currentStreak DESC")
    List<User> findUsersWithStreak(Pageable pageable);
    
    // Default methods for easy calling
    default List<User> findTopPerformers(int limit) {
        return findTopByOrderByTotalPointsDesc(PageRequest.of(0, limit));
    }
    
    default List<User> findTopStreakers(int limit) {
        return findTopByOrderByCurrentStreakDesc(PageRequest.of(0, limit));
    }
}