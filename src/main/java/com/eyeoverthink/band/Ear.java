package com.eyeoverthink.band;

/**
 * How a player hears a hit. It follows the loudness of what reaches it and marks the moment the
 * loudness jumps up out of the dip since the last hit, so a quiet hit close behind a loud one still counts. It keeps the lengths of the last few gaps between hits, which is all a player
 * needs to feel a pulse. Nothing here knows who made the sound.
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
            if (lastOnset >= 0 && now - lastOnset <= refractory) continue;
            if (fast < valley) valley = fast;
            if (fast > FLOOR && fast > JUMP * valley + FLOOR) {
                if (lastOnset >= 0) remember(now - lastOnset);
                lastOnset = now;
                valley = Double.MAX_VALUE;
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
