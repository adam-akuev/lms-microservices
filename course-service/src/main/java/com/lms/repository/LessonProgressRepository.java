package com.lms.repository;

import com.lms.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);

    @Query("SELECT COUNT(lp) FROM LessonProgress lp " +
            "WHERE lp.studentId = :studentId " +
            "AND lp.completed = true " +
            "AND lp.lessonId IN (SELECT l.id FROM Lesson l WHERE l.course.id = :courseId)"
    )
    long countCompletedLessonsByCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
}
