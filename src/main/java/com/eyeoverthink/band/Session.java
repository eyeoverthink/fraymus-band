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
            // How close did they land while playing together? Each of the drummer's hits against the record's nearest.
            int together = 0, alone = 0, t = 0;
            double apart = 0, lean = 0;
            for (int i = 0; i < n; i++) {
                if (drummer.hitSlot(i) < drummer.togetherFrom()) continue;
                while (t + 1 < teacher.hitCount() && Math.abs(teacher.hitAt(t + 1) - drummer.hitAt(i)) <= Math.abs(teacher.hitAt(t) - drummer.hitAt(i))) t++;
                double ms = teacher.hitCount() == 0 ? 1e9 : (drummer.hitAt(i) - teacher.hitAt(t)) * 1000.0 / Audio.RATE;
                if (Math.abs(ms) > 80) { alone++; continue; }
                together++; apart += Math.abs(ms); lean += ms;
            }
            double landedApart = barsTogether == 0 || teacher.hitCount() == 0 ? 0
                : (drummer.hitAt(n - 1) - teacher.hitAt(teacher.hitCount() - 1)) * 1000.0 / Audio.RATE;
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
