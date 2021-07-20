package com.xtuer.service;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.github.wujun234.uid.impl.CachedUidGenerator;
import com.xtuer.config.AppConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

public class BaseService {
    @Autowired
    private CachedUidGenerator uidGenerator;

    @Autowired
    protected UserService userService;

    @Autowired
    protected TempFileService tempFileService;

    @Autowired
    protected RepoFileService repoFileService;

    @Autowired
    protected AppConfig config;

    @Autowired
    private CommonService commonService;

    /**
     * 生成唯一的 64 位 long 的 ID
     *
     * @return 返回唯一 ID
     */
    final public long nextId() {
        return uidGenerator.getUID();
    }

    /**
     * 按年生成下一个序列号，每年从一开始。
     *
     * 示例:
     * 生成订单号，传入 XSDD => XSDD-20201213-0001
     * 生成出库单，传入 PTD  => PTD-20201213-0001
     *
     * @param sequenceName 序列号的名字
     * @return 序列号
     */
    final public String nextSnByYear(String sequenceName) {
        // XSDD-20201213-0001: XSDD 不动, 20200806 为年月日，根据日期自动生成, 0001 为流水号 (0001,0002,003……)，每年再从 0001 开始
        String year = DateTimeFormatter.ofPattern("yyyy").format(LocalDate.now());
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDate.now());
        String name = sequenceName + "-" + year;
        String n    = commonService.nextSequence(name) + "";
        String sn   = sequenceName + "-" + date + "-" + StringUtils.leftPad(n, 4, "0");

        return sn;
    }

    /**
     * 导出的 Excel 文件 (存放在临时文件夹中)
     *
     * @param prefix 前缀
     * @return 返回 Excel 文件
     */
    public File exportedExcelFile(String prefix) {
        String sn = commonService.nextSequence("export-excel") + "";
        return tempFileService.getTempFile(prefix + "-" + sn + ".xls");
    }

    /**
     * 导出 Excel
     *
     * @param excelName excel 文件名
     * @param pojoClass 实体类
     * @param dataSet   数据集
     * @return 返回访问 Excel 文件的 URL
     * @throws IOException 导出异常
     */
    public String exportExcel(String excelName,  Class<?> pojoClass, Collection<?> dataSet) throws IOException {
        File excel = exportedExcelFile(excelName);
        Workbook book = ExcelExportUtil.exportExcel(new ExportParams(null, null), pojoClass, dataSet);

        try (OutputStream out = new FileOutputStream(excel)) {
            book.write(out);
        }

        return tempFileService.getTempFileUrl(excel);
    }
}
