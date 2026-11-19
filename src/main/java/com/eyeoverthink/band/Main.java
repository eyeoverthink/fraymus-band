package com.eyeoverthink.band;

import java.nio.file.Path;
import java.util.Locale;

/** Put a record on, sit the drummer down, and write what happened. */
public final class Main {
    public static void main(String[] args) throws Exception {
        int answers = 6;
        long seed = System.nanoTime();
        Path wav = Path.of("out", "session.wav");
        Path life = Path.of("lives", "drummer.life");
        Path lessonFile = Path.of("lessons", "first.lesson");
        for (int i = 0; i + 1 < args.length; i += 2) {
            switch (args[i]) {
                case "--answers" -> answers = Integer.parseInt(args[i + 1]);
                case "--seed" -> seed = Long.parseLong(args[i + 1]);
                case "--out" -> wav = Path.of(args[i + 1]);
                case "--life" -> life = Path.of(args[i + 1]);
                case "--lesson" -> lessonFile = Path.of(args[i + 1]);
                default -> { System.err.println("Unknown option " + args[i] + ". Options: --lesson FILE --answers N --seed N --out FILE --life FILE"); System.exit(2); }
            }
        }
        Lesson lesson = Lesson.read(lessonFile);
        Session.Report r = Session.run(wav, life, lesson, answers, seed);
        System.out.printf(Locale.ROOT, "The record played \"%s\" at %.0f bpm: %s%n", lesson.name(), lesson.tempo(), lesson.bar());
        System.out.printf(Locale.ROOT, "With %d ticks to the pulse the drummer heard it as: %s%n", r.ticksAfter(), r.lessonAsHeard());
        System.out.printf(Locale.ROOT, "Its pattern before: %s (like the lesson in %d of 16 places)%n", r.patternBefore(), r.matchesBefore());
        System.out.printf(Locale.ROOT, "Its pattern after:  %s (like the lesson in %d of 16 places)%n", r.patternAfter(), r.matchesAfter());
        System.out.printf(Locale.ROOT, "%d answers, %d hits, %.1f s. %d changes copied from the record, %d its own idea; %d kept, %d taken back; cut short %d times.%n",
            r.answers(), r.hits(), r.seconds(), r.copied(), r.ownIdeas(), r.changesKept(), r.changesTakenBack(), r.cutShort());
        System.out.printf(Locale.ROOT, "Ticks to the pulse: %d before, %d after. Listening: %.2f before, %.2f after.%n", r.ticksBefore(), r.ticksAfter(), r.listeningBefore(), r.listeningAfter());
        System.out.printf(Locale.ROOT, "Unsteadiness: %.1f ms at the start, %.1f ms by the end. Its tempo ended at %.1f bpm.%n", r.earlyUnsteadyMs(), r.lateUnsteadyMs(), r.endedBpm());
        System.out.printf(Locale.ROOT, "It landed on the one and let it ring %.2f s; its pulse was %.2f s.%n", r.ringSeconds(), r.lastPulseSeconds());
        System.out.println("Wrote " + r.wav() + " and added the session to " + life);
    }
}
