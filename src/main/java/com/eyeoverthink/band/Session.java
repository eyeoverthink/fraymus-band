package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Path;

/**
 * One sitting. The room carries sound to the players, they hear, they play, and the take is written down.
 * Every buffer is claimed in {@link #run} before the first block; the loop itself only moves samples.
 * Nobody here keeps time for anyone else, and the session does not decide when the take ends.
 */
public final class Session {
    /** What happened, measured from the hits themselves. */
    public record Report(Lesson lesson, int answers, int hits, double seconds, double startedBpm, double endedBpm,
                         double earlyUnsteadyMs, double lateUnsteadyMs, double listeningBefore, double listeningAfter,
                         int ticksBefore, int ticksAfter, String patternBefore, String patternAfter, String lessonAsHeard,
                         int matchesBefore, int matchesAfter, int changesKept, int changesTakenBack, int copied, int ownIdeas,
                         int cutShort, int lastHitPlace, double ringSeconds, double lastPulseSeconds, float peak, Path wav,
                         int barsTogether, int hitsTogether, double apartMs, double leanMs, int alone, double landedApartMs) {}

    /** How long a player's own sound takes to reach it, and how long everyone else's does. */
    public static final double NEAR = 0.003, ACROSS = 0.009;

    public static Report run(Path wav, Path lifeFile, Lesson lesson, int answers, int barsTogether, long seed) throws IOException {
        Life life = Life.open(lifeFile, "the drummer");
        Teacher teacher = new Teacher(lesson, answers, barsTogether, 0.25);
        Drummer drummer = new Drummer(life.name(), 92.0, life.hitsPlayed(), life.listening(), life.ticks(), life.pattern(), answers, barsTogether, seed);
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromRecord = new float[Audio.BLOCK], fromDrummer = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];
        double startedBpm = drummer.tempoNow();

        long now = 0;
        try (WavWriter out = new WavWriter(wav)) {
            long tooLong = (long) Audio.samples(60.0 + 30.0 * answers + 8.0 * barsTogether);   // nothing here should ever need this
            while (!drummer.done(now)) {
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");                     // no objects are made inside this loop
                room.carry(0, others, own, Audio.BLOCK, now);
                teacher.hear(others, own, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                drummer.hear(others, own, Audio.BLOCK, now);
                java.util.Arrays.fill(fromRecord, 0f);
                java.util.Arrays.fill(fromDrummer, 0f);
                teacher.play(fromRecord, Audio.BLOCK, now);
                drummer.play(fromDrummer, Audio.BLOCK, now);
                room.fill(0, fromRecord, Audio.BLOCK, now);
                room.fill(1, fromDrummer, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromRecord[i] + fromDrummer[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            int n = drummer.hitCount(), window = Math.min(24, n / 2);
            double early = unsteadiness(drummer, 0, window);
            double late = unsteadiness(drummer, n - window, n);
            Apart with = apart(drummer, drummer.togetherFrom(), teacher);
            int together = with.together, alone = with.alone;
            double apart = with.sum, lean = with.lean;
            double landedApart = barsTogether == 0 ? 0 : with.landed;
            Report report = new Report(lesson, answers, n, Audio.seconds(out.samples()),
                startedBpm, drummer.tempoNow(), early, late, life.listening(), drummer.listening(),
                life.ticks(), drummer.ticks(), life.pattern(), drummer.pattern(), drummer.lessonAsHeard(),
                lesson.matches(life.pattern()), lesson.matches(drummer.pattern()),
                drummer.changesKept(), drummer.changesTakenBack(), drummer.copied(), drummer.ownIdeas(), drummer.cutShort(),
                drummer.hitSlot(n - 1), Audio.seconds(out.samples() - drummer.hitAt(n - 1)), drummer.lastPulse() / Audio.RATE,
                out.peak(), wav,
                barsTogether, together, together == 0 ? 0 : apart / together, together == 0 ? 0 : lean / together, alone, landedApart);
            life.remember(lesson.name(), answers, n, startedBpm, drummer.tempoNow(), early, late, drummer.listening(), drummer.ticks(),
                drummer.pattern(), lesson.matches(drummer.pattern()), barsTogether, together == 0 ? -1 : apart / together);
            return report;
        }
    }

    /** How close one drummer's hits landed to someone else's. */
    record Apart(int together, int alone, double sum, double lean, double landed) {
        double ms() { return together == 0 ? 0 : sum / together; }
        double leanMs() { return together == 0 ? 0 : lean / together; }
    }

    /** Each of the drummer's hits from place {@code from} on, against the other player's nearest hit. Called after the take. */
    static Apart apart(Drummer d, int from, Hits other) {
        int together = 0, alone = 0, t = 0, n = d.hitCount();
        double sum = 0, lean = 0;
        if (other.hitCount() == 0 || n == 0) return new Apart(0, n, 0, 0, 0);
        for (int i = 0; i < n; i++) {
            if (d.hitSlot(i) < from) continue;
            while (t + 1 < other.hitCount() && Math.abs(other.hitAt(t + 1) - d.hitAt(i)) <= Math.abs(other.hitAt(t) - d.hitAt(i))) t++;
            double ms = (d.hitAt(i) - other.hitAt(t)) * 1000.0 / Audio.RATE;
            if (Math.abs(ms) > 80) { alone++; continue; }
            together++; sum += Math.abs(ms); lean += ms;
        }
        double landed = (d.hitAt(n - 1) - other.hitAt(other.hitCount() - 1)) * 1000.0 / Audio.RATE;
        return new Apart(together, alone, sum, lean, landed);
    }

    /** What happened when two drummers played together, with no record. */
    public record Duo(int bars, double seconds, Part first, Part second, int hitsTogether, double apartMs, double leanMs,
                      double landedApartMs, double tempoFirstBars, double tempoLastBars, float peak, Path wav) {}

    /** One drummer's side of it. */
    public record Part(String name, int hits, double unsteadyMs, double listeningBefore, double listeningAfter,
                       int ticksBefore, int ticksAfter, String pattern, int alone) {}

    /**
     * Two drummers and no record. The first starts; the second hears a bar and comes in. Each has its own
     * clock and its own life, and each listens to the other. Nobody keeps time for them.
     */
    public static Duo together(Path wav, Path firstLife, Path secondLife, int bars, long seed) throws IOException {
        Life lifeA = Life.open(firstLife, "the drummer"), lifeB = Life.open(secondLife, "the second drummer");
        Drummer a = Drummer.withAnother(lifeA.name(), 92.0, lifeA.hitsPlayed(), lifeA.listening(), lifeA.ticks(), lifeA.pattern(), bars, true, seed);
        Drummer b = Drummer.withAnother(lifeB.name(), 92.0, lifeB.hitsPlayed(), lifeB.listening(), lifeB.ticks(), lifeB.pattern(), bars, false, seed ^ 0x9E3779B97F4A7C15L);
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromA = new float[Audio.BLOCK], fromB = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(30.0 + 8.0 * bars);
        try (WavWriter out = new WavWriter(wav)) {
            while (!(a.done(now) && b.done(now))) {          // no objects are made inside this loop
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                room.carry(0, others, own, Audio.BLOCK, now);
                a.hear(others, own, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                b.hear(others, own, Audio.BLOCK, now);
                java.util.Arrays.fill(fromA, 0f);
                java.util.Arrays.fill(fromB, 0f);
                a.play(fromA, Audio.BLOCK, now);
                b.play(fromB, Audio.BLOCK, now);
                room.fill(0, fromA, Audio.BLOCK, now);
                room.fill(1, fromB, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromA[i] + fromB[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            Apart bToA = apart(b, 0, a), aToB = apart(a, SLOTS_IN_A_BAR, b);   // the first drummer's opening bar is played alone
            double uA = unsteadiness(a, 0, a.hitCount()), uB = unsteadiness(b, 0, b.hitCount());
            Part first = new Part(lifeA.name(), a.hitCount(), uA, lifeA.listening(), a.listening(), lifeA.ticks(), a.ticks(), a.pattern(), aToB.alone());
            Part second = new Part(lifeB.name(), b.hitCount(), uB, lifeB.listening(), b.listening(), lifeB.ticks(), b.ticks(), b.pattern(), bToA.alone());
            Duo duo = new Duo(bars, Audio.seconds(out.samples()), first, second, bToA.together(), bToA.ms(), bToA.leanMs(), bToA.landed(),
                tempoOver(a, 0, 2), tempoOver(a, bars - 2, bars), out.peak(), wav);
            lifeA.rememberTogether(lifeB.name(), bars, a.hitCount(), uA, a.listening(), a.ticks(), a.pattern(), bToA.together(), bToA.ms());
            lifeB.rememberTogether(lifeA.name(), bars, b.hitCount(), uB, b.listening(), b.ticks(), b.pattern(), bToA.together(), bToA.ms());
            return duo;
        }
    }

    /** What happened when one drummer gave the lesson to another. */
    public record Taught(int calls, int barsTogether, double seconds, String teacher, String teacherPattern, int teacherHits, double teacherUnsteadyMs,
                         String learner, int learnerHits, int ticksBefore, int ticksAfter, String heardAs, String patternBefore, String patternAfter,
                         int matchesBefore, int matchesAfter, int copied, int ownIdeas, int changesKept, int changesTakenBack, int cutShort,
                         double listeningBefore, double listeningAfter, double learnerUnsteadyMs,
                         int hitsTogether, double apartMs, int alone, double landedApartMs, float peak, Path wav) {}

    /**
     * One drummer teaches another. The teacher plays its own pattern for two bars and leaves two bars of
     * room, on its own clock; the learner answers. After the last answer they play on together.
     */
    public static Taught teach(Path wav, Path teacherLife, Path learnerLife, int calls, int barsTogether, long seed) throws IOException {
        Life lifeT = Life.open(teacherLife, "the drummer"), lifeL = Life.open(learnerLife, "the second drummer");
        Drummer t = Drummer.teaching(lifeT.name(), 92.0, lifeT.hitsPlayed(), lifeT.listening(), lifeT.ticks(), lifeT.pattern(), calls, barsTogether, seed);
        Drummer l = new Drummer(lifeL.name(), 92.0, lifeL.hitsPlayed(), lifeL.listening(), lifeL.ticks(), lifeL.pattern(), calls, barsTogether, seed ^ 0x9E3779B97F4A7C15L);
        Lesson given = new Lesson(lifeT.name(), 92.0, lifeT.pattern());      // only used afterwards, to count how alike they ended
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromT = new float[Audio.BLOCK], fromL = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(60.0 + 30.0 * calls + 8.0 * barsTogether);
        try (WavWriter out = new WavWriter(wav)) {
            while (!(t.done(now) && l.done(now))) {          // no objects are made inside this loop
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                room.carry(0, others, own, Audio.BLOCK, now);
                t.hear(others, own, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                l.hear(others, own, Audio.BLOCK, now);
                java.util.Arrays.fill(fromT, 0f);
                java.util.Arrays.fill(fromL, 0f);
                t.play(fromT, Audio.BLOCK, now);
                l.play(fromL, Audio.BLOCK, now);
                room.fill(0, fromT, Audio.BLOCK, now);
                room.fill(1, fromL, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromT[i] + fromL[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            Apart with = apart(l, l.togetherFrom(), t);
            double uT = unsteadiness(t, 0, t.hitCount()), uL = unsteadiness(l, 0, l.hitCount());
            int before = given.matches(lifeL.pattern()), after = given.matches(l.pattern());
            Taught taught = new Taught(calls, barsTogether, Audio.seconds(out.samples()), lifeT.name(), t.pattern(), t.hitCount(), uT,
                lifeL.name(), l.hitCount(), lifeL.ticks(), l.ticks(), l.lessonAsHeard(), lifeL.pattern(), l.pattern(), before, after,
                l.copied(), l.ownIdeas(), l.changesKept(), l.changesTakenBack(), l.cutShort(), lifeL.listening(), l.listening(), uL,
                with.together(), with.ms(), with.alone(), barsTogether == 0 ? 0 : with.landed(), out.peak(), wav);
            lifeT.rememberTaught(lifeL.name(), calls, barsTogether, t.hitCount(), uT, t.listening(), t.ticks(), t.pattern(), after);
            lifeL.remember(lifeT.name(), calls, l.hitCount(), 92.0, l.tempoNow(), unsteadiness(l, 0, Math.min(24, l.hitCount() / 2)),
                unsteadiness(l, l.hitCount() - Math.min(24, l.hitCount() / 2), l.hitCount()), l.listening(), l.ticks(), l.pattern(), after,
                barsTogether, with.together() == 0 ? -1 : with.ms());
            return taught;
        }
    }

    private static final int SLOTS_IN_A_BAR = Drummer.SLOTS;

    /** The tempo a drummer actually played from one bar to another, from when its hits landed. */
    static double tempoOver(Drummer d, int fromBar, int toBar) {
        int first = -1, last = -1;
        for (int i = 0; i < d.hitCount(); i++) {
            int bar = d.hitSlot(i) / Drummer.SLOTS;
            if (bar >= fromBar && bar <= toBar) { if (first < 0) first = i; last = i; }
        }
        if (first < 0 || last == first) return 0;
        double places = d.hitSlot(last) - d.hitSlot(first);
        double pulse = (d.hitAt(last) - d.hitAt(first)) / places * (Drummer.SLOTS / Drummer.PULSES);
        return Audio.bpmOf(pulse);
    }

    /**
     * How uneven the playing is, in milliseconds: how far each hit lands from where the two hits before it
     * said it would, allowing for how many places apart they are. Only hits inside the same answer are
     * compared. A slow change of tempo does not count against the player; a lurch does.
     */
    static double unsteadiness(Drummer d, int from, int to) {
        int count = 0, answer = Drummer.TURN * Drummer.SLOTS;
        double sumSquares = 0;
        for (int i = from + 2; i < to; i++) {
            if (d.hitSlot(i) / answer != d.hitSlot(i - 2) / answer) continue;
            double gapA = d.hitAt(i - 1) - d.hitAt(i - 2), gapB = d.hitAt(i) - d.hitAt(i - 1);
            double placesA = d.hitSlot(i - 1) - d.hitSlot(i - 2), placesB = d.hitSlot(i) - d.hitSlot(i - 1);
            double off = gapB - gapA * placesB / placesA;
            sumSquares += off * off;
            count++;
        }
        return count == 0 ? 0 : Math.sqrt(sumSquares / count) / Audio.RATE * 1000.0;
    }
}
