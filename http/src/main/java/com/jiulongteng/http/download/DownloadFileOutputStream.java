package com.jiulongteng.http.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import okio.Buffer;
import okio.Source;

public class DownloadFileOutputStream extends DownloadOutputStream{

    private FileChannel fileChannel;
    RandomAccessFile raf;
    ByteBuffer byteBuffer;
    public DownloadFileOutputStream(File file) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rwd");
        fileChannel = raf.getChannel();
        byteBuffer =  ByteBuffer.allocateDirect(8096);
//            fileChannel.position(blockInfo.getRangeLeft());
    }

    @Override
    void flushAndSync() throws IOException {
        raf.getFD().sync();
    }

    @Override
    void seek(long offset) throws IOException {
        raf.seek(offset);
    }

    @Override
    void setLength(long newLength) throws IOException {
        raf.setLength(newLength);
    }

    @Override
    public void write(int b) throws IOException {
    }

    @Override
    public void write(byte[] b) throws IOException {

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteBuffer.flip();
        byteBuffer.put(b,off,len);
        fileChannel.write(byteBuffer);
        byteBuffer.clear();
    }
}
