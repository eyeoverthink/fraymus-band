package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * What a player's ear makes of a recording. This does not teach anyone anything. It plays a WAV file past
 * the same ear the players use and writes down what that ear noticed, so we can see what a real recording
 * looks like to them before asking them to learn from one. The file must be mono, 16-bit, 44,100 Hz.
 */
public final class Hearing {
    /** What the ear noticed. Times are in seconds. */
    public record Heard(double seconds, int hits, int bright, int dark, double pulseSeconds, double pulseStrength,
                        double[] hitTimes, boolean[] hitBright, double[] hitLoud, double loudest) {
        public double bpm() { return pulseSeconds <= 0 ? 0 : 60.0 / pulseSeconds; }
    }

    public static float[] read(Path wav) throws IOException {
        byte[] b = Files.readAllBytes(wav);
        ByteBuffer h = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        if (b.length < 44 || b[0] != 'R' || b[8] != 'W') throw new IOException(wav + " is not a WAV file");
        int at = 12, dataAt = -1, dataLength = 0, channels = 0, rate = 0, bits = 0;
        while (at + 8 <= b.length) {                         // walk the chunks; a WAV can carry more than sound
            String id = new String(b, at, 4);
            int length = h.getInt(at + 4);
            if (id.equals("fmt ")) { channels = h.getShort(at + 10); rate = h.getInt(at + 12); bits = h.getShort(at + 22); }
            if (id.equals("data")) { dataAt = at + 8; dataLength = Math.min(length, b.length - dataAt); break; }
            at += 8 + length + (length & 1);
        }
        if (dataAt < 0 || channels != 1 || rate != Audio.RATE || bits != 16)
            throw new IOException(wav + " must be mono, 16-bit, " + Audio.RATE + " Hz (it is " + channels + " channel, " + bits + "-bit, " + rate + " Hz)");
        float[] sound = new float[dataLength / 2];
        for (int i = 0; i < sound.length; i++) sound[i] = h.getShort(dataAt + 2 * i) / 32768f;
        return sound;
    }

    public static Heard listen(float[] sound) {
        Ear ear = new Ear();
        int most = sound.length / (Audio.RATE / 20) + 16;    // the ear cannot notice hits closer together than this
        double[] times = new double[most], loud = new double[most];
        boolean[] bright = new boolean[most];
        float[] block = new float[Audio.BLOCK];
        int hits = 0, brights = 0;
        long seen = 0;
        double loudest = 0;
        for (int from = 0; from + Audio.BLOCK <= sound.length; from += Audio.BLOCK) {
            System.arraycopy(sound, from, block, 0, Audio.BLOCK);
            ear.listen(block, Audio.BLOCK, from);
            if (ear.hitsEver() == seen) continue;
            seen = ear.hitsEver();
            times[hits] = Audio.seconds(ear.hitTime());
            bright[hits] = ear.hitBright();
            loud[hits] = ear.hitLoud();
            loudest = Math.max(loudest, loud[hits]);
            if (bright[hits]) brights++;
            hits++;
        }
        // The pulse: which gap, between any two hits up to two seconds apart, comes up most often?
        int[] count = new int[400];                          // 5 ms steps
        for (int i = 0; i < hits; i++)
            for (int j = i + 1; j < hits && times[j] - times[i] < 2.0; j++) count[(int) ((times[j] - times[i]) / 0.005)]++;
        int best = 0;
        for (int k = 66; k <= 200; k++)                      // a pulse between 60 and 180 to the minute
            if (count[k - 1] + count[k] + count[k + 1] > count[Math.max(0, best - 1)] + count[best] + count[Math.min(399, best + 1)]) best = k;
        double near = 0, weight = 0;                         // the middle of that pile, finer than 5 ms
        for (int i = 0; i < hits; i++)
            for (int j = i + 1; j < hits && times[j] - times[i] < 2.0; j++) {
                double gap = times[j] - times[i];
                if (Math.abs(gap - (best + 0.5) * 0.005) < 0.0125) { near += gap; weight++; }
            }
        double total = 0;
        for (int c : count) total += c;
        return new Heard(Audio.seconds(sound.length), hits, brights, hits - brights, weight == 0 ? 0 : near / weight,
            total == 0 ? 0 : (count[Math.max(0, best - 1)] + count[best] + count[Math.min(399, best + 1)]) / total,
            java.util.Arrays.copyOf(times, hits), java.util.Arrays.copyOf(bright, hits), java.util.Arrays.copyOf(loud, hits), loudest);
    }

    /** One bar, as the ear makes it out from the whole recording. */
    public record Bar(double bpm, double linesUp, int barsHeard, String bar, double[] pushMs, double[] weight,
                      double[] darkPerBar, double[] brightPerBar) {
        public Lesson asLesson(String name) { return new Lesson(name, bpm, bar, pushMs, weight); }
    }

    /**
     * Fold everything the ear noticed onto one bar of sixteen places. It finds the steady pulse the hits line
     * up on best, lays a grid of sixteenths over the whole recording, and takes the place where the dark
     * hits are heaviest as the one. A place becomes a pot if a dark hit fell there in at least a third of
     * the bars, a pan if a bright hit did in at least half. This is the ear's own reduction of a full mix:
     * dark is whatever is low (kick, bass), bright is everything else (snare, hats, voices, keys).
     */
    public static Bar oneBar(Heard h) {
        double[] t = h.hitTimes();
        double bestBpm = 0, bestScore = -1, bestAngle = 0;
        for (double bpm = 60; bpm <= 180; bpm += 0.01) {     // which tempo do the hits line up on best?
            double tick = 60.0 / bpm / 4, re = 0, im = 0, w = 0;
            for (int i = 0; i < t.length; i++) {
                double angle = 2 * Math.PI * (t[i] % tick) / tick;
                re += h.hitLoud()[i] * Math.cos(angle); im += h.hitLoud()[i] * Math.sin(angle); w += h.hitLoud()[i];
            }
            double score = w == 0 ? 0 : Math.hypot(re, im) / w;
            if (score > bestScore) { bestScore = score; bestBpm = bpm; bestAngle = Math.atan2(im, re); }
        }
        double tick = 60.0 / bestBpm / 4, first = (bestAngle < 0 ? bestAngle + 2 * Math.PI : bestAngle) / (2 * Math.PI) * tick;
        int n = Musician.SLOTS;
        double[] dark = new double[n], bright = new double[n], darkLoud = new double[n], brightLoud = new double[n], darkOff = new double[n], brightOff = new double[n];
        for (int i = 0; i < t.length; i++) {
            long which = Math.round((t[i] - first) / tick);
            double off = t[i] - (first + which * tick);
            if (Math.abs(off) > 0.030) continue;             // too far from any tick to be a place
            int place = (int) ((which % n + n) % n);
            if (h.hitBright()[i]) { bright[place]++; brightLoud[place] += h.hitLoud()[i]; brightOff[place] += off; }
            else { dark[place]++; darkLoud[place] += h.hitLoud()[i]; darkOff[place] += off; }
        }
        int one = 0;                                         // the one: where the dark hits weigh most
        for (int p = 1; p < n; p++) if (darkLoud[p] > darkLoud[one]) one = p;
        int bars = Math.max(1, (int) (h.seconds() / (tick * n)));
        char[] bar = new char[n];
        double[] push = new double[n], weight = new double[n], darkPer = new double[n], brightPer = new double[n];
        double heaviest = 1e-9;
        for (int p = 0; p < n; p++) heaviest = Math.max(heaviest, Math.max(dark[p] == 0 ? 0 : darkLoud[p] / dark[p], bright[p] == 0 ? 0 : brightLoud[p] / bright[p]));
        for (int k = 0; k < n; k++) {
            int p = (k + one) % n;
            darkPer[k] = dark[p] / bars; brightPer[k] = bright[p] / bars;
            boolean pot = k == 0 || darkPer[k] >= 1.0 / 3, pan = !pot && brightPer[k] >= 0.5;
            bar[k] = pot ? 'p' : pan ? 'n' : '.';
            if (pot && dark[p] > 0) { push[k] = darkOff[p] / dark[p] * 1000; weight[k] = darkLoud[p] / dark[p] / heaviest; }
            if (pan) { push[k] = brightOff[p] / bright[p] * 1000; weight[k] = brightLoud[p] / bright[p] / heaviest / Kit.PAN_AGAINST_POT; }
        }
        double shift = push[0];                              // feel is measured from the one
        for (int k = 0; k < n; k++) {
            if (bar[k] == '.') { push[k] = 0; weight[k] = 0; continue; }
            push[k] = Math.max(-30, Math.min(30, push[k] - shift));
            weight[k] = k == 0 ? 1.0 : Math.max(0.2, Math.min(0.85, weight[k]));
        }
        return new Bar(bestBpm, bestScore, bars, new String(bar), push, weight, darkPer, brightPer);
    }
}
