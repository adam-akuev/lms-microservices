package com.lms.repository;

import com.lms.model.Course;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CourseSpecification {

    public static Specification<Course> hasSearchQuery(String query) {
        return (root, criteriaQuery, cb) -> {
            if (!StringUtils.hasText(query)) {
                return cb.conjunction();
            }

            String pattern = "%" + query.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Course> hasCategoryId(Long categoryId) {
        return (root, criteriaQuery, cb) -> {
                if (categoryId == null) {
                    return cb.conjunction();
                }
                return cb.equal(root.get("category").get("id"), categoryId);
        };
    }
}