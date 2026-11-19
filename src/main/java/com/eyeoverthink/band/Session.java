package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Path;

/**
 * One sitting. The room carries sound to the players, they hear, they play, and the take is written down.
 * Every buffer is claimed in {@link #run} before the first block; the loop itself only moves samples.
 */
public final class Session {
    /** What happened, measured from the hits themselves. */
    public record Report(int pulses, long heard, double seconds, double startedBpm, double endedBpm,
                         double earlyUnsteadyMs, double lateUnsteadyMs, double listeningBefore, double listeningAfter, float peak, Path wav) {}

    public static final double ROOM_DELAY = 0.030;

    public static Report run(Path wav, Path lifeFile, int pulses, long seed) throws IOException {
        Life life = Life.open(lifeFile, "the drummer");
        Drummer drummer = new Drummer(life.name(), 92.0, life.pulsesPlayed(), life.listening(), seed, pulses + 8);
        double listeningBefore = life.listening();
        Room room = new Room(ROOM_DELAY);
        float[] heard = new float[Audio.BLOCK];
        float[] played = new float[Audio.BLOCK];
        double startedBpm = drummer.tempoNow();

        long now = 0;
        long tail = -1;
        try (WavWriter out = new WavWriter(wav)) {
            while (true) {                                   // no objects are made inside this loop
                room.carry(heard, Audio.BLOCK, now);
                drummer.hear(heard, Audio.BLOCK, now);
                java.util.Arrays.fill(played, 0f);
                if (drummer.hitCount() < pulses) drummer.play(played, Audio.BLOCK, now);
                else {                                       // the last hit rings out, then the take ends
                    if (tail < 0) tail = now + (long) Audio.samples(0.6);
                    drummer.ringOut(played, Audio.BLOCK);
                    if (now >= tail) break;
                }
                room.fill(played, Audio.BLOCK, now);
                out.write(played, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            double early = unsteadiness(drummer, 0, Math.min(16, pulses / 2));
            double late = unsteadiness(drummer, Math.max(0, drummer.hitCount() - Math.min(16, pulses / 2)), drummer.hitCount());
            Report report = new Report(drummer.hitCount(), drummer.heardOnsets(), Audio.seconds(out.samples()),
                startedBpm, drummer.tempoNow(), early, late, listeningBefore, drummer.listening(), out.peak(), wav);
            life.remember(drummer.hitCount(), startedBpm, drummer.tempoNow(), early, late, drummer.listening());
            return report;
        }
    }

    /**
     * How uneven the gaps between hits are, in milliseconds: the typical difference between one gap and the
     * next. A slow change of tempo does not count against the player; a lurch from beat to beat does.
     */
    static double unsteadiness(Drummer d, int from, int to) {
        int count = 0;
        double sumSquares = 0;
        for (int i = from + 2; i < to; i++) {
            double gapA = d.hitAt(i - 1) - d.hitAt(i - 2);
            double gapB = d.hitAt(i) - d.hitAt(i - 1);
            sumSquares += (gapB - gapA) * (gapB - gapA);
            count++;
        }
        return count == 0 ? 0 : Math.sqrt(sumSquares / count) / Audio.RATE * 1000.0;
    }
}
