package com.eyeoverthink.band;

import java.nio.file.Path;
import java.util.Locale;

/** Put a record on, sit the drummer down, and write what happened. */
public final class Main {
    public static void main(String[] args) throws Exception {
        int answers = 4, together = -1;
        Path with = null, teach = null, bass = null;
        boolean click = false;
        double tempo = 92;
        long seed = System.nanoTime();
        Path wav = Path.of("out", "session.wav");
        Path life = Path.of("lives", "drummer.life");
        Path lessonFile = Path.of("lessons", "first.lesson");
        for (int i = 0; i + 1 < args.length; i += 2) {
            switch (args[i]) {
                case "--answers" -> answers = Integer.parseInt(args[i + 1]);
                case "--together" -> together = Integer.parseInt(args[i + 1]);
                case "--seed" -> seed = Long.parseLong(args[i + 1]);
                case "--out" -> wav = Path.of(args[i + 1]);
                case "--life" -> life = Path.of(args[i + 1]);
                case "--click" -> click = args[i + 1].equalsIgnoreCase("on") || args[i + 1].equalsIgnoreCase("yes");
                case "--tempo" -> tempo = Double.parseDouble(args[i + 1]);
                case "--with" -> with = Path.of(args[i + 1]);
                case "--bass" -> bass = Path.of(args[i + 1]);
                case "--teach" -> teach = Path.of(args[i + 1]);
                case "--lesson" -> lessonFile = Path.of(args[i + 1]);
                default -> { System.err.println("Unknown option " + args[i] + ". Options: --lesson FILE --answers N --together BARS --with LIFE --teach LIFE --bass LIFE --click on --tempo BPM --seed N --out FILE --life FILE"); System.exit(2); }
            }
        }
        if (bass != null) {                                  // drummers and a bass player
            java.util.List<Path> drummers = with == null ? java.util.List.of(life) : java.util.List.of(life, with);
            Session.Band b = Session.band(wav, drummers, bass, together < 0 ? 16 : together, click ? tempo : 0, seed);
            System.out.printf(Locale.ROOT, "%d bars, %.1f s, no record. %s started; the others came in after a bar.%n", b.bars(), b.seconds(), b.parts().get(0).name());
            if (click) System.out.printf(Locale.ROOT, "Everyone had a click in their ears at %.0f bpm. It is not on the recording.%n", tempo);
            for (Session.Part p : b.parts())
                System.out.printf(Locale.ROOT, "%s: %s, %d ticks to the pulse, %d hits, unsteadiness %.1f ms, listening %.2f to %.2f, leaning on the others %.2f to %.2f.%n",
                    p.name(), p.pattern(), p.ticksAfter(), p.hits(), p.unsteadyMs(), p.listeningBefore(), p.listeningAfter(), p.leaningBefore(), p.leaningAfter());
            System.out.printf(Locale.ROOT, "The bass went from %s to %s: %d notes in the bar, %d low ones where a drummer has a pot.%n", b.bassBefore(), b.bassAfter(), b.bassNotes(), b.bassNotesOnAPot());
            System.out.printf(Locale.ROOT, "It has %d note%s (%d before). By its own ear its notes were %.0f hundredths of a semitone out of tune on average.%n",
                b.bassNotesKnownAfter(), b.bassNotesKnownAfter() == 1 ? "" : "s", b.bassNotesKnownBefore(), b.bassOutOfTuneCents());
            if (b.bassNotesKnownAfter() > 1) System.out.printf(Locale.ROOT, "%d of the notes in its bar are the high one, %d of them where a drummer has a pan.%n", b.bassHighNotes(), b.bassHighNotesOnAPan());
            System.out.printf(Locale.ROOT, "It tried %d places it had heard a hit and %d ideas of its own; %d kept, %d taken back.%n", b.bassCopied(), b.bassOwnIdeas(), b.bassKept(), b.bassTakenBack());
            System.out.printf(Locale.ROOT, "%d of its notes landed with the first drummer's hits, %.0f ms apart on average. Their last hits were %.0f ms apart.%n", b.bassHits(), b.bassApartMs(), Math.abs(b.landedApartMs()));
            System.out.printf(Locale.ROOT, "They started near %.1f bpm and ended near %.1f bpm.%n", b.tempoFirstBars(), b.tempoLastBars());
            System.out.println("Wrote " + b.wav() + " and added the session to every life.");
            return;
        }
        if (teach != null) {                                 // one drummer gives the lesson to another
            Session.Taught t = Session.teach(wav, life, teach, answers, together < 0 ? 8 : together, click ? tempo : 0, seed);
            if (click) System.out.printf(Locale.ROOT, "Both had a click in their ears at %.0f bpm. It is not on the recording.%n", tempo);
            System.out.printf(Locale.ROOT, "%s played its pattern for %s: %s%n", t.teacher(), t.learner(), t.teacherPattern());
            System.out.printf(Locale.ROOT, "With %d ticks to the pulse %s heard it as: %s%n", t.ticksAfter(), t.learner(), t.heardAs());
            System.out.printf(Locale.ROOT, "Its pattern before: %s (like the teacher's in %d of 16 places)%n", t.patternBefore(), t.matchesBefore());
            System.out.printf(Locale.ROOT, "Its pattern after:  %s (like the teacher's in %d of 16 places)%n", t.patternAfter(), t.matchesAfter());
            System.out.printf(Locale.ROOT, "%d answers, %.1f s. %d changes copied, %d its own idea; %d kept, %d taken back; cut short %d times.%n",
                t.calls(), t.seconds(), t.copied(), t.ownIdeas(), t.changesKept(), t.changesTakenBack(), t.cutShort());
            System.out.printf(Locale.ROOT, "Ticks to the pulse: %d before, %d after. Listening: %.2f before, %.2f after.%n", t.ticksBefore(), t.ticksAfter(), t.listeningBefore(), t.listeningAfter());
            System.out.printf(Locale.ROOT, "Unsteadiness: the teacher %.1f ms, the learner %.1f ms.%n", t.teacherUnsteadyMs(), t.learnerUnsteadyMs());
            System.out.printf(Locale.ROOT, "Together for %d bars: %d of the learner's hits landed with the teacher's, %.0f ms apart on average; %d landed nowhere near. Their last hits were %.0f ms apart.%n",
                t.barsTogether(), t.hitsTogether(), t.apartMs(), t.alone(), Math.abs(t.landedApartMs()));
            System.out.println("Wrote " + t.wav() + " and added the session to " + life + " and " + teach);
            return;
        }
        if (with != null) {                                  // two drummers, no record
            Session.Duo d = Session.together(wav, life, with, together < 0 ? 16 : together, click ? tempo : 0, seed);
            if (click) System.out.printf(Locale.ROOT, "Both had a click in their ears at %.0f bpm. It is not on the recording.%n", tempo);
            System.out.printf(Locale.ROOT, "%d bars, %.1f s, no record. %s started; %s came in after a bar.%n", d.bars(), d.seconds(), d.first().name(), d.second().name());
            for (Session.Part p : new Session.Part[] {d.first(), d.second()})
                System.out.printf(Locale.ROOT, "%s: %s, %d ticks to the pulse, %d hits, unsteadiness %.1f ms, listening %.2f to %.2f, %d hits with nothing of the other's near.%n",
                    p.name(), p.pattern(), p.ticksAfter(), p.hits(), p.unsteadyMs(), p.listeningBefore(), p.listeningAfter(), p.alone());
            System.out.printf(Locale.ROOT, "%d of the second's hits landed with the first's, %.0f ms apart on average (%+.0f ms: plus is behind).%n", d.hitsTogether(), d.apartMs(), d.leanMs());
            System.out.printf(Locale.ROOT, "They started near %.1f bpm and ended near %.1f bpm. Their last hits were %.0f ms apart.%n", d.tempoFirstBars(), d.tempoLastBars(), Math.abs(d.landedApartMs()));
            System.out.println("Wrote " + d.wav() + " and added the session to " + life + " and " + with);
            return;
        }
        if (together < 0) together = 8;
        Lesson lesson = Lesson.read(lessonFile);
        Session.Report r = Session.run(wav, life, lesson, answers, together, click, seed);
        if (click) System.out.printf(Locale.ROOT, "The drummer had a click in its ears at the record's tempo. It is not on the recording.%n");
        System.out.printf(Locale.ROOT, "The record played \"%s\" at %.0f bpm: %s%n", lesson.name(), lesson.tempo(), lesson.bar());
        System.out.printf(Locale.ROOT, "With %d ticks to the pulse the drummer heard it as: %s%n", r.ticksAfter(), r.lessonAsHeard());
        System.out.printf(Locale.ROOT, "Its pattern before: %s (like the lesson in %d of 16 places)%n", r.patternBefore(), r.matchesBefore());
        System.out.printf(Locale.ROOT, "Its pattern after:  %s (like the lesson in %d of 16 places)%n", r.patternAfter(), r.matchesAfter());
        System.out.printf(Locale.ROOT, "%d answers, %d hits, %.1f s. %d changes copied from the record, %d its own idea; %d kept, %d taken back; cut short %d times.%n",
            r.answers(), r.hits(), r.seconds(), r.copied(), r.ownIdeas(), r.changesKept(), r.changesTakenBack(), r.cutShort());
        System.out.printf(Locale.ROOT, "Ticks to the pulse: %d before, %d after. Listening: %.2f before, %.2f after.%n", r.ticksBefore(), r.ticksAfter(), r.listeningBefore(), r.listeningAfter());
        System.out.printf(Locale.ROOT, "Unsteadiness: %.1f ms at the start, %.1f ms by the end. Its tempo ended at %.1f bpm.%n", r.earlyUnsteadyMs(), r.lateUnsteadyMs(), r.endedBpm());
        if (r.barsTogether() > 0) {
            System.out.printf(Locale.ROOT, "Together for %d bars: %d of its hits landed with the record, %.0f ms apart on average (%+.0f ms: plus is behind the record); %d landed nowhere near.%n",
                r.barsTogether(), r.hitsTogether(), r.apartMs(), r.leanMs(), r.alone());
            System.out.printf(Locale.ROOT, "Their last hits were %.0f ms apart.%n", Math.abs(r.landedApartMs()));
        }
        StringBuilder feel = new StringBuilder();
        for (int i = 0; i < Musician.SLOTS; i++) if (r.patternAfter().charAt(i) != '.')
            feel.append(String.format(Locale.ROOT, " %d:%+.0fms/%.2f", i + 1, r.feelMs()[i], r.feelWeight()[i]));
        System.out.println("Its feel now, for each place it plays (place: after the tick / how hard):" + feel);
        System.out.printf(Locale.ROOT, "It landed on the one and let it ring %.2f s; its pulse was %.2f s.%n", r.ringSeconds(), r.lastPulseSeconds());
        System.out.println("Wrote " + r.wav() + " and added the session to " + life);
    }
}
