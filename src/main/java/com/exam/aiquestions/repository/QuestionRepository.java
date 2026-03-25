package com.exam.aiquestions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.exam.aiquestions.model.Question;
import com.exam.aiquestions.model.QuestionType;
import com.exam.aiquestions.model.ReviewStatus;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    boolean existsByQuestionHash(String questionHash);

    @Query("""
            SELECT q
            FROM Question q
            WHERE (:questionType IS NULL OR q.questionType = :questionType)
              AND (:reviewStatus IS NULL OR q.reviewStatus = :reviewStatus)
              AND (:topic IS NULL OR q.topic = :topic)
            ORDER BY q.createdAt DESC
            """)
    List<Question> findForBank(
            @Param("questionType") QuestionType questionType,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            @Param("topic") String topic);
}
