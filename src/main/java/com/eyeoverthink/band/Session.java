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
                         int cutShort, int lastHitPlace, double ringSeconds, double lastPulseSeconds, float peak, Path wav) {}

    public static final double ROOM_DELAY = 0.030;

    public static Report run(Path wav, Path lifeFile, Lesson lesson, int answers, long seed) throws IOException {
        Life life = Life.open(lifeFile, "the drummer");
        Teacher teacher = new Teacher(lesson, answers, 0.25);
        Drummer drummer = new Drummer(life.name(), 92.0, life.hitsPlayed(), life.listening(), life.ticks(), life.pattern(), answers, seed);
        Room room = new Room(ROOM_DELAY);
        float[] heard = new float[Audio.BLOCK];
        float[] played = new float[Audio.BLOCK];
        double startedBpm = drummer.tempoNow();

        long now = 0;
        try (WavWriter out = new WavWriter(wav)) {
            long tooLong = (long) Audio.samples(60.0 + 30.0 * answers);   // nothing here should ever need this
            while (!drummer.done(now)) {
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");                     // no objects are made inside this loop
                room.carry(heard, Audio.BLOCK, now);
                teacher.hear(heard, Audio.BLOCK, now);
                drummer.hear(heard, Audio.BLOCK, now);
                java.util.Arrays.fill(played, 0f);
                teacher.play(played, Audio.BLOCK, now);
                drummer.play(played, Audio.BLOCK, now);
                room.fill(played, Audio.BLOCK, now);
                out.write(played, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            int n = drummer.hitCount(), window = Math.min(24, n / 2);
            double early = unsteadiness(drummer, 0, window);
            double late = unsteadiness(drummer, n - window, n);
            Report report = new Report(lesson, answers, n, Audio.seconds(out.samples()),
                startedBpm, drummer.tempoNow(), early, late, life.listening(), drummer.listening(),
                life.ticks(), drummer.ticks(), life.pattern(), drummer.pattern(), drummer.lessonAsHeard(),
                lesson.matches(life.pattern()), lesson.matches(drummer.pattern()),
                drummer.changesKept(), drummer.changesTakenBack(), drummer.copied(), drummer.ownIdeas(), drummer.cutShort(),
                drummer.hitSlot(n - 1), Audio.seconds(out.samples() - drummer.hitAt(n - 1)), drummer.lastPulse() / Audio.RATE,
                out.peak(), wav);
            life.remember(lesson.name(), answers, n, startedBpm, drummer.tempoNow(), early, late, drummer.listening(), drummer.ticks(),
                drummer.pattern(), lesson.matches(drummer.pattern()));
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
