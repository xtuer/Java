import com.google.common.base.Stopwatch;
import com.xtuer.Application;
import com.xtuer.bean.UFile;
import com.xtuer.bean.UFileChunk;
import com.xtuer.bean.UFileConst;
import com.xtuer.service.UFileRepo;
import com.xtuer.service.UFileService;
import com.xtuer.util.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = { Application.class })
public class UFileTest {
    @Autowired
    private UFileService service;

    @Autowired
    private UFileRepo repo;

    @Test
    public void beanTest() {
        UFile ufile = new UFile();
        ufile.setFileUid("file-1");
        ufile.setFileName("foo.txt");
        ufile.getChunks().add(new UFileChunk());

        Utils.dump(ufile);
    }

    @Test
    public void updateUFileState() {
        repo.updateUFileState("xyz", UFileConst.FAILED);
    }

    @Test
    public void updateUFileChunkState() {
        repo.updateUFileChunkState("xyz", 1, UFileConst.SUCCESS);
    }

    @Test
    public void testFile() throws FileNotFoundException {
        Stopwatch watch = Stopwatch.createStarted();
        String md5 = Utils.md5(new File("/Users/biao/Downloads/temp/win11-arm.iso"));

        System.out.printf("MD5 [%s], 耗时 [%d] 毫秒", md5, watch.elapsed(TimeUnit.MILLISECONDS));
    }
}
