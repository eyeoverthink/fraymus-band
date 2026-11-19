package com.eyeoverthink.band;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes one channel of 16-bit sound to a WAV file as it is made. Its one byte buffer is claimed up front. */
public final class WavWriter implements Closeable {
    private final RandomAccessFile file;
    private final byte[] bytes = new byte[Audio.BLOCK * 2];
    private long samples;
    private float peak;

    public WavWriter(Path path) throws IOException {
        if (path.getParent() != null) Files.createDirectories(path.getParent());
        Files.deleteIfExists(path);
        file = new RandomAccessFile(path.toFile(), "rw");
        file.write(new byte[44]);            // room for the header, filled in when the take is over
    }

    public void write(float[] block, int n) throws IOException {
        for (int i = 0; i < n; i++) {
            float v = block[i];
            if (Math.abs(v) > peak) peak = Math.abs(v);
            int s = Math.round(Math.max(-1f, Math.min(1f, v)) * 32767f);
            bytes[2 * i] = (byte) s;
            bytes[2 * i + 1] = (byte) (s >> 8);
        }
        file.write(bytes, 0, n * 2);
        samples += n;
    }

    public long samples() { return samples; }
    public float peak() { return peak; }

    @Override
    public void close() throws IOException {
        long data = samples * 2;
        file.seek(0);
        file.writeBytes("RIFF"); le32(36 + data); file.writeBytes("WAVE");
        file.writeBytes("fmt "); le32(16); le16(1); le16(1); le32(Audio.RATE); le32(Audio.RATE * 2L); le16(2); le16(16);
        file.writeBytes("data"); le32(data);
        file.close();
    }

    private void le32(long v) throws IOException { file.write((int) v); file.write((int) (v >> 8)); file.write((int) (v >> 16)); file.write((int) (v >> 24)); }
    private void le16(int v) throws IOException { file.write(v); file.write(v >> 8); }
}
