package com.eyeoverthink.band;

import java.util.SplittableRandom;

/**
 * Age 0. One sound and a pulse of its own.
 *
 * Its hand is unsteady: every pulse wanders a little and every hit lands a little off. It hears its own
 * hits come back through the room and can pull its pulse toward what it heard. How much it listens is not
 * given to it. A newborn barely listens. Every few hits it tries listening a little more or a little less,
 * and keeps whichever sounded steadier to its own ear. That habit is written into its life and is where the
 * next session starts. It is never told a tempo; it has one it is comfortable at, and that is all.
 */
public final class Drummer implements Player {
    /** How far the pulse wanders each beat, as a fraction of itself. */
    static final double WANDER = 0.030;
    /** How far a hit lands from where it was meant, in seconds (one standard deviation). */
    static final double SLIP = 0.004;
    /** How much a newborn listens to itself, before it has learned anything. */
    static final double NEWBORN_LISTENING = 0.15;
    /** How big a change it tries each time. */
    static final double TRY = 0.06;
    /** How many heard gaps it plays before judging whether the last change helped. */
    static final int JUDGE_EVERY = Ear.MEMORY;
    /** How strongly it settles back toward the tempo its body likes. */
    static final double HOME = 0.02;

    private final String name;
    private final Clock clock;
    private final Ear ear = new Ear();
    private final Thump thump;
    private final SplittableRandom chance;
    private final double comfortable;

    private double listening;             // how strongly it follows what it hears; learned, and kept between sessions
    private double trying = TRY;          // the change it is trying now: a little more, or a little less
    private double roughBefore = -1;      // how its playing sounded before the last change
    private long judgedAt;                // heard gaps at the last judgement
    private long experience;              // pulses played in its whole life before and during this session
    private long gapsUsed;                // how many heard gaps it has already taken in
    private int voiceAt = -1;             // where in the thump it is, or -1 when silent
    private float voiceLoud;
    private long strikeAt = -1;           // sample time of the hit it is about to land

    private final long[] hits;            // when each hit of this session landed
    private int hitCount;

    public Drummer(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar, long seed, int mostHits) {
        this.listening = listeningSoFar;
        this.name = name;
        this.comfortable = Audio.periodOf(comfortableBpm);
        this.clock = new Clock(comfortable, Audio.samples(0.25));
        this.thump = new Thump();
        this.chance = new SplittableRandom(seed);
        this.experience = experienceSoFar;
        this.hits = new long[mostHits];
    }

    @Override public String name() { return name; }

    /** How many of its recent gaps it trusts: one for a newborn, up to the ear's whole memory with practice. */
    int trust() { return (int) Math.min(Ear.MEMORY, 1 + experience / 12); }

    @Override
    public void hear(float[] heard, int n, long blockStart) {
        ear.listen(heard, n, blockStart);
        if (ear.gapsEver() > gapsUsed) {                    // something new reached it: follow it
            gapsUsed = ear.gapsEver();
            clock.pullToward(ear.recentGap(trust()), listening);
            if (gapsUsed - judgedAt >= JUDGE_EVERY) judge();
        }
    }

    /**
     * Did the last change make it sound steadier? If so, keep going the same way. If not, undo it and try the
     * other way. It judges only by what its own ear remembers.
     */
    private void judge() {
        judgedAt = gapsUsed;
        double roughNow = ear.roughness();
        if (roughBefore >= 0 && roughNow > roughBefore) { listening -= trying; trying = -trying; }
        roughBefore = roughNow;
        listening = Math.max(0.02, Math.min(0.95, listening + trying));
    }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        while (clock.dueBefore(blockEnd) && strikeAt < 0) {
            long meant = Math.round(clock.nextBeat() + chance.nextGaussian() * Audio.samples(SLIP));
            strikeAt = Math.max(meant, blockStart);
            clock.advance();
            clock.drift(chance.nextGaussian() * WANDER);
            clock.pullToward(comfortable, HOME);
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                voiceAt = 0;
                voiceLoud = (float) (0.78 + 0.2 * chance.nextDouble());
                if (hitCount < hits.length) hits[hitCount++] = blockStart + i;
                experience++;
                strikeAt = -1;
            }
            if (voiceAt >= 0) {
                out[i] += thump.at(voiceAt) * voiceLoud;
                if (++voiceAt >= thump.length()) voiceAt = -1;
            }
        }
    }

    /** After its last hit: let the sound that is already ringing finish, and start nothing new. */
    public void ringOut(float[] out, int n) {
        for (int i = 0; i < n && voiceAt >= 0; i++) {
            out[i] += thump.at(voiceAt) * voiceLoud;
            if (++voiceAt >= thump.length()) voiceAt = -1;
        }
    }

    public int hitCount() { return hitCount; }
    public long hitAt(int i) { return hits[i]; }
    public long heardOnsets() { return ear.onsetsHeard(); }
    public double tempoNow() { return Audio.bpmOf(clock.period()); }
    public long experience() { return experience; }
    public double listening() { return listening; }
}
