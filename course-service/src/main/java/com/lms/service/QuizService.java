package com.lms.service;

import com.lms.dto.quiz.*;
import com.lms.model.quiz.Quiz;
import com.lms.model.quiz.QuizOption;
import com.lms.model.quiz.QuizQuestion;
import com.lms.model.quiz.UserQuizAttempt;
import com.lms.repository.QuizRepository;
import com.lms.repository.UserQuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final UserQuizAttemptRepository attemptRepository;
    private final LessonProgressService progressService;

    private Quiz loadQuizWithDetails(Long courseId) {
        Quiz quiz = quizRepository.findByCourseIdWithQuestions(courseId)
                .orElseThrow(() -> new RuntimeException("Тест для данного курса не найден"));

        if (!quiz.getQuestions().isEmpty()) {
            quizRepository.fetchOptionsForQuizzes(List.of(quiz));
        }

        return quiz;
    }

    public StudentQuizDto getFinalQuiz(Long courseId, Long userId) {
        if (!progressService.isAllLessonsCompleted(userId, courseId)) {
            throw new IllegalStateException("Сначала пройдите все уроки курса!");
        }

        Quiz quiz = loadQuizWithDetails(courseId);

        var questionsDto = quiz.getQuestions().stream()
                .map(q -> new StudentQuizDto.StudentQuestionDto(
                        q.getId(),
                        q.getText(),
                        q.getOptions().stream()
                                .map(o -> new StudentQuizDto.StudentOptionDto(o.getId(), o.getText()))
                                .toList()
                ))
                .toList();

        return new StudentQuizDto(quiz.getId(), quiz.getTitle(), questionsDto);
    }

    @Transactional
    public QuizResultDto submitQuiz(Long courseId, Long userId, SubmitQuizRequest request) {
        Quiz quiz = loadQuizWithDetails(courseId);

        Map<Long, Long> correctAnswers = quiz.getQuestions().stream()
                .collect(Collectors.toMap(
                        QuizQuestion::getId,
                        q -> q.getOptions().stream()
                                .filter(QuizOption::getIsCorrect)
                                .map(QuizOption::getId)
                                .findFirst()
                                .orElse(-1L)
                ));

        int correctCount = 0;
        for (SubmitQuizRequest.AnswerDto answer : request.answers()) {
            Long correctOptionId = correctAnswers.get(answer.questionId());
            if (correctOptionId != null && correctOptionId.equals(answer.selectedOptionId())) {
                correctCount++;
            }
        }

        int totalQuestions = quiz.getQuestions().size();
        int score = totalQuestions == 0 ? 0 : (int) Math.round(((double) correctCount / totalQuestions) * 100);
        boolean isPassed = score >= quiz.getPassScore();

        UserQuizAttempt attempt = UserQuizAttempt.builder()
                .userId(userId)
                .quizId(quiz.getId())
                .score(score)
                .isPassed(isPassed)
                .build();
        attemptRepository.save(attempt);

        String message = isPassed
                ? "Вы успешно сдали финальный тест!"
                : "Тест не сдан. Попробуйте еще раз.";

        return new QuizResultDto(quiz.getId(), score, isPassed, message);
    }

    @Transactional
    public QuizResponse createQuiz(Long courseId, CreateQuizRequest request) {
        if (quizRepository.findByCourseId(courseId).isPresent()) {
            throw new IllegalStateException("Тест для данного курса уже существует");
        }

        Quiz quiz = buildQuizEntity(courseId, request);
        Quiz saved = quizRepository.save(quiz);
        return toQuizResponse(saved);
    }

    @Transactional
    public QuizResponse updateQuiz(Long courseId, CreateQuizRequest request) {
        Quiz quiz = loadQuizWithDetails(courseId);

        validateQuestions(request);

        quiz.setTitle(request.title());
        quiz.setPassScore(request.passScore());

        quiz.getQuestions().clear();

        request.questions().forEach(qReq -> {
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .text(qReq.text())
                    .build();

            qReq.options().forEach(oReq -> {
                QuizOption option = QuizOption.builder()
                        .question(question)
                        .text(oReq.text())
                        .isCorrect(oReq.isCorrect())
                        .build();
                question.getOptions().add(option);
            });

            quiz.getQuestions().add(question);
        });

        Quiz saved = quizRepository.save(quiz);
        return toQuizResponse(saved);
    }

    @Transactional
    public void deleteQuiz(Long courseId) {
        Quiz quiz = quizRepository.findByCourseId(courseId)
                .orElseThrow(() -> new RuntimeException("Тест для данного курса не найден"));

        quizRepository.delete(quiz);
    }

    public List<QuizAttemptResponse> getMyAttempts(Long courseId, Long userId) {
        Quiz quiz = quizRepository.findByCourseIdWithQuestions(courseId)
                .orElseThrow(() -> new RuntimeException("Тест для данного курса не найден"));

        return attemptRepository.findByUserIdAndQuizIdOrderByCreatedAtDesc(userId, quiz.getId()).stream()
                .map(a -> new QuizAttemptResponse(a.getId(), a.getQuizId(), a.getScore(), a.getIsPassed(), a.getCreatedAt()))
                .toList();
    }

    private Quiz buildQuizEntity(Long courseId, CreateQuizRequest request) {
        validateQuestions(request);

        Quiz quiz = Quiz.builder()
                .courseId(courseId)
                .title(request.title())
                .passScore(request.passScore())
                .build();

        request.questions().forEach(qReq -> {
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .text(qReq.text())
                    .build();

            qReq.options().forEach(oReq -> {
                QuizOption option = QuizOption.builder()
                        .question(question)
                        .text(oReq.text())
                        .isCorrect(oReq.isCorrect())
                        .build();
                question.getOptions().add(option);
            });

            quiz.getQuestions().add(question);
        });

        return quiz;
    }

    private void validateQuestions(CreateQuizRequest request) {
        request.questions().forEach(qReq -> {
            boolean hasCorrectOption = qReq.options().stream()
                    .anyMatch(CreateQuizRequest.OptionRequest::isCorrect);
            if (!hasCorrectOption) {
                throw new IllegalArgumentException(
                        "Вопрос \"%s\" должен иметь хотя бы один правильный вариант ответа".formatted(qReq.text())
                );
            }
        });
    }

    public QuizAdminDto getQuizForManagement(Long courseId) {
        Quiz quiz = loadQuizWithDetails(courseId);

        var questionsDto = quiz.getQuestions().stream()
                .map(q -> new QuizAdminDto.QuestionDto(
                        q.getId(),
                        q.getText(),
                        q.getOptions().stream()
                                .map(o -> new QuizAdminDto.OptionDto(o.getId(), o.getText(), o.getIsCorrect()))
                                .toList()
                ))
                .toList();

        return new QuizAdminDto(quiz.getId(), quiz.getTitle(), quiz.getPassScore(), questionsDto);
    }

    private QuizResponse toQuizResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getCourseId(),
                quiz.getTitle(),
                quiz.getPassScore(),
                quiz.getQuestions().size()
        );
    }
}