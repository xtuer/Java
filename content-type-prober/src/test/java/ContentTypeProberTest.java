import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ContentTypeProberTest {
    @Test
    public void probeTest() throws IOException {
        Assert.assertEquals("text/plain", Files.probeContentType(Paths.get("foo/test.txt")));
        Assert.assertEquals("application/vndms-excel", Files.probeContentType(Paths.get("foo/test.xls")));
        Assert.assertNull(Files.probeContentType(Paths.get("foo/test.bib")));
        Assert.assertNull(Files.probeContentType(Paths.get("foo/txt")));
    }
}
