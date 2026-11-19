package com.eyeoverthink.band;

import java.nio.file.Path;
import java.util.Locale;

/** Sit the drummer down for one session and write what it played. */
public final class Main {
    public static void main(String[] args) throws Exception {
        int bars = 16;
        long seed = System.nanoTime();
        Path wav = Path.of("out", "session.wav");
        Path life = Path.of("lives", "drummer.life");
        for (int i = 0; i + 1 < args.length; i += 2) {
            switch (args[i]) {
                case "--bars" -> bars = Integer.parseInt(args[i + 1]);
                case "--seed" -> seed = Long.parseLong(args[i + 1]);
                case "--out" -> wav = Path.of(args[i + 1]);
                case "--life" -> life = Path.of(args[i + 1]);
                default -> { System.err.println("Unknown option " + args[i] + ". Options: --bars N --seed N --out FILE --life FILE"); System.exit(2); }
            }
        }
        Session.Report r = Session.run(wav, life, bars, seed);
        System.out.printf(Locale.ROOT, "Played %d bars, %d hits, in %.1f s and heard %d of them come back.%n", r.bars(), r.hits(), r.seconds(), r.heard());
        System.out.printf(Locale.ROOT, "Tempo went from %.1f to %.1f bpm.%n", r.startedBpm(), r.endedBpm());
        System.out.printf(Locale.ROOT, "Unsteadiness: %.1f ms at the start, %.1f ms by the end.%n", r.earlyUnsteadyMs(), r.lateUnsteadyMs());
        System.out.printf(Locale.ROOT, "How much it listens to itself: %.2f before, %.2f after.%n", r.listeningBefore(), r.listeningAfter());
        System.out.printf(Locale.ROOT, "Ticks to the pulse: %d before, %d after.%n", r.ticksBefore(), r.ticksAfter());
        System.out.printf(Locale.ROOT, "Pattern: %s before, %s after (%d changes kept, %d taken back).%n", r.patternBefore(), r.patternAfter(), r.changesKept(), r.changesTakenBack());
        System.out.printf(Locale.ROOT, "It landed on the one and let it ring %.2f s; its pulse was %.2f s.%n", r.ringSeconds(), r.lastPulseSeconds());
        System.out.println("Wrote " + r.wav() + " and added the session to " + life);
    }
}
