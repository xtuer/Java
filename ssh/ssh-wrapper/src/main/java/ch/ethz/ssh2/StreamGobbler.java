package ch.ethz.ssh2;

import java.io.IOException;
import java.io.InputStream;

public class StreamGobbler extends InputStream {
    private InputStream is;

    public StreamGobbler(InputStream is) {
        this.is = is;
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int available() throws IOException {
        return is.available();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.is.read(b, 0, b.length);
    }

    @Override
    public void close() throws IOException {
        this.is.close();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.is.read(b, off, len);
    }
}
