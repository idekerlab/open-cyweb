package edu.ucsd.idekerlab.opencyweb;

import java.io.OutputStream;

/**
 * A lightweight {@link OutputStream} that counts bytes written without storing any data. Used by
 * {@link OpenInCytoscapeWebTaskFactoryImpl#measureCx2ExportSize} to measure the CX2 export size of
 * a network without allocating heap memory for the serialized content.
 */
public class CountingOutputStream extends OutputStream {

    private long byteCount;

    @Override
    public void write(int b) {
        byteCount++;
    }

    @Override
    public void write(byte[] b, int off, int len) {
        byteCount += len;
    }

    public long getByteCount() {
        return byteCount;
    }
}
