package com.xtuer.mapper.exam;

import com.xtuer.bean.exam.Question;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 题目的 Mapper
 */
@Mapper
public interface QuestionMapper {
    /**
     * 查找指定 ID 的题目: 复合题时包含了小题，所以返回类型使用数组，需要把小题合并到大题的 subQuestions 中后才能使用
     *
     * [注意]: 不要直接使用这个方法，应该使用 QuestionService.findQuestionById(questionId)
     *
     * @param questionId 问题的 ID
     * @return 返回查找到的问题
     */
    List<Question> findQuestionById(long questionId);

    /**
     * 插入或者更新题目
     *
     * @param question 题目
     */
    void upsertQuestion(Question question);

    /**
     * 删除题目
     * 同时会删除题目的选项，题目的小题以及小题的选项
     *
     * @param questionId 题目 ID
     */
    void deleteQuestion(long questionId);
}
