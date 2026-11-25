package com.eyeoverthink.band;

/**
 * How a bass player hears which note the bottom of a song is on. It keeps the last moments of the track in
 * its headphones, thinned out to what a low note needs, and a little after a tick it asks: of all the notes
 * a bass can play, which one is most there? It listens for each note's bottom tone and the two tones above
 * it, because on a small speaker or under a kick drum the bottom tone alone is not to be trusted.
 * Nothing here is told what the song is; it only measures. Everything is claimed once.
 */
public final class LineEar {
    /** The lowest and highest notes it listens for, as piano key numbers: the B below a bass's low E, up two octaves. */
    public static final int LOWEST = 23, HIGHEST = 47;
    private static final int EVERY = 16;                                 // it keeps one sample in sixteen
    private static final int STEPS = 4;                                  // it tries four pitches to the semitone
    private static final int PIECE = (int) (Audio.RATE * 0.2 / EVERY);   // a fifth of a second
    private static final long SKIP = (long) Audio.samples(0.04);         // let the pluck, or the kick, go by
    private final int lowest, TRIES;
    private static final double[] PART = {1.0, 0.7, 0.4};                // how much the bottom tone and the two above it count
    /** A note is clear when it is this much of the clearest it has heard lately. */
    private static final double CLEAR = 0.35, LATELY = 0.97;

    private final float[] slow = new float[1024];
    private final double[] soft = new double[PIECE];
    private final double[][] turnCos, turnSin;
    private double sum, clearestLately, midi;
    private long from = -1;

    public LineEar() { this(LOWEST, HIGHEST); }

    /** An ear for another part of the band: the notes from this key number up to that one. */
    public LineEar(int lowest, int highest) {
        this.lowest = lowest; this.TRIES = (highest - lowest) * STEPS + 1;
        turnCos = new double[TRIES][PART.length]; turnSin = new double[TRIES][PART.length];
        for (int i = 0; i < PIECE; i++) {
            double x = 2 * Math.PI * i / (PIECE - 1);
            soft[i] = 0.42 - 0.5 * Math.cos(x) + 0.08 * Math.cos(2 * x);
        }
        for (int c = 0; c < TRIES; c++) {
            double hz = hzOf(lowest + c / (double) STEPS);
            for (int h = 0; h < PART.length; h++) {
                double w = 2 * Math.PI * hz * (h + 1) * EVERY / Audio.RATE;
                turnCos[c][h] = Math.cos(w); turnSin[c][h] = Math.sin(w);
            }
        }
    }

    /** The pitch of a piano key number, and the key number of a pitch. Key 69 is the A at 440. */
    public static double hzOf(double midi) { return 440.0 * Math.pow(2, (midi - 69) / 12.0); }
    public static double midiOf(double hz) { return 69 + 12 * Math.log(hz / 440.0) / Math.log(2); }

    /** Take in this block of the track. */
    public void listen(float[] track, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            sum += track[i];
            long t = blockStart + i;
            if (t % EVERY == EVERY - 1) { slow[(int) ((t / EVERY) & 1023)] = (float) (sum / EVERY); sum = 0; }
        }
    }

    /** A tick falls at this moment: listen for the note that is sounding just after it. */
    public void noteAt(long tick) { from = tick + SKIP; }

    /** True while it is still listening after a tick. It cannot start on another until it has finished with this one. */
    public boolean busy() { return from >= 0; }

    /** True once enough of the track has gone by since that tick. */
    public boolean ready(long now) { return from >= 0 && now >= from + (long) (PIECE + 1) * EVERY; }

    /** Work out the note. True if one stood out clearly; then {@link #midi} says which. Call once it is {@link #ready}. */
    public boolean hear() {
        long first = from / EVERY;
        from = -1;
        double best = 0;
        int bestAt = 0;
        for (int c = 0; c < TRIES; c++) {
            double there = 0;
            for (int h = 0; h < PART.length; h++) {
                double cw = turnCos[c][h], sw = turnSin[c][h], co = 1, si = 0, re = 0, im = 0;
                for (int i = 0; i < PIECE; i++) {
                    double v = soft[i] * slow[(int) ((first + i) & 1023)];
                    re += v * co; im += v * si;
                    double next = co * cw - si * sw; si = si * cw + co * sw; co = next;
                }
                there += PART[h] * Math.sqrt(re * re + im * im);
            }
            if (there > best) { best = there; bestAt = c; }
        }
        clearestLately = Math.max(clearestLately * LATELY, best);
        midi = lowest + bestAt / (double) STEPS;
        return best >= CLEAR * clearestLately && best > 0;
    }

    /** The note it last heard, as a piano key number, to a quarter of a semitone. */
    public double midi() { return midi; }
}
