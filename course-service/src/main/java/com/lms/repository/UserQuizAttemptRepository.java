package com.lms.repository;

import com.lms.model.quiz.UserQuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserQuizAttemptRepository extends JpaRepository<UserQuizAttempt, Long> {

    List<UserQuizAttempt> findByUserIdAndQuizIdOrderByCreatedAtDesc(Long userId, Long quizId);

    Optional<UserQuizAttempt> findFirstByUserIdAndQuizIdAndIsPassedTrue(Long userId, Long quizId);
}