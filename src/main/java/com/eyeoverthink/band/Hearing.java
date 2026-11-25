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

    /**
     * Hear the bottom of the sound and the top of it separately, each with its own ear, the way a listener
     * picks the kick out from under a singer. Everything below about 150 Hz goes to one ear and is called
     * dark; everything above about 5,000 Hz goes to the other and is called bright. What lies between
     * (voices, keys, most of a guitar) goes to neither. Each part is turned up to the same level first, so a
     * quiet kick under a loud mix is still heard.
     */
    public static Heard listenLowAndHigh(float[] sound) {
        float[] low = new float[sound.length], high = new float[sound.length];
        double a = 1 - Math.exp(-2 * Math.PI * 150.0 / Audio.RATE), b = 1 - Math.exp(-2 * Math.PI * 5000.0 / Audio.RATE);
        double l1 = 0, l2 = 0, l3 = 0, h1 = 0, h2 = 0, h3 = 0;
        float lowTop = 1e-9f, highTop = 1e-9f;
        for (int i = 0; i < sound.length; i++) {
            l1 += a * (sound[i] - l1); l2 += a * (l1 - l2); l3 += a * (l2 - l3);          // three gentle passes: keep the bottom
            low[i] = (float) l3;
            h1 += b * (sound[i] - h1); double x1 = sound[i] - h1;                          // three gentle passes: keep the top
            h2 += b * (x1 - h2); double x2 = x1 - h2;
            h3 += b * (x2 - h3);
            high[i] = (float) (x2 - h3);
            lowTop = Math.max(lowTop, Math.abs(low[i])); highTop = Math.max(highTop, Math.abs(high[i]));
        }
        for (int i = 0; i < sound.length; i++) { low[i] *= 0.8f / lowTop; high[i] *= 0.8f / highTop; }
        Heard bottom = listen(low), top = listen(high);
        int n = bottom.hits() + top.hits(), i = 0, j = 0;
        double[] times = new double[n], loud = new double[n];
        boolean[] bright = new boolean[n];
        for (int k = 0; k < n; k++) {                        // one list, in the order they happened
            boolean fromTop = i >= bottom.hits() || (j < top.hits() && top.hitTimes()[j] < bottom.hitTimes()[i]);
            times[k] = fromTop ? top.hitTimes()[j] : bottom.hitTimes()[i];
            loud[k] = fromTop ? top.hitLoud()[j++] : bottom.hitLoud()[i++];
            bright[k] = fromTop;
        }
        return new Heard(bottom.seconds(), n, top.hits(), bottom.hits(), bottom.pulseSeconds(), bottom.pulseStrength(), times, bright, loud, 0.8);
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
    /**
     * Swing, as a drum machine counts it: of each pair of sixteenths, how much of the pair's time goes by
     * before the second one. 50 is straight; 66.7 puts the second sixteenth on the triplet. It is worked out
     * from how late the places played off the eighths are, against the places played on them. -1 if nothing
     * is played off the eighths, or nothing on them.
     */
    public static double swing(String bar, double[] afterTheTickMs, double bpm) {
        if (bar.length() != Musician.SLOTS) return -1;       // swing is a matter of sixteenths
        double on = 0, off = 0;
        int ons = 0, offs = 0;
        for (int i = 0; i < bar.length() && i < afterTheTickMs.length; i++) {
            if (bar.charAt(i) == '.') continue;
            if (i % 2 == 1) { off += afterTheTickMs[i]; offs++; } else { on += afterTheTickMs[i]; ons++; }
        }
        if (ons == 0 || offs == 0) return -1;
        return 50 + 50 * (off / offs - on / ons) / (60000.0 / bpm / 4);
    }

    public record Bar(double bpm, double linesUp, int barsHeard, String bar, double[] pushMs, double[] weight,
                      double[] darkPerBar, double[] brightPerBar, double aOneAtSeconds) {
        public Lesson asLesson(String name) { return new Lesson(name, bpm, bar, pushMs, weight); }
        /** How much the sixteenths swing, as a drum machine counts it. See {@link Hearing#swing}. */
        public double swing() { return Hearing.swing(bar, pushMs, bpm); }
    }

    /**
     * Fold everything the ear noticed onto one bar of sixteen places. It finds the steady pulse the hits line
     * up on best, lays a grid of sixteenths over the whole recording, and takes the place where the dark
     * hits are heaviest as the one. A place becomes a pot if a dark hit with some weight to it fell there in
     * at least a third of the bars, a pan if a bright hit did in at least half. This is the ear's own reduction of a full mix:
     * dark is whatever is low (kick, bass), bright is everything else (snare, hats, voices, keys).
     */
    /** Two thirds of the best tempo is taken instead if the hits line up on it at least this well against the best. */
    static final double SWUNG_NOT_FASTER = 0.6;

    /** A recording is in threes if it comes round this much better after twelve ticks than after sixteen. */
    static final double THREES_CLEARLY = 1.15;

    /** How alike the loudness at each tick is to the loudness this many ticks later: 1 if it comes round exactly. */
    private static double comesRound(double[] loudAt, int after) {
        double same = 0, all = 0;
        for (int k = 0; k + after < loudAt.length; k++) { same += loudAt[k] * loudAt[k + after]; all += (loudAt[k] * loudAt[k] + loudAt[k + after] * loudAt[k + after]) / 2; }
        return all == 0 ? 0 : same / all;
    }

    public static Bar oneBar(Heard h) { return oneBar(h, false); }

    /**
     * The same, for players who count in threes if asked: then hits that could be sixteenths swung hard are not
     * taken for that. If the music does not come round after twelve ticks the bar still comes back with
     * sixteen places, and it is not for them.
     */
    public static Bar oneBar(Heard h, boolean forThrees) {
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
        // Any hits that line up on a tick line up on one twice as fast. If the tick twice as long does as well, that is the tick.
        while (bestBpm / 2 >= 60) {
            double tick = 60.0 / (bestBpm / 2) / 4, re = 0, im = 0, w = 0;
            for (int i = 0; i < t.length; i++) {
                double angle = 2 * Math.PI * (t[i] % tick) / tick;
                re += h.hitLoud()[i] * Math.cos(angle); im += h.hitLoud()[i] * Math.sin(angle); w += h.hitLoud()[i];
            }
            double score = w == 0 ? 0 : Math.hypot(re, im) / w;
            if (score < 0.97 * bestScore) break;
            bestBpm /= 2; bestScore = score; bestAngle = Math.atan2(im, re);
        }
        // Sixteenths that swing hard sit near the triplet, and then the hits line up even better on a grid half
        // as fast again, which is not the tempo. If two thirds of the best tempo lines up nearly as well, that is
        // the tempo, and what it heard is swing.
        double swungBpm = 0, swungScore = -1, swungAngle = 0;
        for (double bpm = bestBpm * 2 / 3 * 0.99; bpm <= bestBpm * 2 / 3 * 1.01 && bestBpm * 2 / 3 * 0.99 >= 60; bpm += 0.01) {
            double tick = 60.0 / bpm / 4, re = 0, im = 0, w = 0;
            for (int i = 0; i < t.length; i++) {
                double angle = 2 * Math.PI * (t[i] % tick) / tick;
                re += h.hitLoud()[i] * Math.cos(angle); im += h.hitLoud()[i] * Math.sin(angle); w += h.hitLoud()[i];
            }
            double score = w == 0 ? 0 : Math.hypot(re, im) / w;
            if (score > swungScore) { swungScore = score; swungBpm = bpm; swungAngle = Math.atan2(im, re); }
        }
        boolean swung = swungScore >= SWUNG_NOT_FASTER * bestScore;
        if (swung && !forThrees) { bestBpm = swungBpm; bestScore = swungScore; bestAngle = swungAngle; }
        double tick = 60.0 / bestBpm / 4, first = (bestAngle < 0 ? bestAngle + 2 * Math.PI : bestAngle) / (2 * Math.PI) * tick;
        // Fours or threes? The ticks are the same either way; what differs is how many of them go round before
        // the music comes round. It is in threes only if it comes round clearly better after twelve than after sixteen.
        long firstTick = Math.round((t.length == 0 ? 0 : t[0] - first) / tick), lastTick = Math.round((t.length == 0 ? 0 : t[t.length - 1] - first) / tick);
        double[] loudAt = new double[(int) (lastTick - firstTick) + 1];
        for (int i = 0; i < t.length; i++) {
            long which = Math.round((t[i] - first) / tick);
            if (Math.abs(t[i] - (first + which * tick)) <= Musician.REACH * tick) loudAt[(int) (which - firstTick)] += h.hitLoud()[i];
        }
        double afterTwelve = comesRound(loudAt, Musician.THREES), afterSixteen = comesRound(loudAt, Musician.SLOTS);
        boolean inThrees = afterTwelve > THREES_CLEARLY * afterSixteen;
        if (inThrees) bestBpm = bestBpm * 4 / 3;             // the same ticks, three to the pulse
        int n = inThrees ? Musician.THREES : Musician.SLOTS;
        double[] dark = new double[n], bright = new double[n], darkLoud = new double[n], brightLoud = new double[n], darkOff = new double[n], brightOff = new double[n];
        for (int i = 0; i < t.length; i++) {
            long which = Math.round((t[i] - first) / tick);
            double off = t[i] - (first + which * tick);
            if (Math.abs(off) > Musician.REACH * tick) continue;   // too far from any tick to be a place; a swung sixteenth is still within reach
            int place = (int) ((which % n + n) % n);
            if (h.hitBright()[i]) { bright[place]++; brightLoud[place] += h.hitLoud()[i]; brightOff[place] += off; }
            else { dark[place]++; darkLoud[place] += h.hitLoud()[i]; darkOff[place] += off; }
        }
        int one = 0;                                         // the one: where the dark hits weigh most
        for (int p = 1; p < n; p++) if (darkLoud[p] > darkLoud[one]) one = p;
        int bars = Math.max(1, (int) (h.seconds() / (tick * n)));
        char[] bar = new char[n];
        double[] push = new double[n], weight = new double[n], darkPer = new double[n], brightPer = new double[n];
        double heaviest = 1e-9, heaviestDark = 1e-9;
        for (int p = 0; p < n; p++) if (dark[p] > 0) heaviestDark = Math.max(heaviestDark, darkLoud[p] / dark[p]);
        for (int p = 0; p < n; p++) heaviest = Math.max(heaviest, Math.max(dark[p] == 0 ? 0 : darkLoud[p] / dark[p], bright[p] == 0 ? 0 : brightLoud[p] / bright[p]));
        for (int k = 0; k < n; k++) {
            int p = (k + one) % n;
            darkPer[k] = dark[p] / bars; brightPer[k] = bright[p] / bars;
            // A low hit only counts if it has some weight to it: the bottom of a bright sound leaks into the low ear, quietly.
            boolean heavyEnough = dark[p] > 0 && darkLoud[p] / dark[p] >= 0.3 * heaviestDark;
            boolean pot = k == 0 || (darkPer[k] >= 1.0 / 3 && heavyEnough), pan = !pot && brightPer[k] >= 0.5;
            bar[k] = pot ? 'p' : pan ? 'n' : '.';
            if (pot && dark[p] > 0) { push[k] = darkOff[p] / dark[p] * 1000; weight[k] = darkLoud[p] / dark[p] / heaviest; }
            if (pan) { push[k] = brightOff[p] / bright[p] * 1000; weight[k] = brightLoud[p] / bright[p] / heaviest / Kit.PAN_AGAINST_POT; }
        }
        double shift = push[0];                              // feel is measured from the one
        for (int k = 0; k < n; k++) {
            if (bar[k] == '.') { push[k] = 0; weight[k] = 0; continue; }
            push[k] = Math.max(-tick * 1000 / 3, Math.min(tick * 1000 / 3, push[k] - shift));   // no further than the triplet
            weight[k] = k == 0 ? 1.0 : Math.max(0.2, Math.min(0.85, weight[k]));
        }
        return new Bar(bestBpm, bestScore, bars, new String(bar), push, weight, darkPer, brightPer, first + one * tick);
    }

    /**
     * A stretch of the recording with a click laid over it where the ear thinks the pulse is, and a higher
     * click where it thinks the one is. The ear can be wrong about the one, and nobody here can listen, so
     * this is for a person to check by ear.
     */
    public static float[] withThePulseMarked(float[] sound, Bar bar, double fromSeconds, double seconds) {
        int from = (int) Audio.samples(fromSeconds), n = (int) Math.min(Audio.samples(seconds), sound.length - from);
        float[] out = new float[n];
        for (int i = 0; i < n; i++) out[i] = sound[from + i] * 0.6f;
        double beat = 60.0 / bar.bpm();
        long firstBeat = (long) Math.ceil((fromSeconds - bar.aOneAtSeconds()) / beat);
        for (long k = firstBeat; ; k++) {
            int at = (int) Audio.samples(bar.aOneAtSeconds() + k * beat - fromSeconds);
            if (at >= n) break;
            boolean one = Math.floorMod(k, Musician.PULSES) == 0;
            for (int i = 0; i < Audio.samples(0.03) && at + i < n; i++)
                out[at + i] += (float) ((one ? 0.5 : 0.25) * Math.sin(2 * Math.PI * (one ? 1760 : 880) * i / Audio.RATE) * Math.exp(-i / Audio.samples(0.006)));
        }
        return out;
    }
}
