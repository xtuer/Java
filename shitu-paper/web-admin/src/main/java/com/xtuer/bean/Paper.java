package com.xtuer.bean;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

@Getter
@Setter
@Accessors(chain=true)
public class Paper {
    private String paperId;

    @NotBlank(message="试卷 名字不能为空")
    private String name;

    private String uuidName;
    private String originalName;
    private String paperDirectoryId;
    private String realDirectoryName;
    private String subject;
    private String publishYear;
    private String region;
    private String paperFrom;
    private String paperType;
    private String description;
    private String originalPaperId; // 试卷原数据库中的 FixPaperId

    private int status; // 审核状态: 0: 未处理(默认值)，1: 已通过，2: 未通过

    private List<KnowledgePoint> knowledgePoints; // 知识点
}
