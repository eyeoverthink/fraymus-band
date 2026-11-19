package com.eyeoverthink.band;

/**
 * How a player hears a hit. It follows the loudness of what reaches it and marks the moment the
 * loudness jumps up out of the dip since the last hit, so a quiet hit close behind a loud one still counts. It keeps the lengths of the last few gaps between hits, which is all a player
 * needs to feel a pulse. A moment after each hit it can also say whether the hit was bright (a pan) or
 * dark (a pot), from how fast the sound wiggles. Nothing here knows who made the sound.
 */
public final class Ear {
    public static final int MEMORY = 8;

    private final double[] gaps = new double[MEMORY];
    private int gapCount;
    private long gapsEver;
    private int gapHead;

    private double fast;          // loudness right now
    private double valley;        // the quietest it has been since the last hit
    private long lastOnset = -1;
    private long onsetsHeard;

    private long judging = -1;    // the hit whose brightness is still being worked out
    private double energy, wiggle, top;
    private float before;
    private long hitsEver;
    private long hitTime;
    private boolean hitBright;
    private double hitLoud;
    // Let the click at the front go by, and the click of anyone else who hit a few milliseconds later:
    // two pots landing almost together must still sound dark.
    private static final int SKIP = (int) Audio.samples(0.025);
    private static final int LOOK = (int) Audio.samples(0.020);
    private static final double BRIGHT = 0.004;
    private final int refractory = (int) Audio.samples(0.070);   // one hit cannot be heard twice
    private static final double ATTACK = 1.0 - Math.exp(-1.0 / Audio.samples(0.002));
    private static final double RELEASE = 1.0 - Math.exp(-1.0 / Audio.samples(0.030));
    private static final double JUMP = 1.5;
    private static final double FLOOR = 0.02;

    public void listen(float[] heard, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            double level = Math.abs(heard[i]);
            fast += (level > fast ? ATTACK : RELEASE) * (level - fast);
            long now = blockStart + i;
            if (judging >= 0) {
                long age = now - judging;
                if (Math.abs(heard[i]) > top) top = Math.abs(heard[i]);
                if (age >= SKIP && age < SKIP + LOOK) {
                    energy += heard[i] * heard[i];
                    wiggle += (heard[i] - before) * (heard[i] - before);
                } else if (age >= SKIP + LOOK) {
                    hitTime = judging; hitBright = wiggle > BRIGHT * energy; hitLoud = top; hitsEver++;
                    judging = -1;
                }
            }
            before = heard[i];
            if (lastOnset >= 0 && now - lastOnset <= refractory) continue;
            if (fast < valley) valley = fast;
            if (fast > FLOOR && fast > JUMP * valley + FLOOR) {
                if (lastOnset >= 0) remember(now - lastOnset);
                lastOnset = now;
                valley = Double.MAX_VALUE;
                judging = now; energy = 0; wiggle = 0; top = 0;
                onsetsHeard++;
            }
        }
    }

    private void remember(double gap) {
        gaps[gapHead] = gap;
        gapHead = (gapHead + 1) % MEMORY;
        if (gapCount < MEMORY) gapCount++;
        gapsEver++;
    }

    public long onsetsHeard() { return onsetsHeard; }
    /** Counts every hit whose brightness is known, so a player can tell a new one has arrived. */
    public long hitsEver() { return hitsEver; }
    /** When the latest such hit began. */
    public long hitTime() { return hitTime; }
    /** Whether it was bright like the pan, or dark like the pot. */
    public boolean hitBright() { return hitBright; }
    /** How loud it was at its loudest. */
    public double hitLoud() { return hitLoud; }
    public int gapsRemembered() { return gapCount; }
    /** Counts every gap ever heard, so a player can tell that something new has reached it. */
    public long gapsEver() { return gapsEver; }

    /** The average of the most recent {@code k} gaps, or 0 when nothing has been heard yet. */
    public double recentGap(int k) {
        int use = Math.min(k, gapCount);
        if (use == 0) return 0;
        double sum = 0;
        for (int j = 1; j <= use; j++) sum += gaps[(gapHead - j + MEMORY) % MEMORY];
        return sum / use;
    }
}
