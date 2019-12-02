package com.exam.dao;

import com.exam.bean.exam.ExamRecord;
import com.exam.bean.exam.QuestionForAnswer;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考试 Dao (MongoDB 使用)
 *
 * 提示:
 *     mongoTemplate.save()   : 如果 id 对应的 document 不存在则插入，存在则插入新的属性，替换非 null 属性
 *     mongoTemplate.upsert() : 查询不到复合条件的则插入，存在则替换 (如果 Update 使用 Document 创建则原来的所有属性都会被删除)
 *     mongoTemplate.find()   : 返回 List，查询不到返回空的 List
 *     mongoTemplate.findOne(): 返回查询到的对象，查询不到返回 null
 */
@Service
public class ExamDao {
    // 考试记录作答表: 复合唯一索引 (examRecordId, questionOptionId)
    private static final String QUESTION_ANSWER = "exam_question_answer";
    private static final String EXAM_RECORD     = "exam_record";
    private static final int ELAPSED_TIME_DELTA = 5; // 计时的时间差

    @Resource(name = "mongoTemplate")
    private MongoTemplate mongoTemplate;

    /**
     * 统计用户某次考试的考试记录数量
     *
     * @param userId 用户 ID
     * @param examId 考试 ID
     * @return 返回考试记录数量
     */
    public int countExamRecordsByUserIdAndExamId(long userId, long examId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("examId").is(examId);
        long count = mongoTemplate.count(Query.query(criteria), EXAM_RECORD);
        return (int) count;
    }

    /**
     * 查找用户在某次考试中已经做过的试卷
     *
     * @param userId 用户 ID
     * @param examId 考试 ID
     * @return 返回试卷 ID 的数组
     */
    public Set<Long> findPaperIdsByUserIdAndExamId(long userId, long examId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("examId").is(examId);
        Query query = Query.query(criteria);
        query.fields().include("paperId");

        List<ExamRecord> records = mongoTemplate.find(query, ExamRecord.class, EXAM_RECORD);
        Set<Long> paperIds = records.stream().map(ExamRecord::getPaperId).filter(id -> id > 0).collect(Collectors.toSet());

        return paperIds;
    }

    /**
     * 插入或者更新题目选项的作答
     *
     * @param answer 作答
     */
    public void upsertQuestionAnswer(QuestionForAnswer answer) {
        Document document = new Document()
                .append("examRecordId", answer.getExamRecordId())
                .append("questionId", answer.getQuestionId())
                .append("answers", answer.getAnswers());

        // 更新条件: 指定考试记录的选项
        Query condition = Query.query(Criteria
                .where("examRecordId").is(answer.getExamRecordId())
                .and("questionId").is(answer.getQuestionId())
        );

        mongoTemplate.upsert(condition, Update.fromDocument(document), QUESTION_ANSWER);
    }

    /**
     * 查询考试记录的作答
     *
     * @param examRecordId 考试记录 ID
     * @return 返回作答的数组
     */
    public List<QuestionForAnswer> findQuestionForAnswersByExamRecordId(long examRecordId) {
        Criteria criteria = Criteria.where("examRecordId").is(examRecordId);
        List<QuestionForAnswer> answers = mongoTemplate.find(Query.query(criteria), QuestionForAnswer.class, QUESTION_ANSWER);

        return answers;
    }

    /**
     * 插入或者更新考试记录
     *
     * @param record 考试记录
     */
    public void upsertExamRecord(ExamRecord record) {
        mongoTemplate.save(record, EXAM_RECORD);
    }

    /**
     * 查询考试记录
     *
     * @param examRecordId 考试记录 ID
     * @return 返回查询得到的考试记录，查询不到返回 null
     */
    public ExamRecord findExamRecordById(long examRecordId) {
        Criteria criteria = Criteria.where("id").is(examRecordId);
        ExamRecord record = mongoTemplate.findOne(Query.query(criteria), ExamRecord.class, EXAM_RECORD);

        return record;
    }

    /**
     * 查找用户的指定考试的所有考试记录
     *
     * @param userId 用户 ID
     * @param examId 考试 ID
     * @return 返回查找到的考试记录数组
     */
    public List<ExamRecord> findExamRecordsByUserIdAndExamId(long userId, long examId) {
        Criteria criteria = Criteria.where("userId").is(userId).and("examId").is(examId);
        Query query = Query.query(criteria);
        query.fields().exclude("questions"); // 忽略题目的作答
        List<ExamRecord> records = mongoTemplate.find(query, ExamRecord.class, EXAM_RECORD);

        return records;
    }

    /**
     * 增加考试记录的使用时间
     *
     * @param examRecordId  考试记录 ID
     * @param timeInSeconds 增加的时间
     */
    public void increaseExamRecordElapsedTime(long examRecordId, int timeInSeconds) {
        // 1. 查询考试记录的 elapsedTime 和 tickAt
        // 2. 如果 tickAt + timeInSeconds <= 当前时间则增加时间

        Criteria criteria = Criteria.where("_id").is(examRecordId); // 奇怪: 使用 id 可以查询到，但是更新不行，使用 _id 的话都可以

        // [1] 查询考试记录的 elapsedTime 和 tickAt
        Query query = Query.query(criteria);
        query.fields().include("tickAt").include("elapsedTime");
        ExamRecord record = mongoTemplate.findOne(query, ExamRecord.class, EXAM_RECORD);

        if (record == null) { return; }

        long will = record.getTickAt().getTime() + (timeInSeconds-ELAPSED_TIME_DELTA)*1000L; // 下次最早可计时时间
        long now  = System.currentTimeMillis(); // 当前时间

        // [2] 如果 tickAt + timeInSeconds <= 当前时间则增加时间
        if (will <= now) {
            Update update = new Update().set("elapsedTime", record.getElapsedTime() + timeInSeconds).set("tickAt", new Date());
            mongoTemplate.updateFirst(Query.query(criteria), update, EXAM_RECORD);
        }
    }
}