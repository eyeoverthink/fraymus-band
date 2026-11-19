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
 * The lesson. A record plays two bars and leaves two bars of room. While the record plays, the drummer is
 * quiet. It takes the first hit it hears as the one, ticks along, and leans its clock toward the record's.
 * Each hit it hears, it puts at the nearest tick it has, and notes whether it was bright or dark. A hit
 * that falls between its ticks it cannot place, so a drummer with one tick to the pulse hears only the
 * pulses of the lesson. If a hit turns out heavier than the one it took for the one, it takes the heavy
 * hit as the one and shifts everything to fit. What it made of the two bars is all it knows of the
 * lesson.
 *
 * The answer. Then it plays two bars of its own. It starts life with the pot on every pulse. Before each
 * answer it changes one place its ticks can reach. Most times it moves one place toward what it just heard;
 * one time in four it tries an idea of its own. If the answer sounded clearly more uneven than its playing
 * usually does, the change was more than its hand could hold, and it takes it back. If the record comes back in before the answer
 * is finished, it stops and listens. If it waits two bars and the record does not come back, it comes in
 * on its own.
 *
 * The ending. After its last answer it lands the one of the next bar and lets that hit ring for one pulse
 * of its own clock, whatever its tempo has become.
 *
 * Its listening, its ticks and its pattern are written into its life and are where the next session starts.
 * It is never told a tempo, and it is never shown the lesson. It only hears it.
 */
public final class Drummer implements Player {
    /** How far the pulse wanders each pulse, as a fraction of itself. */
    static final double WANDER = 0.030;
    /** How far a newborn's hit lands from where it was meant, in seconds (one standard deviation). */
    static final double SLIP = 0.004;
    /** The surest a hand ever gets, and how many hits it takes to get most of the way there. */
    static final double SUREST = 0.0012;
    static final double HITS_TO_LEARN = 1500;
    /** How much a newborn listens, before it has learned anything. */
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
    /** Bars the record plays, and bars in an answer. */
    public static final int TURN = 2;
    /** It earns the next cut when the ticks it has are this even (0.02 = those gaps off by 2%). */
    static final double EARN = 0.012;
    /** A change to the pattern is kept if the answer stayed at least this even. */
    static final double HOLD = 0.025;
    /** It can place a heard hit only if it falls within this part of a tick from one of its ticks... */
    static final double REACH = 0.12;
    /** ...but never less than this many seconds, and never more than this many. */
    static final double REACH_AT_LEAST = 0.035, REACH_AT_MOST = 0.060;
    /** A change is also kept if the answer was no more than this much rougher than its playing usually is. */
    static final double ROUGHER = 1.2;
    /** A hit this much heavier than the one it took for the one is the real one. */
    static final double HEAVIER = 1.08;
    /** How far it shifts its tick toward a hit it placed. Tapping along is a reflex, not a skill. */
    static final double TAP = 0.5;
    /** How often a change is its own idea and not something it heard. */
    static final int OWN_IDEA_ONE_IN = 4;
    /** A sound this soon after its own hit is its own hit coming back. */
    static final double MINE = 0.080;
    public static final String FIRST_PATTERN = "p...p...p...p...";

    private static final byte REST = Kit.REST, POT = Kit.POT, PAN = Kit.PAN;
    private static final int WAITING = 0, LISTENING = 1, ANSWERING = 2;

    private final String name;
    private final Clock clock;
    private final Ear ear = new Ear();
    private final Kit kit = new Kit();
    private final SplittableRandom chance;
    private final double comfortable;
    private final int bars;               // bars it will play in all its answers

    private double listening;             // how strongly it follows what it hears
    private double trying = TRY;
    private double roughBefore = -1;
    private long judgedAt, gapsJudged;
    private long experience;              // hits played in its whole life
    private long hitsUsed;

    private int ticks;                    // ticks to the pulse it can hold: 1, 2 or 4
    private int evenBars;
    private final byte[] pattern = new byte[SLOTS];
    private int triedSlot = -1;
    private byte triedWas;
    private int changesKept, changesTakenBack, copied, ownIdeas, cutShort;

    private int mode = WAITING;
    private int listenTick;               // which tick of the record's two bars its clock is about to reach
    private int placedTick = -1; private long placedAt;
    private final byte[] hearing = new byte[SLOTS];   // what it is making of the bars being played now
    private final double[] weight = new double[SLOTS];  // and how heavy the hit at each place was
    private final byte[] shifted = new byte[SLOTS];
    private int refoundTheOne;
    private double usual = -1;            // how uneven its answers usually sound
    private final byte[] lesson = new byte[SLOTS];    // what it made of the last two bars it heard
    private boolean hasLesson;
    private int heardPlaced, heardUnplaced;

    private final double[] gaps = new double[Ear.MEMORY];
    private final int[] gapSlots = new int[Ear.MEMORY];
    private final double[] uneven = new double[Ear.MEMORY];
    private final double[] tickUneven = new double[Ear.MEMORY];
    private int gapHead, gapCount, unevenHead, unevenCount, tickHead, tickCount;
    private double roundUneven; private int roundGaps;
    private long waitingSince = -1;
    private int cameInAlone;
    private long mineBefore = -1;         // when the last of its own hits in this answer came back

    private int bar, tickInBar;
    private long strikeAt = -1;
    private byte strikeSound;
    private int strikeSlot;
    private long lastStrike = Long.MIN_VALUE / 2;
    private boolean landing;
    private double lastPulse;
    private long endAt = -1;

    private final long[] hits;
    private final int[] hitSlots;
    private int hitCount;

    public Drummer(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                   int ticksSoFar, String patternSoFar, int answers, long seed) {
        this.name = name;
        this.listening = listeningSoFar;
        this.ticks = ticksSoFar;
        for (int i = 0; i < SLOTS; i++) pattern[i] = Kit.read(i < patternSoFar.length() ? patternSoFar.charAt(i) : '.');
        this.bars = answers * TURN;
        this.comfortable = Audio.periodOf(comfortableBpm);
        this.clock = new Clock(comfortable, 0);
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
        if (ear.hitsEver() != hitsUsed) {
            hitsUsed = ear.hitsEver();
            long at = ear.hitTime();
            boolean mine = at - lastStrike >= 0 && at - lastStrike <= Audio.samples(MINE);
            if (!mine) someoneElse(at, ear.hitBright(), ear.hitLoud());
            else {                                           // its own hit, come back: only these tell it how it is playing
                if (mineBefore >= 0) ownGap(at - mineBefore);
                mineBefore = at;
            }
        }
    }

    /** A hit it did not make. */
    private void someoneElse(long at, boolean bright, double loud) {
        byte kind = bright ? PAN : POT;
        double tick = clock.period() / ticks;
        if (mode == ANSWERING) {
            boolean firstBar = bar % TURN == 0 && tickInBar > 0;
            if (firstBar) return;                            // too soon to be the record coming back; let it go
            if (bar % TURN != 0) {                           // the record is back before it finished: stop and listen
                cutShort++;
                strikeAt = -1;
                bar = (bar / TURN + 1) * TURN; tickInBar = 0;
            }
            endOfAnswer();
            mode = WAITING;
        }
        if (mode == WAITING) {                               // the first hit after the quiet is the one
            mode = LISTENING;
            clock.startAt(at + tick);
            listenTick = 1;
            java.util.Arrays.fill(hearing, REST);
            java.util.Arrays.fill(weight, 0);
            hearing[0] = kind; weight[0] = loud;
            placedTick = 0; placedAt = at; heardPlaced = 1; heardUnplaced = 0;
            return;
        }
        int best = -1; double off = Double.MAX_VALUE;        // which of its ticks was that nearest to?
        for (int back = 0; back <= 2; back++) {
            double d = at - (clock.nextBeat() - back * tick);
            if (Math.abs(d) < Math.abs(off)) { off = d; best = listenTick - back; }
        }
        if (best < 0 || best >= TURN * PULSES * ticks || Math.abs(off) > Math.min(Math.max(REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST))) {
            heardUnplaced++;
            if (loud > HEAVIER * weight[0] && listenTick > PULSES * ticks / 2) {
                // It fits none of its ticks and it is heavier than what it took for the one. So this is the one,
                // of the record's second bar, and its ticks were in the wrong place. Start again from here.
                clock.startAt(at + tick);
                listenTick = PULSES * ticks + 1;
                java.util.Arrays.fill(hearing, REST); java.util.Arrays.fill(weight, 0);
                hearing[0] = kind; weight[0] = loud;
                placedTick = PULSES * ticks; placedAt = at;
                refoundTheOne++;
            }
            return;
        }
        heardPlaced++;
        int place = (best % (PULSES * ticks)) * (SLOTS / PULSES / ticks);
        hearing[place] = kind;
        if (loud > weight[place]) weight[place] = loud;
        clock.nudge(TAP * off);
        if (best > placedTick) {
            clock.pullToward((double) (at - placedAt) / (best - placedTick) * ticks, listening);
            placedTick = best; placedAt = at;
        }
    }

    /** A gap between two of its own hits, come back through the room. */
    private void ownGap(double gap) {
        double tick = clock.period() / ticks;
        int span = (int) Math.max(1, Math.round(gap / tick)) * (SLOTS / PULSES / ticks);
        if (gapCount > 0) {
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
        gapsJudged++;

        clock.pullToward(feltPulse(trust()), listening);
        if (gapsJudged - judgedAt >= JUDGE_EVERY) judge();
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
        judgedAt = gapsJudged;
        double roughNow = unevenness();
        if (roughBefore >= 0 && roughNow > roughBefore) { listening -= trying; trying = -trying; }
        roughBefore = roughNow;
        listening = Math.max(0.02, Math.min(0.95, listening + trying));
    }

    /** The top of a bar in its answer: where it finds out what it has earned. */
    private void barLine() {
        if (ticks < 4 && tickUnevenness() < EARN) {
            if (++evenBars >= 2) { ticks *= 2; evenBars = 0; tickCount = 0; }
        } else evenBars = 0;
        if (bar % TURN == 0) {
            endOfAnswer();
            if (bar < bars) mode = WAITING;                  // quiet now, until the record's one
        }
    }

    /** Could it hold the change it tried? */
    private void endOfAnswer() {
        double rough = roundGaps > 0 ? roundUneven / roundGaps : 0;
        if (triedSlot >= 0) {
            if (roundGaps > 0 && rough > HOLD && usual >= 0 && rough > ROUGHER * usual) { pattern[triedSlot] = triedWas; changesTakenBack++; }
            else changesKept++;
            triedSlot = -1;
        }
        if (roundGaps > 0) usual = usual < 0 ? rough : usual + 0.3 * (rough - usual);
        roundUneven = 0; roundGaps = 0;
    }

    /** The record has played its two bars. Keep what it made of them, pick one change, and come in. */
    private void startAnswer() {
        int one = 0;                                        // was the heaviest hit where it thought the one was?
        for (int s = 1; s < SLOTS; s++) if (weight[s] > weight[one]) one = s;
        if (weight[one] <= HEAVIER * weight[0]) one = 0;
        if (one != 0) {                                      // no: the heavy hit is the one, and it is already that far into the bar
            for (int s = 0; s < SLOTS; s++) shifted[s] = hearing[(s + one) % SLOTS];
            System.arraycopy(shifted, 0, hearing, 0, SLOTS);
            refoundTheOne++;
        }
        System.arraycopy(hearing, 0, lesson, 0, SLOTS);
        hasLesson = true;
        mode = ANSWERING;
        tickInBar = ((SLOTS - one) % SLOTS) / (SLOTS / PULSES / ticks);
        mineBefore = -1; gapCount = 0;
        if (hasLesson && chance.nextInt(OWN_IDEA_ONE_IN) != 0 && copyOnePlace()) copied++;
        else if (ownIdea()) ownIdeas++;
    }

    /** Move one place it can reach toward what it heard. False if it already plays everything it heard. */
    private boolean copyOnePlace() {
        int step = SLOTS / PULSES / ticks, differ = 0;
        for (int s = step; s < SLOTS; s += step) if (wants(s)) differ++;
        if (differ == 0) return false;
        int pick = chance.nextInt(differ);
        for (int s = step; s < SLOTS; s += step) {
            if (!wants(s) || pick-- != 0) continue;
            triedSlot = s; triedWas = pattern[s];
            pattern[s] = lesson[s];
        }
        return true;
    }

    /** A place where it heard something other than what it plays. A pulse is never left empty. */
    private boolean wants(int s) { return lesson[s] != pattern[s] && !(lesson[s] == REST && s % (SLOTS / PULSES) == 0); }

    /** Change one place its ticks can reach, for no reason but to try it. The one stays on the pot. */
    private boolean ownIdea() {
        int step = SLOTS / PULSES / ticks;
        int slot = step * chance.nextInt(SLOTS / step);
        if (slot == 0) return false;
        byte was = pattern[slot];
        pattern[slot] = was == REST ? (chance.nextBoolean() ? POT : PAN)
                      : was == POT ? PAN
                      : slot % (SLOTS / PULSES) == 0 ? POT : REST;
        triedSlot = slot; triedWas = was;
        return true;
    }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        if (mode == WAITING) {
            if (waitingSince < 0) waitingSince = blockStart;
            else if (blockStart - waitingSince > TURN * PULSES * clock.period()) {   // nobody came: come in on its own
                clock.startAt(blockStart);
                System.arraycopy(lesson, 0, hearing, 0, SLOTS);
                java.util.Arrays.fill(weight, 0);
                cameInAlone++;
                mode = LISTENING; listenTick = TURN * PULSES * ticks;
            }
        } else waitingSince = -1;
        while (mode != WAITING && !landing && endAt < 0 && strikeAt < 0 && clock.dueBefore(blockEnd)) {
            if (mode == LISTENING) {
                if (listenTick < TURN * PULSES * ticks) {    // ticking along, quietly
                    listenTick++;
                    step();
                    continue;
                }
                startAnswer();
            } else if (tickInBar == 0) {
                barLine();
                if (mode == WAITING) break;
            }
            int slot = tickInBar * (SLOTS / PULSES / ticks);
            boolean last = bar >= bars;                      // the one after its last answer: where it lands
            byte sound = last ? POT : pattern[slot];
            if (sound != REST) {
                long meant = Math.round(clock.nextBeat() + chance.nextGaussian() * Audio.samples(slip()));
                strikeAt = Math.max(meant, blockStart);
                strikeSound = sound;
                strikeSlot = bar * SLOTS + slot;
                landing = last;
            }
            if (last) break;
            step();
            if (++tickInBar == PULSES * ticks) { tickInBar = 0; bar++; }
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                kit.strike(strikeSound, (float) (Kit.weight(strikeSlot % SLOTS) * (0.88 + 0.12 * chance.nextDouble())));
                if (hitCount < hits.length) { hits[hitCount] = blockStart + i; hitSlots[hitCount++] = strikeSlot; }
                experience++;
                lastStrike = blockStart + i;
                strikeAt = -1;
                if (landing) { lastPulse = clock.period(); endAt = blockStart + i + Math.round(lastPulse); }
            }
            out[i] += kit.next();
        }
    }

    /** On to its next tick, with the wander of its hand and the pull of the tempo its body likes. */
    private void step() {
        clock.advance(1.0 / ticks);
        clock.drift(chance.nextGaussian() * WANDER / Math.sqrt(ticks));
        clock.pullToward(comfortable, HOME / ticks);
    }

    /** True once its last hit has landed and rung for one pulse. */
    public boolean done(long now) { return endAt >= 0 && now >= endAt; }

    public String pattern() { return write(pattern); }
    /** What it made of the last two bars the record played. */
    public String lessonAsHeard() { return write(lesson); }
    private static String write(byte[] b) {
        char[] c = new char[SLOTS];
        for (int i = 0; i < SLOTS; i++) c[i] = Kit.write(b[i]);
        return new String(c);
    }

    public int hitCount() { return hitCount; }
    public long hitAt(int i) { return hits[i]; }
    public int hitSlot(int i) { return hitSlots[i]; }
    public double tempoNow() { return Audio.bpmOf(clock.period()); }
    public double lastPulse() { return lastPulse; }
    public long experience() { return experience; }
    public double listening() { return listening; }
    public int ticks() { return ticks; }
    public int changesKept() { return changesKept; }
    public int changesTakenBack() { return changesTakenBack; }
    public int copied() { return copied; }
    public int ownIdeas() { return ownIdeas; }
    public int cutShort() { return cutShort; }
    public int refoundTheOne() { return refoundTheOne; }
    public int cameInAlone() { return cameInAlone; }
}
