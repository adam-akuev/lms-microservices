package com.lms.mapper;

import com.lms.dto.review.ReviewRequestDto;
import com.lms.dto.review.ReviewResponseDto;
import com.lms.model.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewResponseDto toResponseDto(Review review);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "studentId", source = "studentId")
    @Mapping(target = "courseId", source = "courseId")
    @Mapping(target = "rating", source = "requestDto.rating")
    @Mapping(target = "text", source = "requestDto.text")
    Review toEntity(ReviewRequestDto requestDto, Long studentId, Long courseId);
}
