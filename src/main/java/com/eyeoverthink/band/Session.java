package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Path;

/**
 * One sitting. The room carries sound to the players, they hear, they play, and the take is written down.
 * Every buffer is claimed in {@link #run} before the first block; the loop itself only moves samples.
 * The session does not decide when the take ends. The player does.
 */
public final class Session {
    /** What happened, measured from the hits themselves. */
    public record Report(int bars, int hits, long heard, double seconds, double startedBpm, double endedBpm,
                         double earlyUnsteadyMs, double lateUnsteadyMs, double listeningBefore, double listeningAfter,
                         int ticksBefore, int ticksAfter, String patternBefore, String patternAfter,
                         int changesKept, int changesTakenBack, int lastHitPlace, double ringSeconds, double lastPulseSeconds,
                         float peak, Path wav) {}

    public static final double ROOM_DELAY = 0.030;

    public static Report run(Path wav, Path lifeFile, int bars, long seed) throws IOException {
        Life life = Life.open(lifeFile, "the drummer");
        Drummer drummer = new Drummer(life.name(), 92.0, life.hitsPlayed(), life.listening(), life.ticks(), life.pattern(), bars, seed);
        Room room = new Room(ROOM_DELAY);
        float[] heard = new float[Audio.BLOCK];
        float[] played = new float[Audio.BLOCK];
        double startedBpm = drummer.tempoNow();

        long now = 0;
        try (WavWriter out = new WavWriter(wav)) {
            while (!drummer.done(now)) {                     // no objects are made inside this loop
                room.carry(heard, Audio.BLOCK, now);
                drummer.hear(heard, Audio.BLOCK, now);
                java.util.Arrays.fill(played, 0f);
                drummer.play(played, Audio.BLOCK, now);
                room.fill(played, Audio.BLOCK, now);
                out.write(played, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            int n = drummer.hitCount(), window = Math.min(16, n / 2);
            double early = unsteadiness(drummer, 0, window);
            double late = unsteadiness(drummer, n - window, n);
            Report report = new Report(bars, n, drummer.heardOnsets(), Audio.seconds(out.samples()),
                startedBpm, drummer.tempoNow(), early, late, life.listening(), drummer.listening(),
                life.ticks(), drummer.ticks(), life.pattern(), drummer.pattern(),
                drummer.changesKept(), drummer.changesTakenBack(), drummer.hitSlot(n - 1),
                Audio.seconds(out.samples() - drummer.hitAt(n - 1)), drummer.lastPulse() / Audio.RATE,
                out.peak(), wav);
            life.remember(bars, n, startedBpm, drummer.tempoNow(), early, late, drummer.listening(), drummer.ticks(), drummer.pattern());
            return report;
        }
    }

    /**
     * How uneven the playing is, in milliseconds: how far each hit lands from where the two hits before it
     * said it would, allowing for how many places apart they are. A slow change of tempo does not count
     * against the player; a lurch does.
     */
    static double unsteadiness(Drummer d, int from, int to) {
        int count = 0;
        double sumSquares = 0;
        for (int i = from + 2; i < to; i++) {
            double gapA = d.hitAt(i - 1) - d.hitAt(i - 2), gapB = d.hitAt(i) - d.hitAt(i - 1);
            double placesA = d.hitSlot(i - 1) - d.hitSlot(i - 2), placesB = d.hitSlot(i) - d.hitSlot(i - 1);
            double off = gapB - gapA * placesB / placesA;
            sumSquares += off * off;
            count++;
        }
        return count == 0 ? 0 : Math.sqrt(sumSquares / count) / Audio.RATE * 1000.0;
    }
}
