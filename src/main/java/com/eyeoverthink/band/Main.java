package com.eyeoverthink.band;

import java.nio.file.Path;
import java.util.Locale;

/** Put a record on, sit the drummer down, and write what happened. */
public final class Main {
    public static void main(String[] args) throws Exception {
        int answers = 4, together = -1;
        Path with = null, teach = null, bass = null, hear = null, along = null, hats = null;
        String askBass = null, askHats = null, askDrummer = null, askWords = "";
        String born = null, bornPupil = null;
        double from = 46;
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
                case "--ask-bass" -> askBass = args[i + 1];
                case "--ask-hats" -> askHats = args[i + 1];
                case "--ask-drummer" -> askDrummer = args[i + 1];
                case "--in-words" -> askWords = args[i + 1];
                case "--name" -> born = args[i + 1];
                case "--name-pupil" -> bornPupil = args[i + 1];
                case "--hats" -> hats = Path.of(args[i + 1]);
                case "--along" -> along = Path.of(args[i + 1]);
                case "--from" -> from = Double.parseDouble(args[i + 1]);
                case "--hear" -> hear = Path.of(args[i + 1]);
                case "--bass" -> bass = Path.of(args[i + 1]);
                case "--teach" -> teach = Path.of(args[i + 1]);
                case "--lesson" -> lessonFile = Path.of(args[i + 1]);
                default -> { System.err.println("Unknown option " + args[i] + ". Options: --lesson FILE --answers N --together BARS --with LIFE --teach LIFE --bass LIFE --hear WAV --along WAV --hats LIFE --ask-bass PLACES --ask-hats PLACES --ask-drummer PLACES --in-words TEXT --name NAME --name-pupil NAME --from SECONDS --click on --tempo BPM --seed N --out FILE --life FILE"); System.exit(2); }
            }
        }
        // A newborn can be given a name. One that already has a life keeps the name it has.
        if (born != null && !java.nio.file.Files.exists(life)) {
            if (life.getParent() != null) java.nio.file.Files.createDirectories(life.getParent());
            java.nio.file.Files.write(life, java.util.List.of("name: " + born));
        }
        Path pupil = teach != null ? teach : with;           // the same for a newborn it is about to teach, or play with
        if (bornPupil != null && pupil != null && !java.nio.file.Files.exists(pupil)) {
            if (pupil.getParent() != null) java.nio.file.Files.createDirectories(pupil.getParent());
            java.nio.file.Files.write(pupil, java.util.List.of("name: " + bornPupil));
        }
        // The bandleader asks a player for particular places. It is written into that player's life before anyone plays.
        if (askBass != null && bass != null) Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN).ask(askBass, askWords);
        if (askHats != null && hats != null) Life.open(hats, "the hat player", Musician.FIRST_HATS_PATTERN).ask(askHats, askWords);
        if (askDrummer != null) Life.open(life, "the drummer").ask(askDrummer, askWords);
        if (along != null) {                                 // the drummer plays along with a recording
            float[] song = Hearing.read(along);
            Hearing.Heard h = Hearing.listenLowAndHigh(song);
            Hearing.Bar b = Hearing.oneBar(h);
            String title = along.getFileName().toString().replaceFirst("\\.[^.]+$", "").replace('-', ' ');
            java.util.List<Path> drummers = with == null ? java.util.List.of(life) : java.util.List.of(life, with);
            Session.Along a = Session.playAlong(wav, drummers, hats, bass, title, song, h, b, from, together < 0 ? 24 : together, seed);
            System.out.printf(Locale.ROOT, "Played along with \"%s\" (%.2f to the minute) for %d bars, starting on the one at %.1f s. %.1f s in all.%n", a.recording(), a.tempo(), a.bars(), a.fromSeconds(), a.seconds());
            if (b.swing() >= 0) System.out.printf(Locale.ROOT, "The song's sixteenths swing %.0f%% (50 is straight, 67 is the triplet).%n", b.swing());
            for (Session.AlongPart p : a.parts()) {
                System.out.printf(Locale.ROOT, "%s, on %s: went from %s to %s. It believes it heard %s.%n", p.name(), p.plays(), p.patternBefore(), p.patternAfter(), p.believes());
                System.out.println(p.swing() < 0 ? "   It plays nothing off the eighths, so it has no swing to speak of." : String.format(Locale.ROOT, "   Its sixteenths swing %.0f%% (50 is straight, 67 is the triplet).", p.swing()));
                if (p.borrowed() > 0 || !p.own().matches("\\.*")) System.out.printf(Locale.ROOT, "   Ideas borrowed from other records: %d tried, %d held%s. Places that are its own: %s%n", p.borrowed(), p.borrowedKept(), p.borrowedKept() > 0 ? " (the last from \"" + p.borrowedFrom() + "\")" : "", p.own());
                if (p.askedAndPlaying() + p.askedAndWaiting() > 0) System.out.printf(Locale.ROOT, "   It is playing %d of the places it was asked for; %d are waiting until it can reach them.%n", p.askedAndPlaying(), p.askedAndWaiting());
                if (p.withTheSong() + p.againstTheSong() > 0) System.out.printf(Locale.ROOT, "   It believes a phrase is %d bars (the line came round, of the times it compared, after 1, 2, 4 and 8 bars: %s). The line: %s (%d of %d places). %d of its notes were on the note the song was on, %d were not. Out of tune by %.0f cents.%n", p.phraseBars(), p.phraseHeard(), p.line(), p.linePlaces(), p.linePlacesInReach(), p.withTheSong(), p.againstTheSong(), p.outOfTuneCents());
                System.out.printf(Locale.ROOT, "   %d changes taken from what it heard, %d its own idea; %d kept, %d taken back. %d of its %d hits landed within 25 ms of a hit in the recording, %.0f ms apart. Unsteadiness %.1f ms. Listening %.2f to %.2f.%n",
                    p.copied(), p.ownIdeas(), p.kept(), p.takenBack(), p.landedWithIt(), p.hits(), p.apartMs(), p.unsteadyMs(), p.listeningBefore(), p.listeningAfter());
            }
            System.out.println("Wrote " + a.wav() + " (the recording and the players together; the click is not on it) and added the session to every life.");
            return;
        }
        if (hear != null) {                                  // what does a player's ear make of a recording?
            Hearing.Heard h = Hearing.listen(Hearing.read(hear));
            System.out.printf(Locale.ROOT, "%s: %.0f seconds. The ear noticed %d hits: %d dark, %d bright. That is %.1f a second.%n",
                hear.getFileName(), h.seconds(), h.hits(), h.dark(), h.bright(), h.hits() / h.seconds());
            System.out.printf(Locale.ROOT, "The gap that comes up most often between hits is %.3f s, which is %.1f to the minute (%.0f%% of all gaps under two seconds).%n",
                h.pulseSeconds(), h.bpm(), 100 * h.pulseStrength());
            float[] song = Hearing.read(hear);
            Hearing.Heard split = Hearing.listenLowAndHigh(song);
            System.out.printf(Locale.ROOT, "With one ear on the bottom of the sound and one on the top: %d hits in the bottom, %d in the top.%n", split.dark(), split.bright());
            Hearing.Bar b = Hearing.oneBar(split);
            System.out.printf(Locale.ROOT, "The hits line up best on a steady pulse of %.2f to the minute (%.2f, where 1 is perfectly and 0 is not at all), about %d bars.%n", b.bpm(), b.linesUp(), b.barsHeard());
            System.out.println(b.swing() < 0 ? "Nothing falls off the eighths, so there is no swing to measure." : String.format(Locale.ROOT, "The sixteenths swing %.0f%% (50 is straight, 67 is the triplet).", b.swing()));
            System.out.println("Folded onto one bar, taking the place where the dark hits weigh most as the one:");
            StringBuilder dark = new StringBuilder("  dark hits per bar:  "), bright = new StringBuilder("  bright hits per bar:"), push = new StringBuilder(), weight = new StringBuilder();
            for (int i = 0; i < Musician.SLOTS; i++) {
                dark.append(String.format(Locale.ROOT, " %.1f", b.darkPerBar()[i])); bright.append(String.format(Locale.ROOT, " %.1f", b.brightPerBar()[i]));
                push.append(String.format(Locale.ROOT, "%s%.0f", i == 0 ? "" : " ", b.pushMs()[i])); weight.append(String.format(Locale.ROOT, "%s%.2f", i == 0 ? "" : " ", b.weight()[i]));
            }
            System.out.println(dark); System.out.println(bright);
            System.out.println("  as a bar:           " + b.bar());
            String stem = hear.getFileName().toString().replaceFirst("\\.[^.]+$", "");
            Path lesson = hear.resolveSibling(stem + ".lesson");
            java.nio.file.Files.write(lesson, java.util.List.of("name: " + stem.replace('-', ' '), String.format(Locale.ROOT, "tempo: %.2f", b.bpm()),
                "bar:    " + b.bar(), "push:   " + push, "weight: " + weight));
            System.out.println("Wrote that bar, with its feel, as a lesson: " + lesson);
            Path marked = hear.resolveSibling(stem + "-pulse-marked.wav");
            double clipFrom = Math.min(from, Math.max(0, h.seconds() - 30));
            try (WavWriter out = new WavWriter(marked)) {
                float[] clip = Hearing.withThePulseMarked(song, b, clipFrom, 30);
                float[] block = new float[Audio.BLOCK];
                for (int at = 0; at + Audio.BLOCK <= clip.length; at += Audio.BLOCK) { System.arraycopy(clip, at, block, 0, Audio.BLOCK); out.write(block, Audio.BLOCK); }
            }
            System.out.printf(Locale.ROOT, "Wrote 30 seconds from %.0f s with a click where it thinks the pulse is and a higher click on the one: %s%n", clipFrom, marked);
            return;
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
            System.out.printf(Locale.ROOT, "Its pattern before: %s (like the teacher's in %d of its places)%n", t.patternBefore(), t.matchesBefore());
            System.out.printf(Locale.ROOT, "Its pattern after:  %s (like the teacher's in %d of its places)%n", t.patternAfter(), t.matchesAfter());
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
        System.out.printf(Locale.ROOT, "Its pattern before: %s (like the lesson in %d of its places)%n", r.patternBefore(), r.matchesBefore());
        System.out.printf(Locale.ROOT, "Its pattern after:  %s (like the lesson in %d of its places)%n", r.patternAfter(), r.matchesAfter());
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
        for (int i = 0; i < r.patternAfter().length(); i++) if (r.patternAfter().charAt(i) != '.')
            feel.append(String.format(Locale.ROOT, " %d:%+.0fms/%.2f", i + 1, r.feelMs()[i], r.feelWeight()[i]));
        System.out.println("Its feel now, for each place it plays (place: after the tick / how hard):" + feel);
        System.out.printf(Locale.ROOT, "It landed on the one and let it ring %.2f s; its pulse was %.2f s.%n", r.ringSeconds(), r.lastPulseSeconds());
        System.out.println("Wrote " + r.wav() + " and added the session to " + life);
    }
}
