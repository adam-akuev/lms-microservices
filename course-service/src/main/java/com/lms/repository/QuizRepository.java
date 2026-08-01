package com.lms.repository;

import com.lms.model.quiz.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("""
        SELECT DISTINCT q FROM Quiz q
        LEFT JOIN FETCH q.questions
        WHERE q.courseId = :courseId
    """)
    Optional<Quiz> findByCourseIdWithQuestions(@Param("courseId") Long courseId);

    @Query("""
        SELECT DISTINCT q FROM Quiz q
        LEFT JOIN FETCH q.questions qn
        LEFT JOIN FETCH qn.options
        WHERE q IN :quizzes
    """)
    void fetchOptionsForQuizzes(@Param("quizzes") List<Quiz> quizzes);

    Optional<Quiz> findByCourseId(Long courseId);
}