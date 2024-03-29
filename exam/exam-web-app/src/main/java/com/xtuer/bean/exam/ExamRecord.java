package com.xtuer.bean.exam;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * 用户的考试记录，一个考试可以进行多次作答，每个作答即为一个考试记录
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonIgnoreProperties({ "questions" })
@Document
public class ExamRecord {
    // 考试记录状态
    public static final int STATUS_ANSWERED         = 0; // 已作答
    public static final int STATUS_SUBMITTED        = 1; // 已提交
    public static final int STATUS_CORRECTED_AUTO   = 2; // 自动批改
    public static final int STATUS_CORRECTED_MANUAL = 3; // 手动批改
    public static final int STATUS_CORRECTED_FINISH = 4; // 批改结束
    public static final String[] STATUS_LABELS      = { "已作答", "已提交", "自动批改", "手动批改", "批改结束" };

    @Id private long id;      // 试卷记录 ID
    private long userId;      // 考试用户 ID
    private long examId;      // 考试 ID
    private long paperId;     // 试卷 ID，方便使用考试记录查找考试的试卷
    private int  status;      // 状态: 0 (已作答)、1 (已提交)、2 (自动批改)、3 (手动批改)、4 (批改结束) [点击考试的时候才创建考试记录]
    private int  elapsedTime; // 已考试时间，单位为秒
    private String username;  // 用户账号
    private String nickname;  // 用户昵称
    private double  score;    // 考试得分
    private double  totalScore;  // 满分
    private boolean objective;   // true (使用的试卷全是客观题)、false (使用的试卷包含主观题)
    private Date    submittedAt; // 提交试卷时间
    private Date    tickAt;      // 打卡时间
    private Date    startAt;     // 开始时间
    private double objectiveScore;  // 客观题得分
    private double subjectiveScore; // 主观题得分

    private List<QuestionWithAnswer> questions = new LinkedList<>(); // 考试记录题目的作答 (不返回给前端，但要保存到 MongoDB exam_record 中)

    @Transient private Exam exam;   // 考试 (不保存到 Mongo)
    @Transient private Paper paper; // 试卷 (不保存到 Mongo)

    /**
     * 获取考试记录的状态 label
     *
     * @return 返回状态的 label
     */
    public String getStatusLabel() {
        if (status < 0 || status >= STATUS_LABELS.length) {
            return "未定义";
        } else {
            return STATUS_LABELS[status];
        }
    }
}
