package com.eyeoverthink.band;

import java.util.SplittableRandom;

/**
 * A young drummer with a pot, a pan, and a pulse of its own.
 *
 * The pulse. Its hand is unsteady: the pulse wanders and each hit lands a little off. It hears its own hits
 * come back through the room and pulls its pulse toward what it heard. How much it listens is something it
 * learns by trying a little more or a little less and keeping whichever sounded steadier.
 *
 * The tick. A bar is four pulses. A newborn can only feel the pulse itself: one tick to the pulse. To earn
 * the next cut (two ticks to the pulse, then four) it has to be playing the ticks it already has, and those
 * gaps have to sound even enough, for two bars running. Four ticks to each of four pulses is sixteen places
 * in the bar where a hit can go. Listening alone does not get it there. Its hand has to get surer too, and
 * the hand gets surer only by hitting things: that one rule is given to it, not learned.
 *
 * The pattern. It starts with the one thing it knows, the pot on every pulse. Every round (two bars) it
 * changes one place its ticks can reach: a new hit, a pot swapped for the pan, a hit taken away. If the
 * round sounded too uneven to its own ear, the change was more than its hand could hold, and it takes it
 * back. Otherwise it keeps it. The one of the bar stays on the pot.
 *
 * The ending. After its last bar it lands the one of the next bar and lets that hit ring for one pulse of
 * its own clock, whatever its tempo has become.
 *
 * Its listening, its ticks and its pattern are written into its life and are where the next session starts.
 * It is never told a tempo.
 */
public final class Drummer implements Player {
    /** How far the pulse wanders each pulse, as a fraction of itself. */
    static final double WANDER = 0.030;
    /** How far a newborn's hit lands from where it was meant, in seconds (one standard deviation). */
    static final double SLIP = 0.004;
    /** The surest a hand ever gets, and how many hits it takes to get most of the way there. */
    static final double SUREST = 0.0012;
    static final double HITS_TO_LEARN = 1500;
    /** How much a newborn listens to itself, before it has learned anything. */
    static final double NEWBORN_LISTENING = 0.15;
    /** How big a change in listening it tries each time. */
    static final double TRY = 0.06;
    /** How many heard gaps it plays before judging whether the last change in listening helped. */
    static final int JUDGE_EVERY = Ear.MEMORY;
    /** How strongly it settles back toward the tempo its body likes. */
    static final double HOME = 0.02;

    /** Places in a bar: four pulses, at most four ticks each. */
    public static final int SLOTS = 16;
    public static final int PULSES = 4;
    /** Bars in a round. */
    static final int ROUND = 2;
    /** It earns the next cut when the ticks it has are this even (0.02 = those gaps off by 2%). */
    static final double EARN = 0.012;
    /** A change to the pattern is kept if the round stayed at least this even. */
    static final double HOLD = 0.025;
    public static final String FIRST_PATTERN = "p...p...p...p...";

    static final byte REST = 0, POT = 1, PAN = 2;

    private final String name;
    private final Clock clock;
    private final Ear ear = new Ear();
    private final Sound[] sounds = {null, new Thump(), new Pan()};
    private final int[] voiceAt = {-1, -1, -1};      // where each sound is in its ring, or -1 when silent
    private final float[] voiceLoud = new float[3];
    private final SplittableRandom chance;
    private final double comfortable;
    private final int bars;

    private double listening;             // how strongly it follows what it hears
    private double trying = TRY;
    private double roughBefore = -1;
    private long judgedAt;
    private long experience;              // hits played in its whole life
    private long gapsUsed;

    private int ticks;                    // ticks to the pulse it can hold: 1, 2 or 4
    private int evenBars;                 // bars in a row that sounded even enough to earn the next cut
    private final byte[] pattern = new byte[SLOTS];
    private int triedSlot = -1;           // the place it changed this round, or -1
    private byte triedWas;
    private int changesKept, changesTakenBack;

    private final double[] gaps = new double[Ear.MEMORY];   // what it heard lately...
    private final int[] gapSlots = new int[Ear.MEMORY];     // ...and how many places each gap spanned
    private final double[] uneven = new double[Ear.MEMORY]; // how far off each gap was, as a fraction of itself
    private final double[] tickUneven = new double[Ear.MEMORY]; // the same, for gaps exactly one tick long
    private int gapHead, gapCount, unevenHead, unevenCount, tickHead, tickCount;
    private double roundUneven; private int roundGaps;

    private int bar, tickInBar;
    private long strikeAt = -1;           // sample time of the hit it is about to land
    private byte strikeSound;
    private int strikeSlot;
    private boolean landing;              // the hit it is about to land is its last
    private double lastPulse;             // how long its pulse was when it landed
    private long endAt = -1;              // when the last hit has rung for one pulse

    private final long[] hits;            // when each hit of this session landed
    private final int[] hitSlots;         // and which place, counted from the start of the session
    private int hitCount;

    public Drummer(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                   int ticksSoFar, String patternSoFar, int bars, long seed) {
        this.name = name;
        this.listening = listeningSoFar;
        this.ticks = ticksSoFar;
        for (int i = 0; i < SLOTS; i++) {
            char c = i < patternSoFar.length() ? patternSoFar.charAt(i) : '.';
            pattern[i] = c == 'p' ? POT : c == 'n' ? PAN : REST;
        }
        this.bars = bars;
        this.comfortable = Audio.periodOf(comfortableBpm);
        this.clock = new Clock(comfortable, Audio.samples(0.25));
        this.chance = new SplittableRandom(seed);
        this.experience = experienceSoFar;
        this.hits = new long[bars * SLOTS + 8];
        this.hitSlots = new int[hits.length];
    }

    @Override public String name() { return name; }

    /** How many of its recent gaps it trusts: one for a newborn, up to its whole memory with practice. */
    int trust() { return (int) Math.min(Ear.MEMORY, 1 + experience / 12); }

    @Override
    public void hear(float[] heard, int n, long blockStart) {
        ear.listen(heard, n, blockStart);
        if (ear.gapsEver() == gapsUsed) return;
        gapsUsed = ear.gapsEver();
        double gap = ear.recentGap(1);
        double tick = clock.period() / ticks;
        int span = (int) Math.max(1, Math.round(gap / tick)) * (SLOTS / PULSES / ticks);   // places this gap spanned
        if (gapCount > 0) {                                  // how far off was it from the gap before?
            int before = (gapHead - 1 + Ear.MEMORY) % Ear.MEMORY;
            double expected = gaps[before] * span / gapSlots[before];
            double off = Math.abs(gap - expected) / expected;
            uneven[unevenHead] = off;
            unevenHead = (unevenHead + 1) % Ear.MEMORY;
            if (unevenCount < Ear.MEMORY) unevenCount++;
            roundUneven += off; roundGaps++;
            if (span == SLOTS / PULSES / ticks) {
                tickUneven[tickHead] = off;
                tickHead = (tickHead + 1) % Ear.MEMORY;
                if (tickCount < Ear.MEMORY) tickCount++;
            }
        }
        gaps[gapHead] = gap; gapSlots[gapHead] = span;
        gapHead = (gapHead + 1) % Ear.MEMORY;
        if (gapCount < Ear.MEMORY) gapCount++;

        clock.pullToward(feltPulse(trust()), listening);
        if (gapsUsed - judgedAt >= JUDGE_EVERY) judge();
    }

    /** The pulse it feels in its last few gaps, however many places each one spanned. */
    private double feltPulse(int k) {
        int use = Math.min(k, gapCount);
        double time = 0; int places = 0;
        for (int j = 1; j <= use; j++) {
            int at = (gapHead - j + Ear.MEMORY) % Ear.MEMORY;
            time += gaps[at]; places += gapSlots[at];
        }
        return time / places * (SLOTS / PULSES);
    }

    /** How uneven its playing has sounded lately: 0.02 means its gaps were off by 2%. */
    double unevenness() {
        if (unevenCount == 0) return 0;
        double sum = 0;
        for (int j = 0; j < unevenCount; j++) sum += uneven[j];
        return sum / unevenCount;
    }

    /** How uneven its finest ticks have sounded lately, or 1 if it has not played enough of them to know. */
    double tickUnevenness() {
        if (tickCount < Ear.MEMORY) return 1;
        double sum = 0;
        for (int j = 0; j < Ear.MEMORY; j++) sum += tickUneven[j];
        return sum / Ear.MEMORY;
    }

    /** How far its hand slips now, in seconds. */
    double slip() { return SUREST + (SLIP - SUREST) * Math.exp(-experience / HITS_TO_LEARN); }

    /** Did the last change in listening make it sound steadier? If not, undo it and try the other way. */
    private void judge() {
        judgedAt = gapsUsed;
        double roughNow = unevenness();
        if (roughBefore >= 0 && roughNow > roughBefore) { listening -= trying; trying = -trying; }
        roughBefore = roughNow;
        listening = Math.max(0.02, Math.min(0.95, listening + trying));
    }

    /** The top of a bar: where it finds out what it has earned, and decides what to try next. */
    private void barLine() {
        if (bar == 0) return;
        if (ticks < 4 && tickUnevenness() < EARN) {
            if (++evenBars >= 2) { ticks *= 2; evenBars = 0; tickCount = 0; }
        } else evenBars = 0;
        if (bar % ROUND != 0) return;
        if (triedSlot >= 0) {
            if (roundGaps > 0 && roundUneven / roundGaps > HOLD) { pattern[triedSlot] = triedWas; changesTakenBack++; }
            else changesKept++;
            triedSlot = -1;
        }
        roundUneven = 0; roundGaps = 0;
        if (bar < bars) tryAChange();
    }

    /** Change one place its ticks can reach. The one of the bar stays on the pot. */
    private void tryAChange() {
        int step = SLOTS / PULSES / ticks;
        int slot = step * chance.nextInt(SLOTS / step);
        if (slot == 0) return;                               // not this round
        byte was = pattern[slot];
        byte now = was == REST ? (chance.nextBoolean() ? POT : PAN)
                 : was == POT ? PAN
                 : slot % (SLOTS / PULSES) == 0 ? POT : REST;   // a pulse is never left empty
        pattern[slot] = now;
        triedSlot = slot; triedWas = was;
    }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        while (!landing && endAt < 0 && strikeAt < 0 && clock.dueBefore(blockEnd)) {
            if (tickInBar == 0) barLine();
            int slot = tickInBar * (SLOTS / PULSES / ticks);
            boolean last = bar >= bars;                      // the one after the last bar: where it lands
            byte sound = last ? POT : pattern[slot];
            if (sound != REST) {
                long meant = Math.round(clock.nextBeat() + chance.nextGaussian() * Audio.samples(slip()));
                strikeAt = Math.max(meant, blockStart);
                strikeSound = sound;
                strikeSlot = bar * SLOTS + slot;
                landing = last;
            }
            if (last) break;
            clock.advance(1.0 / ticks);
            clock.drift(chance.nextGaussian() * WANDER / Math.sqrt(ticks));
            clock.pullToward(comfortable, HOME / ticks);
            if (++tickInBar == PULSES * ticks) { tickInBar = 0; bar++; }
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                int place = strikeSlot % SLOTS;
                double weight = place == 0 ? 1.0 : place % 4 == 0 ? 0.85 : place % 2 == 0 ? 0.7 : 0.6;
                voiceAt[strikeSound] = 0;
                voiceLoud[strikeSound] = (float) (weight * (0.88 + 0.12 * chance.nextDouble()));
                if (hitCount < hits.length) { hits[hitCount] = blockStart + i; hitSlots[hitCount++] = strikeSlot; }
                experience++;
                strikeAt = -1;
                if (landing) { lastPulse = clock.period(); endAt = blockStart + i + Math.round(lastPulse); }   // rings for one pulse of its own
            }
            float sum = 0;
            for (int v = POT; v <= PAN; v++) {
                if (voiceAt[v] < 0) continue;
                sum += sounds[v].at(voiceAt[v]) * voiceLoud[v];
                if (++voiceAt[v] >= sounds[v].length()) voiceAt[v] = -1;
            }
            out[i] += sum * 0.8f;
        }
    }

    /** True once its last hit has landed and rung for one pulse. */
    public boolean done(long now) { return endAt >= 0 && now >= endAt; }

    public String pattern() {
        char[] c = new char[SLOTS];
        for (int i = 0; i < SLOTS; i++) c[i] = pattern[i] == POT ? 'p' : pattern[i] == PAN ? 'n' : '.';
        return new String(c);
    }

    public int hitCount() { return hitCount; }
    public long hitAt(int i) { return hits[i]; }
    public int hitSlot(int i) { return hitSlots[i]; }
    public long heardOnsets() { return ear.onsetsHeard(); }
    public double tempoNow() { return Audio.bpmOf(clock.period()); }
    public double lastPulse() { return lastPulse; }
    public long experience() { return experience; }
    public double listening() { return listening; }
    public int ticks() { return ticks; }
    public int changesKept() { return changesKept; }
    public int changesTakenBack() { return changesTakenBack; }
}
