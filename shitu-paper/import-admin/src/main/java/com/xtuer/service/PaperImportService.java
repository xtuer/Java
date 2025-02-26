package com.xtuer.service;

import com.xtuer.bean.Paper;
import com.xtuer.mapper.ImporterMapper;
import com.xtuer.util.CommonUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 试卷导入服务
 */
@Service
public class PaperImportService {
    @Autowired
    private ImporterMapper mapper;

    /**
     * 导入试卷信息到数据库，并复制试卷到指定目录.
     *
     * @param subject
     * @param papers
     * @param destDirectory
     */
    @Transactional
    public void importPaper(String subject, List<File> papers, String destDirectory) {
        for (File paperFile : papers) {
            // name, uuid_name, original_name, real_directory_name, subject
            String name         = paperFile.getName();
            String originalName = name;
            String uuid         = CommonUtils.uuid();
            String uuidName     = uuid + "." + FilenameUtils.getExtension(name);
            String realDirectoryName = CommonUtils.directoryNameByUuid(uuid);

            Paper paper = new Paper();
            paper.setPaperId(uuid)
                    .setName(name)
                    .setOriginalName(name)
                    .setUuidName(uuidName)
                    .setRealDirectoryName(realDirectoryName)
                    .setSubject(subject);

            mapper.insertPaper(paper); // 插入试卷到数据库

            try {
                // 复制试卷到目录
                File finalDir = new File(destDirectory, realDirectoryName);
                FileUtils.copyFile(paperFile, new File(finalDir, uuidName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取目录下的试卷，试卷的扩展名都是 .doc 的 word 文件.
     *
     * @param paperDirectory 试卷的目录
     * @return 试卷的 list
     */
    public static List<File> listPapers(String paperDirectory) {
        paperDirectory = paperDirectory.trim();
        List<File> papers = Arrays.asList(new File(paperDirectory).listFiles((dir, name) -> name.toLowerCase().endsWith(".doc")));
        Collections.sort(papers, (a, b) -> a.getName().compareTo(b.getName())); // 对文件名进行排序

        return papers;
    }

    /**
     * 更新试卷的属性.
     *
     * @param subject 试卷所属学科
     * @param path 属性文件的路径
     * @throws Exception
     */
    @Transactional
    public void updatePapersMeta(String subject, String path) throws Exception {
        Reader reader = new InputStreamReader(new FileInputStream(path));
        Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader().parse(reader);

        for (CSVRecord record : records) {
            String paperName   = record.get("FixPaperName").trim();
            String paperYear   = record.get("PaperYear").trim();
            String paperRegion = record.get("PaperRegion").trim();
            String paperFrom   = record.get("PaperFrom").trim();
            String paperType   = record.get("PaperType").trim();
            String description = record.get("memo").trim();
            // String originalPaperId = record.get("FixPaperID").trim(); // 读取这一列会报错，没找到为啥

            Paper paper = new Paper();
            paper.setOriginalName(paperName + ".doc")
                    .setPublishYear(paperYear)
                    .setRegion(paperRegion)
                    .setPaperFrom(paperFrom)
                    .setPaperType(paperType)
                    .setSubject(subject)
                    .setDescription(description);

            mapper.updatePaperMeta(paper);
        }
    }
}
