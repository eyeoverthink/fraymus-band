package com.eyeoverthink.band;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/** Step nineteen: hat and bass players count bars, so a part is told from a passing sound. */
class StepThirtyFourTest {
    static final Lesson LESSON = new Lesson("first lesson", 96, "p..pn...p.p.n..n");

    @Test
    void theTakeIsARealWavFile(@TempDir Path dir) throws Exception {
        Path wav = dir.resolve("take.wav");
        Session.Report r = Session.run(wav, dir.resolve("d.life"), LESSON, 2, 0, 7);
        byte[] b = Files.readAllBytes(wav);
        ByteBuffer h = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals("RIFF", new String(b, 0, 4));
        assertEquals("WAVE", new String(b, 8, 4));
        assertEquals(b.length - 8, h.getInt(4));
        assertEquals(1, h.getShort(20), "plain PCM");
        assertEquals(1, h.getShort(22), "mono");
        assertEquals(Audio.RATE, h.getInt(24));
        assertEquals(16, h.getShort(34), "16-bit");
        assertEquals(b.length - 44, h.getInt(40));
        assertEquals(r.seconds(), (b.length - 44) / 2.0 / Audio.RATE, 1e-9);
        assertTrue(r.seconds() > 15, "two calls and two answers last well over fifteen seconds");
    }

    @Test
    void aLessonIsSixteenPlacesOfPotPanOrNothing(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("mine.lesson");
        Files.write(file, List.of("name: mine", "tempo: 104", "bar: p.n.p.n.p.n.p.nn"));
        Lesson l = Lesson.read(file);
        assertEquals("mine", l.name());
        assertEquals(104, l.tempo());
        assertEquals(16, l.matches("p.n.p.n.p.n.p.nn"));
        assertEquals(15, l.matches("p.n.p.n.p.n.p.n."));
        assertThrows(IllegalArgumentException.class, () -> new Lesson("short", 96, "p...n..."));
        assertThrows(IllegalArgumentException.class, () -> new Lesson("odd", 96, "p...x...p...n..."));
    }

    @Test
    void theEarTellsThePotFromThePan() {
        Ear ear = new Ear();
        float[] air = new float[Audio.RATE];
        Sound pot = new Thump(), pan = new Pan();
        for (int i = 0; i < pot.length(); i++) air[1000 + i] += pot.at(i) * 0.8f;
        for (int i = 0; i < pan.length(); i++) air[20000 + i] += pan.at(i) * 0.8f;
        ear.listen(air, 15000, 0);
        assertEquals(1, ear.hitsEver());
        assertFalse(ear.hitBright(), "the pot is dark");
        double potLoud = ear.hitLoud();
        float[] rest = java.util.Arrays.copyOfRange(air, 15000, air.length);
        ear.listen(rest, rest.length, 15000);
        assertEquals(2, ear.hitsEver());
        assertTrue(ear.hitBright(), "the pan is bright");
        assertTrue(potLoud > ear.hitLoud(), "and the pot is the heavier of the two");
    }

    @Test
    void aNewbornHasOneTickToThePulseAndThePotOnEveryPulse(@TempDir Path dir) throws Exception {
        Session.Report r = Session.run(dir.resolve("take.wav"), dir.resolve("d.life"), LESSON, 2, 0, 5);
        assertEquals(1, r.ticksBefore());
        assertEquals("p...p...p...p...", r.patternBefore());
    }

    /** A life written by hand, so the test can ask what a practised drummer with a given number of ticks hears. */
    private static Path practised(Path dir, String name, int ticks) throws Exception { return practised(dir, name, ticks, "p...p...p...p..."); }

    private static Path practised(Path dir, String name, int ticks, String pattern) throws Exception {
        Path file = dir.resolve(name);
        Files.write(file, List.of("name: the drummer", "age: 0", "listens to itself: 0.60", "ticks to the pulse: " + ticks,
            "pattern: " + pattern, "", "session 1: bars=400, hits=3000, made up for a test"));
        return file;
    }

    @Test
    void aPlayerHearsItselfAtOnceAndTheOthersFromAcrossTheRoom() {
        Room room = new Room(2, Session.NEAR, Session.ACROSS);
        float[] click = new float[Audio.BLOCK], quiet = new float[Audio.BLOCK], others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        click[0] = 1f;
        int near = (int) Math.round(Audio.samples(Session.NEAR)), across = (int) Math.round(Audio.samples(Session.ACROSS));
        long ownAt = -1, othersAt = -1;
        for (long now = 0; now < 2000; now += Audio.BLOCK) {
            room.carry(1, others, own, Audio.BLOCK, now);
            for (int i = 0; i < Audio.BLOCK; i++) assertEquals(0f, others[i], "player 1 never hears its own click as someone else");
            for (int i = 0; i < Audio.BLOCK; i++) if (own[i] != 0) ownAt = now + i;
            room.carry(0, others, own, Audio.BLOCK, now);
            for (int i = 0; i < Audio.BLOCK; i++) if (others[i] != 0) othersAt = now + i;
            room.fill(0, quiet, Audio.BLOCK, now);
            room.fill(1, now == 0 ? click : quiet, Audio.BLOCK, now);
        }
        assertEquals(near, ownAt);
        assertEquals(across, othersAt);
    }

    @Test
    void theMetronomeCountsOneBarInAndClicksEvenly() {
        Metronome m = new Metronome(120, 0.25);
        assertEquals(0.25 + 4 * 0.5, m.firstBarSeconds(), 1e-4);
        float[] block = new float[Audio.BLOCK];
        long before = -1; int clicks = 0;
        boolean wasQuiet = true;
        for (long now = 0; now < Audio.RATE * 3L; now += Audio.BLOCK) {
            m.play(block, Audio.BLOCK, now);
            for (int i = 0; i < Audio.BLOCK; i++) {
                boolean quiet = block[i] == 0f;
                if (wasQuiet && !quiet && (before < 0 || now + i - before > 1000)) {
                    if (before >= 0) assertEquals(Audio.RATE / 2.0, now + i - before, 2.0, "half a second between clicks at 120");
                    before = now + i; clicks++;
                }
                wasQuiet = quiet;
            }
        }
        assertEquals(6, clicks);
    }

    @Test
    void theClickIsInTheirEarsAndNotOnTheRecording(@TempDir Path dir) throws Exception {
        Path wav = dir.resolve("t.wav");
        Session.together(wav, practised(dir, "a.life", 4, LESSON.bar()), practised(dir, "b.life", 4, LESSON.bar()), 4, 92, 3);
        byte[] b = Files.readAllBytes(wav);
        ByteBuffer samples = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN);
        int countIn = (int) (Audio.RATE * (0.25 + 4 * 60.0 / 92) * 0.95);
        for (int i = 0; i < countIn; i++) assertEquals(0, samples.getShort(44 + 2 * i), "the bar of count-in is silent on the recording");
    }

    @Test
    void withAClickTwoDrummersHoldTheTempo(@TempDir Path dir) throws Exception {
        for (int seed = 0; seed < 10; seed++) {
            Session.Duo d = Session.together(dir.resolve("t.wav"), practised(dir, "a" + seed + ".life", 4, LESSON.bar()),
                practised(dir, "b" + seed + ".life", 4, "p...n...p.p.n..."), 16, 92, seed);
            assertEquals(92, d.tempoFirstBars(), 2.0, "they start at the click's tempo");
            assertEquals(92, d.tempoLastBars(), 2.0, "and are still there sixteen bars later");
            assertTrue(Math.abs(d.landedApartMs()) < 40, "and end together");
        }
    }

    @Test
    void aNewbornNoLongerDragsAPractisedDrummerWhenBothHaveAClick(@TempDir Path dir) throws Exception {
        double without = 0, with = 0;
        for (int seed = 0; seed < 10; seed++) {
            without += Session.together(dir.resolve("t.wav"), practised(dir, "a" + seed + ".life", 4, LESSON.bar()), dir.resolve("n" + seed + ".life"), 16, 0, seed).first().unsteadyMs() / 10;
            with += Session.together(dir.resolve("t.wav"), practised(dir, "c" + seed + ".life", 4, LESSON.bar()), dir.resolve("m" + seed + ".life"), 16, 92, seed).first().unsteadyMs() / 10;
        }
        assertTrue(with < without * 0.7, "the practised drummer's unsteadiness: " + without + " ms without the click, " + with + " ms with it");
    }

    @Test
    void aClickHelpsAYoungDrummerLearnFromAnother(@TempDir Path dir) throws Exception {
        double[] like = new double[2];
        for (int c = 0; c < 2; c++)
            for (int d = 0; d < 8; d++) {
                Path young = dir.resolve(c + "y" + d + ".life");
                for (int s = 0; s < 12; s++) {
                    Session.Taught t = Session.teach(dir.resolve("t.wav"), practised(dir, c + "o" + d + ".life", 4, LESSON.bar()), young, 4, 8, c == 0 ? 0 : 92, 1000L * d + s);
                    if (s == 11) like[c] += t.matchesAfter() / 8.0;
                }
            }
        System.out.printf(Locale.ROOT, "CLICK after twelve lessons: like the teacher in %.1f of 16 places without a click, %.1f with one%n", like[0], like[1]);
        assertTrue(like[1] > like[0] + 0.5, "more like the teacher with a click: " + like[0] + " without, " + like[1] + " with");
    }

    @Test
    void withAClickOldHandsLandALittleCloserOverTimeAndTakeUpLeaningSlowly(@TempDir Path dir) throws Exception {
        int pairs = 8, sessions = 8;
        double[] apart = new double[sessions], leaning = new double[sessions];
        for (int d = 0; d < pairs; d++) {
            Path a = practised(dir, "a" + d + ".life", 4, LESSON.bar()), b = practised(dir, "b" + d + ".life", 4, "p...n...p.p.n...");
            for (int s = 0; s < sessions; s++) {
                Session.Duo duo = Session.together(dir.resolve("t.wav"), a, b, 16, 92, 1000L * d + s);
                if (s == 0) assertEquals(0, duo.first().leaningBefore(), "nobody starts out leaning on the others");
                else assertEquals(Life.open(a, "x").leaning(), duo.first().leaningAfter(), 0.006, "what it learned is in its life");
                apart[s] += duo.apartMs() / pairs;
                leaning[s] += (duo.first().leaningAfter() + duo.second().leaningAfter()) / 2 / pairs;
                assertEquals(92, duo.tempoLastBars(), 2.0, "the click still holds the tempo");
            }
        }
        for (int s = 0; s < sessions; s++)
            System.out.printf(Locale.ROOT, "LEAN session %d: %.1f ms apart, leaning on each other %.2f%n", s + 1, apart[s], leaning[s]);
        double late = (apart[sessions - 3] + apart[sessions - 2] + apart[sessions - 1]) / 3;
        assertTrue(late < apart[0], "they land a little closer: " + apart[0] + " ms at first, " + late + " ms later");
        assertTrue(leaning[sessions - 1] < 0.2, "old hands do not take up leaning quickly; the gain is too small to hear clearly: " + leaning[sessions - 1]);
    }

    @Test
    void theBassIsDarkToTheEarLikeAPot() {
        Ear ear = new Ear();
        Bass bass = new Bass();
        bass.strike(Kit.POT, 1f);
        float[] air = new float[Audio.RATE / 2];
        for (int i = 1000; i < air.length; i++) air[i] = bass.next();
        ear.listen(air, air.length, 0);
        assertEquals(1, ear.hitsEver(), "one note is one hit");
        assertFalse(ear.hitBright(), "a drummer or a bass player hears it as dark");
    }

    /** Four bands of two practised drummers and a newborn bass player, twenty-four sessions each, with a click. */
    @Test
    void whatAPlayerHasLearnedDoesNotWander(@TempDir Path dir) throws Exception {
        int bands = 4, sessions = 24, from = 8, rough = 0, takes = 0;
        double wander = 0;
        for (int d = 0; d < bands; d++) {
            Path a = practised(dir, "a" + d + ".life", 4, LESSON.bar()), b = practised(dir, "b" + d + ".life", 4, "p...n...p.p.n..."), bass = dir.resolve("bass" + d + ".life");
            double[][] listening = new double[3][sessions];
            for (int s = 0; s < sessions; s++) {
                Session.Band band = Session.band(dir.resolve("t.wav"), List.of(a, b), bass, 16, 92, 1000L * d + s);
                for (int i = 0; i < 3; i++) listening[i][s] = band.parts().get(i).listeningAfter();
                if (s >= from) { takes++; if (band.parts().get(0).unsteadyMs() > 14 || band.parts().get(1).unsteadyMs() > 14) rough++; }
            }
            for (int i = 0; i < 2; i++) {                    // how far a practised drummer's listening moves from one session to the next
                double jumps = 0;
                for (int s = from + 1; s < sessions; s++) jumps += Math.abs(listening[i][s] - listening[i][s - 1]);
                wander += jumps / (sessions - from - 1) / (2 * bands);
            }
        }
        System.out.printf(Locale.ROOT, "WANDER a practised drummer's listening changes by %.3f from one session to the next; a practised drummer was unsteadier than 14 ms in %d of %d takes%n", wander, rough, takes);
        assertTrue(wander < 0.06, "its listening moves a little each session, not a lot: " + wander);
        assertTrue(rough * 8 <= takes, "a practised drummer rarely has a rough take: " + rough + " of " + takes);
    }

    @Test
    void aLessonCanSayHowItIsPlayed(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("feel.lesson");
        Files.write(file, List.of("name: feel", "tempo: 96", "bar:    p...n...p...n...",
            "push:   0 0 0 0 18 0 0 0 0 0 0 0 18 0 0 0", "weight: 1 0 0 0 0.8 0 0 0 0.7 0 0 0 0.8 0 0 0"));
        Lesson l = Lesson.read(file);
        assertEquals(18, l.pushMs()[4]);
        assertEquals(0.8, l.weight()[12]);
        assertEquals(0, new Lesson("plain", 96, "p...n...p...n...").pushMs()[4], "a lesson that says nothing about feel is played on the tick");
        Files.write(file, List.of("bar: p...n...p...n...", "push: 0 0 0 0 80 0 0 0 0 0 0 0 0 0 0 0"));
        assertThrows(IllegalArgumentException.class, () -> Lesson.read(file), "80 ms late is a different place in the bar, not a feel");
        for (String name : new String[] {"first", "laid-back", "on-top"}) {
            Lesson shipped = Lesson.read(Path.of("lessons", name + ".lesson"));
            for (int i = 1; i < 16; i++) assertTrue(shipped.weight()[i] < shipped.weight()[0] - 0.1, name + ": the one must be the heaviest hit");
        }
    }

    /** Six practised drummers raised for six sessions on each of two records with the same places and opposite feels. */
    @Test
    void aDrummerTakesOnTheFeelOfTheRecordItHears(@TempDir Path dir) throws Exception {
        Lesson laidBack = Lesson.read(Path.of("lessons", "laid-back.lesson")), onTop = Lesson.read(Path.of("lessons", "on-top.lesson"));
        double[] pans = new double[2], pickup = new double[2], panWeight = new double[2], unsteady = new double[2];
        for (int c = 0; c < 2; c++)
            for (int d = 0; d < 6; d++) {
                Path life = dir.resolve(c + "-" + d + ".life");
                Files.write(life, List.of("name: the drummer", "age: 0", "listens to itself: 0.80", "ticks to the pulse: 4", "habits: 9999999999999999",
                    "pattern: " + laidBack.bar(), "", "session 1: bars=400, hits=6000, made up for a test"));
                Session.Report r = null;
                for (int s = 0; s < 6; s++) {
                    r = Session.run(dir.resolve("t.wav"), life, c == 0 ? laidBack : onTop, 4, 8, true, 1000L * d + s);
                    if (s == 0) assertTrue(Math.abs(r.feelMs()[4]) < 8, "after one session it only half trusts what it heard: " + r.feelMs()[4]);
                }
                pans[c] += (r.feelMs()[4] + r.feelMs()[12]) / 2 / 6;
                pickup[c] += r.feelMs()[3] / 6;
                panWeight[c] += (r.feelWeight()[4] + r.feelWeight()[12]) / 2 / 6;
                unsteady[c] += r.lateUnsteadyMs() / 6;
                assertEquals(1.0, r.feelWeight()[0], 1e-9, "the one stays the heaviest");
                assertTrue(Files.readAllLines(life).stream().anyMatch(l -> l.startsWith("feel: ")), "its feel is in its life");
            }
        System.out.printf(Locale.ROOT, "FEEL raised on \"laid back\": pans %+.1f ms (record +22), pickup %+.1f ms (record -8), pans hit %.2f hard (record 0.85), unsteadiness %.1f ms%n", pans[0], pickup[0], panWeight[0], unsteady[0]);
        System.out.printf(Locale.ROOT, "FEEL raised on \"on top\":    pans %+.1f ms (record -9), pickup %+.1f ms (record 0), pans hit %.2f hard (record 0.70), unsteadiness %.1f ms%n", pans[1], pickup[1], panWeight[1], unsteady[1]);
        assertEquals(22, pans[0], 5, "raised on the laid-back record it plays its pans behind the tick");
        assertEquals(-8, pickup[0], 4, "and its pickup ahead");
        assertTrue(pans[1] < -3, "raised on the other record it plays its pans ahead: " + pans[1]);
        assertEquals(0.85, panWeight[0], 0.1, "and hits them as hard as the record did");
        assertEquals(0.70, panWeight[1], 0.1);
        assertTrue(unsteady[0] < 13 && unsteady[1] < 13, "playing with feel is not playing unsteadily: " + unsteady[0] + ", " + unsteady[1]);
    }

    /** The record alone, written to a file, then heard back the way a recording Vaughn sends would be. */
    @Test
    void theEarCanFoldACleanRecordingBackIntoItsBar(@TempDir Path dir) throws Exception {
        Lesson laidBack = Lesson.read(Path.of("lessons", "laid-back.lesson"));
        Teacher record = new Teacher(laidBack, 0, 24, 0.25);
        Path wav = dir.resolve("record.wav");
        float[] block = new float[Audio.BLOCK], none = new float[Audio.BLOCK];
        try (WavWriter out = new WavWriter(wav)) {
            for (long now = 0; now < Audio.RATE * 62L; now += Audio.BLOCK) {
                java.util.Arrays.fill(block, 0f);
                record.hear(none, none, none, none, Audio.BLOCK, now);
                record.play(block, Audio.BLOCK, now);
                out.write(block, Audio.BLOCK);
            }
        }
        Hearing.Heard heard = Hearing.listen(Hearing.read(wav));
        Hearing.Bar bar = Hearing.oneBar(heard);
        System.out.printf(Locale.ROOT, "HEARD a clean record: %d hits, %.2f to the minute, lines up %.2f, bar %s, pans %+.0f and %+.0f ms%n",
            heard.hits(), bar.bpm(), bar.linesUp(), bar.bar(), bar.pushMs()[4], bar.pushMs()[12]);
        assertEquals(24 * 7 + 1, heard.hits(), 3, "it notices nearly every hit");
        assertEquals(96, bar.bpm(), 0.3, "it finds the tempo");
        assertEquals(laidBack.bar(), bar.bar(), "and the bar, with the one in the right place");
        assertEquals(22, bar.pushMs()[4], 6, "and that the pans are behind the tick");
        assertEquals(22, bar.pushMs()[12], 6);
        assertEquals(laidBack.bar(), bar.asLesson("heard").bar(), "and what it heard can be used as a lesson");
    }

    /** A made-up song: a record's drum part, a bass line under it, hats over it and a voice in the middle. */
    private static float[] madeUpSong(Lesson drums, int bars) {
        Teacher record = new Teacher(drums, 0, bars, 0.25);
        float[] mix = new float[(int) (Audio.RATE * (bars * 4 * 60.0 / drums.tempo() + 3))], block = new float[Audio.BLOCK];
        for (int now = 0; now + Audio.BLOCK <= mix.length; now += Audio.BLOCK) {
            java.util.Arrays.fill(block, 0f);
            record.play(block, Audio.BLOCK, now);
            System.arraycopy(block, 0, mix, now, Audio.BLOCK);
        }
        java.util.SplittableRandom chance = new java.util.SplittableRandom(8);
        double sixteenth = 60.0 / drums.tempo() / 4;
        for (int tick = 0; tick < bars * 16; tick++) {
            int at = (int) Audio.samples(0.25 + tick * sixteenth);
            if (tick % 2 == 0)                               // a quiet hat on every eighth
                for (int i = 0; i < 900; i++) mix[at + i] += (float) (0.05 * (chance.nextDouble() * 2 - 1) * Math.exp(-i / 200.0));
            if (chance.nextInt(3) == 0) {                    // a bass note now and then, softer than the kick
                double hz = 55 * Math.pow(2, chance.nextInt(8) / 12.0);
                for (int i = 0; i < 7000; i++) mix[at + i] += (float) (0.10 * Math.sin(2 * Math.PI * hz * i / Audio.RATE) * Math.min(1, i / 400.0) * Math.exp(-i / 3000.0));
            }
        }
        for (int note = 0; note < bars * 6; note++) {        // a voice in the middle of the sound
            int at = chance.nextInt(mix.length - Audio.RATE), length = (int) Audio.samples(0.15 + 0.3 * chance.nextDouble());
            double hz = 300 * Math.pow(4, chance.nextDouble());
            for (int i = 0; i < length; i++) mix[at + i] += (float) (0.3 * Math.sin(2 * Math.PI * hz * i / Audio.RATE) * Math.min(1, i / 200.0) * Math.min(1, (length - i) / 400.0));
        }
        return mix;
    }

    @Test
    void aDrummerPlaysAlongWithARecordingAndPicksUpItsDrumPart(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        assertEquals(92, bar.bpm(), 0.3, "the tool finds the tempo to set the click by");
        int moved = 0, landedWell = 0; double unsteady = 0;
        for (int d = 0; d < 6; d++) {
            Path life = dir.resolve(d + ".life");
            Files.write(life, List.of("name: the drummer", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 4",
                "pattern: p...p...p...p...", "", "session 1: bars=400, hits=3000, made up for a test"));
            Session.Along take = null;
            for (int s = 0; s < 3; s++) take = Session.playAlong(dir.resolve("t.wav"), life, "made up", song, heard, bar, 5, 24, 100L * d + s);
            Session.AlongPart a = take.parts().get(0);
            assertTrue(take.peak() > 0.2f && take.peak() < 1.0f, "loud enough to hear, never clipped: " + take.peak());
            assertEquals(24 * Musician.SLOTS, Math.round(a.hits() == 0 ? 0 : 24 * Musician.SLOTS), "it counts its bars");
            if (drums.matches(a.patternAfter()) > drums.matches("p...p...p...p...")) moved++;
            if (a.landedWithIt() * 10 >= a.hits() * 6) landedWell++;
            unsteady += a.unsteadyMs() / 6;
            List<String> lines = Files.readAllLines(life);
            assertTrue(lines.get(lines.size() - 1).contains("played along with \"made up\" for 24 bars"));
            if (d == 0) System.out.printf(Locale.ROOT, "ALONG the song's drums are %s; after three takes the drummer plays %s and believes it heard %s; %d of %d hits landed with the song, %.0f ms apart%n",
                drums.bar(), a.patternAfter(), a.believes(), a.landedWithIt(), a.hits(), a.apartMs());
        }
        assertTrue(moved >= 5, "its pattern moved toward the song's drum part in " + moved + " of 6 lives");
        assertTrue(landedWell >= 5, "most of its hits landed with a hit in the song in " + landedWell + " of 6 lives");
        assertTrue(unsteady < 14, "and it kept its time: " + unsteady + " ms");
    }

    @Test
    void theWholeBandPlaysAlongWithARecording(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        int steady = 0, moved = 0;
        for (int d = 0; d < 4; d++) {
            Path first = dir.resolve(d + "a.life"), second = dir.resolve(d + "b.life"), bass = dir.resolve(d + "c.life");
            for (Path life : new Path[] {first, second})
                Files.write(life, List.of("name: " + (life == first ? "the drummer" : "the second drummer"), "age: 0", "listens to itself: 0.60", "ticks to the pulse: 4",
                    "pattern: p...p...p...p...", "", "session 1: bars=400, hits=3000, made up for a test"));
            Files.write(bass, List.of("name: the bass player", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 2", "notes: 2",
                "pattern: p...............", "", "session 1: played 400 bars, hits=2000, made up for a test"));
            Session.Along take = null;
            for (int s = 0; s < 3; s++) take = Session.playAlong(dir.resolve("t.wav"), List.of(first, second), bass, "made up", song, heard, bar, 5, 24, 100L * d + s);
            assertEquals(3, take.parts().size());
            assertTrue(take.peak() > 0.2f && take.peak() < 1.0f, "loud enough to hear, never clipped: " + take.peak());
            boolean allSteady = true;
            for (Session.AlongPart p : take.parts()) if (p.unsteadyMs() > 14) allSteady = false;
            if (allSteady) steady++;
            Session.AlongPart a = take.parts().get(0), b = take.parts().get(1), c = take.parts().get(2);
            if (drums.matches(a.patternAfter()) > 12 && drums.matches(b.patternAfter()) > 12) moved++;
            assertEquals("the bass player", c.name());
            for (int i = 0; i < 16; i++) if (i % (4 / c.ticksAfter()) != 0) assertEquals('.', c.patternAfter().charAt(i), "the bass player plays only what its ticks reach (it has " + c.ticksAfter() + " to the pulse)");
            assertTrue(c.hits() > 24, "the bass player found more to play than its one note on the one: " + c.patternAfter());
            assertTrue(Files.readAllLines(bass).get(Files.readAllLines(bass).size() - 1).contains("played along with \"made up\""));
            if (d == 0) System.out.printf(Locale.ROOT, "BAND ALONG the song's drums are %s; the drummers play %s and %s, the bass player %s%n", drums.bar(), a.patternAfter(), b.patternAfter(), c.patternAfter());
        }
        assertTrue(steady >= 3, "everyone kept their time in " + steady + " of 4 bands");
        assertTrue(moved >= 3, "both drummers moved toward the song's drum part in " + moved + " of 4 bands");
    }

    @Test
    void theHatIsBrightToTheEarAndAHatPlayerPlaysNothingElse(@TempDir Path dir) throws Exception {
        Ear ear = new Ear();
        Hats hats = new Hats();
        hats.strike(Kit.PAN, 1f);
        float[] air = new float[Audio.RATE / 2];
        for (int i = 1000; i < air.length; i++) air[i] = hats.next();
        ear.listen(air, air.length, 0);
        assertEquals(1, ear.hitsEver(), "one hat is one hit");
        assertTrue(ear.hitBright(), "and it is bright");
        assertEquals("a hi-hat (n)", hats.describe());
    }

    /** A made-up song with a quiet hat on every eighth. A drummer who moves to the hats should find them; the kit drummer should not. */
    @Test
    void eachPlayerTakesItsOwnPartFromTheSong(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        int hatsRight = 0, kitRight = 0, steady = 0;
        for (int d = 0; d < 4; d++) {
            Path kit = dir.resolve(d + "a.life"), hat = dir.resolve(d + "b.life"), bass = dir.resolve(d + "c.life");
            for (Path life : new Path[] {kit, hat})
                Files.write(life, List.of("name: " + (life == kit ? "the drummer" : "the second drummer"), "age: 0", "listens to itself: 0.60", "sounds: a pot (p) and a pan (n)",
                    "ticks to the pulse: 4", "pattern: p...p...p...p...", "", "session 1: bars=400, hits=3000, made up for a test"));
            Files.write(bass, List.of("name: the bass player", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 2", "notes: 2",
                "pattern: p...............", "", "session 1: played 400 bars, hits=2000, made up for a test"));
            Session.Along take = null;
            for (int s = 0; s < 4; s++) take = Session.playAlong(dir.resolve("t.wav"), List.of(kit), hat, bass, "made up", song, heard, bar, 5, 24, 100L * d + s);
            assertEquals(3, take.parts().size());
            assertTrue(take.peak() > 0.2f && take.peak() < 1.0f, "loud enough to hear, never clipped: " + take.peak());
            Session.AlongPart k = take.parts().get(0), h = take.parts().get(1), b = take.parts().get(2);
            assertEquals("a hi-hat (n)", h.plays());
            assertTrue(Files.readAllLines(hat).contains("sounds: a hi-hat (n)"), "moving to the hats is written into its life");
            assertEquals('n', h.patternAfter().charAt(0), "a hat player's one is a hat");
            int onEighths = 0, offEighths = 0;
            for (int i = 0; i < 16; i++) {
                assertNotEquals('p', h.patternAfter().charAt(i), "a hat player has no pot: " + h.patternAfter());
                if (h.patternAfter().charAt(i) == 'n') { if (i % 2 == 0) onEighths++; else offEighths++; }
            }
            if (onEighths >= 6 && offEighths <= 2) hatsRight++;
            if (drums.matches(k.patternAfter()) >= 14) kitRight++;
            if (k.unsteadyMs() < 14 && h.unsteadyMs() < 14 && b.unsteadyMs() < 14) steady++;
            if (d == 0) System.out.printf(Locale.ROOT, "ROLES the song has drums %s and a hat on every eighth; the kit drummer plays %s, the hat player %s, the bass player %s%n",
                drums.bar(), k.patternAfter(), h.patternAfter(), b.patternAfter());
        }
        assertTrue(hatsRight >= 3, "the hat player found the eighth-note hats in " + hatsRight + " of 4 bands");
        assertTrue(kitRight >= 3, "the kit drummer stayed on the kick and snare in " + kitRight + " of 4 bands");
        assertTrue(steady >= 3, "everyone kept their time in " + steady + " of 4 bands");
    }

    /** The record, with something loud in the middle of the sound laid over it, the way a voice sits over the drums. */
    @Test
    void twoEarsHearTheRecordUnderAVoice() throws Exception {
        Lesson lesson = Lesson.read(Path.of("lessons", "first.lesson"));
        Teacher record = new Teacher(lesson, 0, 24, 0.25);
        float[] mix = new float[Audio.RATE * 62], block = new float[Audio.BLOCK], none = new float[Audio.BLOCK];
        for (int now = 0; now + Audio.BLOCK <= mix.length; now += Audio.BLOCK) {
            java.util.Arrays.fill(block, 0f);
            record.play(block, Audio.BLOCK, now);
            System.arraycopy(block, 0, mix, now, Audio.BLOCK);
        }
        java.util.SplittableRandom chance = new java.util.SplittableRandom(5);
        for (int note = 0; note < 150; note++) {             // a voice: notes between 300 and 1200 Hz, starting whenever they like
            int at = chance.nextInt(mix.length - Audio.RATE), length = (int) Audio.samples(0.15 + 0.3 * chance.nextDouble());
            double hz = 300 * Math.pow(4, chance.nextDouble());
            for (int i = 0; i < length; i++)
                mix[at + i] += (float) (0.35 * Math.sin(2 * Math.PI * hz * i / Audio.RATE) * Math.min(1, i / 200.0) * Math.min(1, (length - i) / 400.0));
        }
        Hearing.Heard whole = Hearing.listen(mix), split = Hearing.listenLowAndHigh(mix);
        Hearing.Bar oneEar = Hearing.oneBar(whole), twoEars = Hearing.oneBar(split);
        System.out.printf(Locale.ROOT, "VOICE over the record: one ear makes it %s (%d hits), two ears make it %s (%d low, %d high); the record plays %s%n",
            oneEar.bar(), whole.hits(), twoEars.bar(), split.dark(), split.bright(), lesson.bar());
        assertEquals(96, twoEars.bpm(), 0.3, "two ears still find the tempo");
        assertTrue(lesson.matches(twoEars.bar()) >= 15, "and the bar, or all but one place of it: " + twoEars.bar());
        assertTrue(lesson.matches(twoEars.bar()) >= lesson.matches(oneEar.bar()), "at least as well as one ear does");
        float[] marked = Hearing.withThePulseMarked(mix, twoEars, 10, 5);
        assertEquals(Audio.RATE * 5, marked.length, "and it can mark the pulse on a stretch for someone to check");
    }

    @Test
    void aNewbornHasNoHabitsAndTheyAreWrittenIntoItsLife(@TempDir Path dir) throws Exception {
        Path life = dir.resolve("d.life");
        assertEquals("0000000000000000", Life.open(life, "x").habits());
        Session.Report r = Session.run(dir.resolve("t.wav"), life, LESSON, 4, 0, true, 3);
        String habits = Life.open(life, "x").habits();
        int reach = 4 / r.ticksAfter();
        assertTrue(habits.matches("[0-9]{16}"), habits);
        assertNotEquals("0000000000000000", habits, "four answers leave some places a little settled");
        for (int i = 0; i < 16; i++) if (i % reach != 0) assertEquals('0', habits.charAt(i), "a place it cannot reach has no habit: " + habits);
        assertTrue(Files.readAllLines(life).contains("habits: " + habits));
    }

    /** The same drummer, the same record, the same chances: once with no habits, once thoroughly settled. */
    @Test
    void aSettledDrummerLeavesAloneWhatHasWorked(@TempDir Path dir) throws Exception {
        int[] stayed = new int[2];
        for (int c = 0; c < 2; c++)
            for (int seed = 0; seed < 12; seed++) {
                Path life = dir.resolve(c + "-" + seed + ".life");
                Files.write(life, List.of("name: the drummer", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 4",
                    "pattern: " + LESSON.bar(), "habits: " + (c == 0 ? "0000000000000000" : "9999999999999999"), "", "session 1: bars=400, hits=3000, made up for a test"));
                Session.Report r = null;
                for (int s = 0; s < 3; s++) r = Session.run(dir.resolve("t.wav"), life, LESSON, 4, 0, true, 100L * seed + s);
                if (r.patternAfter().equals(LESSON.bar())) stayed[c]++;
            }
        System.out.printf(Locale.ROOT, "SETTLED after three sessions it still plays the lesson exactly: %d of 12 with no habits, %d of 12 settled%n", stayed[0], stayed[1]);
        assertTrue(stayed[1] >= 10, "a settled drummer keeps the lesson it knows: " + stayed[1] + " of 12");
        assertTrue(stayed[1] > stayed[0], "more often than one with no habits: " + stayed[0] + " of 12");
    }

    @Test
    void itsEarHearsHowFarOffANoteIs() {
        for (byte sound : new byte[] {Kit.POT, Kit.PAN})
            for (double cents : new double[] {-30, -12, 0, 9, 30}) {
                Bass bass = new Bass();
                PitchEar ear = new PitchEar();
                bass.detune(cents);
                bass.strike(sound, 0.9f);
                float[] block = new float[Audio.BLOCK];
                ear.noteBegan(0);
                long now = 0;
                while (!ear.ready(now)) {
                    for (int i = 0; i < Audio.BLOCK; i++) block[i] = bass.next();
                    ear.listen(block, Audio.BLOCK, now);
                    now += Audio.BLOCK;
                }
                double meant = Bass.pitchOf(sound);
                assertEquals(cents, PitchEar.cents(ear.hz(meant), meant), 3.0, "a note " + cents + " hundredths of a semitone off");
            }
        assertEquals(1200, PitchEar.cents(Bass.HIGH, Bass.LOW), 1.0, "the high note is an octave above the low one");
    }

    @Test
    void withTwoNotesItPutsTheHighOneWhereItHearsThePan(@TempDir Path dir) throws Exception {
        int onAPan = 0, lowOnAPot = 0, high = 0;
        for (int d = 0; d < 6; d++) {
            Path bass = dir.resolve("bass" + d + ".life");
            Files.write(bass, List.of("name: the bass player", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 2", "notes: 2",
                "pattern: p...............", "", "session 1: played 400 bars, hits=2000, made up for a test"));
            Session.Band band = null;
            for (int s = 0; s < 5; s++)
                band = Session.band(dir.resolve("t.wav"), List.of(practised(dir, "a" + d + ".life", 4, LESSON.bar()),
                    practised(dir, "b" + d + ".life", 4, "p...n...p.p.n...")), bass, 16, 92, 1000L * d + s);
            onAPan += band.bassHighNotesOnAPan(); lowOnAPot += band.bassNotesOnAPot(); high += band.bassHighNotes();
        }
        System.out.printf(Locale.ROOT, "HIGH after five sessions, six players: %d high notes, %d of them on a pan; %d low notes on a pot%n", high, onAPan, lowOnAPot);
        assertTrue(onAPan >= 6, "about one high note each on a pan: " + onAPan);
        assertTrue(onAPan * 3 >= high * 2, "most of its high notes are where a drummer has a pan: " + onAPan + " of " + high);
        assertTrue(lowOnAPot >= 12, "and its low notes are on the pots: " + lowOnAPot);
    }

    @Test
    void aBassPlayerIsBornWithOneNoteOnTheOne(@TempDir Path dir) throws Exception {
        Path a = practised(dir, "a.life", 4, LESSON.bar()), b = practised(dir, "b.life", 4, "p...n...p.p.n..."), bass = dir.resolve("bass.life");
        Session.Band band = Session.band(dir.resolve("t.wav"), List.of(a, b), bass, 8, 92, 4);
        assertEquals("p...............", band.bassBefore());
        assertEquals(3, band.parts().size());
        assertEquals("the bass player", band.parts().get(2).name());
        assertEquals(1, band.parts().get(2).ticksBefore());
        assertEquals(LESSON.bar(), band.parts().get(0).pattern(), "the drummers keep their own patterns");
        assertEquals("p...n...p.p.n...", band.parts().get(1).pattern());
        assertTrue(band.peak() > 0.2f && band.peak() < 1.0f, "loud enough to hear, never clipped: " + band.peak());
        List<String> life = Files.readAllLines(bass);
        assertTrue(life.stream().anyMatch(l -> l.startsWith("sounds: a low note (p)")));
        assertTrue(life.contains("notes: 1"), "it is born with one note");
        assertTrue(life.get(life.size() - 1).startsWith("session 1: played 8 bars with the drummer and the drummer, no record"), "both test drummers are named the drummer");
        assertTrue(Files.readAllLines(a).get(Files.readAllLines(a).size() - 1).contains("and the bass player, no record"), "the drummers remember who they played with");
    }

    /** Six newborn bass players, each living twelve sessions with two practised drummers and a click. */
    @Test
    void aBassPlayerGrowsOntoTheDrummersPots(@TempDir Path dir) throws Exception {
        int players = 6, sessions = 12;
        double[] notes = new double[sessions], onAPot = new double[sessions], apart = new double[sessions], cents = new double[sessions];
        int[] twoNotes = new int[sessions];
        int[] together = new int[sessions];
        for (int d = 0; d < players; d++) {
            Path bass = dir.resolve("bass" + d + ".life");
            for (int s = 0; s < sessions; s++) {
                Session.Band band = Session.band(dir.resolve("t.wav"), List.of(practised(dir, "a" + d + ".life", 4, LESSON.bar()),
                    practised(dir, "b" + d + ".life", 4, "p...n...p.p.n...")), bass, 16, 92, 1000L * d + s);
                String p = band.bassAfter();
                int reach = Musician.SLOTS / Musician.PULSES / band.parts().get(2).ticksAfter();
                assertEquals('p', p.charAt(0), "its note stays on the one");
                for (int i = 0; i < Musician.SLOTS; i++) {
                    if (band.bassNotesKnownAfter() < 2) assertNotEquals('n', p.charAt(i), "it cannot play a note it has not earned: " + p);
                    if (i % reach != 0) assertEquals('.', p.charAt(i), "it only plays places its ticks can reach: " + p);
                }
                notes[s] += band.bassNotes() / (double) players;
                onAPot[s] += band.bassNotesOnAPot() / (double) players;
                apart[s] += band.bassApartMs() / players;
                cents[s] += band.bassOutOfTuneCents() / players;
                if (band.bassNotesKnownAfter() == 2) twoNotes[s]++;
                assertTrue(band.bassNotesKnownAfter() >= band.bassNotesKnownBefore(), "a note once earned is kept");
                if (Math.abs(band.landedApartMs()) < 40) together[s]++;
                assertEquals(92, band.tempoLastBars(), 2.5, "the click holds the tempo with three players too");
            }
        }
        for (int s = 0; s < sessions; s += 2)
            System.out.printf(Locale.ROOT, "BASS session %2d: %.0f cents out of tune, %d of %d have two notes, %.1f notes in the bar, %.1f low ones on a pot, %.1f ms from the drummer, ended together %d%n",
                s + 1, cents[s], twoNotes[s], players, notes[s], onAPot[s], apart[s], together[s]);
        double early = (onAPot[0] + onAPot[1] + onAPot[2]) / 3, late = (onAPot[sessions - 3] + onAPot[sessions - 2] + onAPot[sessions - 1]) / 3;
        double farEarly = (apart[0] + apart[1] + apart[2]) / 3, farLate = (apart[sessions - 3] + apart[sessions - 2] + apart[sessions - 1]) / 3;
        assertTrue(late > early + 0.3, "more of its notes sit on a pot: " + early + " at first, " + late + " later");
        assertTrue(farLate < farEarly - 4, "and they land closer to the drummer: " + farEarly + " ms at first, " + farLate + " ms later");
        assertTrue(cents[sessions - 1] < cents[0] - 5, "and it plays more in tune: " + cents[0] + " cents out at first, " + cents[sessions - 1] + " later");
        assertTrue(twoNotes[0] <= players / 3, "few have a second note after one session");
        assertTrue(twoNotes[sessions - 1] >= players - 1, "nearly all have earned it after twelve");
    }

    @Test
    void aSessionOfTeachingIsWrittenIntoBothLives(@TempDir Path dir) throws Exception {
        Path old = practised(dir, "old.life", 4, LESSON.bar()), young = dir.resolve("young.life");
        Session.Taught t = Session.teach(dir.resolve("t.wav"), old, young, 3, 4, 9);
        assertEquals(LESSON.bar(), t.teacherPattern(), "the teacher plays its own pattern and does not change it");
        assertEquals(LESSON.bar(), Life.open(old, "x").pattern());
        assertEquals(1, t.ticksBefore(), "the learner is born here");
        assertTrue(t.peak() > 0.2f && t.peak() < 1.0f, "loud enough to hear, never clipped: " + t.peak());
        List<String> o = Files.readAllLines(old), y = Files.readAllLines(young);
        assertTrue(o.get(o.size() - 1).contains("played its pattern for the second drummer, 3 calls and 4 bars together"));
        assertTrue(y.get(y.size() - 1).startsWith("session 1: heard \"the drummer\", answers=3"));
        assertEquals(t.learnerHits(), Life.open(young, "x").hitsPlayed());
    }

    /** Sixteen newborns, each taught for sixteen sessions by a practised drummer who never changes its pattern. */
    @Test
    void aYoungDrummerGrowsTowardTheOneWhoTeachesIt(@TempDir Path dir) throws Exception {
        int pairs = 16, sessions = 16;
        double[] like = new double[sessions], believed = new double[sessions];
        int[] atFour = new int[sessions];
        for (int d = 0; d < pairs; d++) {
            Path young = dir.resolve("y" + d + ".life");
            for (int s = 0; s < sessions; s++) {
                Session.Taught t = Session.teach(dir.resolve("t.wav"), practised(dir, "o" + d + ".life", 4, LESSON.bar()), young, 4, 8, 1000L * d + s);
                like[s] += t.matchesAfter() / (double) pairs;
                believed[s] += LESSON.matches(t.heardAs()) / (double) pairs;
                if (t.ticksAfter() == 4) atFour[s]++;
                int reach = Musician.SLOTS / Musician.PULSES / t.ticksAfter();
                for (int i = 0; i < Musician.SLOTS; i++) {
                    if (i % reach != 0) assertEquals('.', t.patternAfter().charAt(i), "it only plays places its ticks can reach");
                    if (i % reach != 0) assertEquals('.', t.heardAs().charAt(i), "and only hears those places too");
                }
            }
        }
        for (int s = 0; s < sessions; s += 3)
            System.out.printf(Locale.ROOT, "TAUGHT session %2d: like the teacher in %.1f of 16 places, believes the lesson right in %.1f, at four ticks %d of %d%n",
                s + 1, like[s], believed[s], atFour[s], pairs);
        double early = (like[0] + like[1] + like[2]) / 3, late = (like[sessions - 3] + like[sessions - 2] + like[sessions - 1]) / 3;
        assertTrue(late > early + 0.5, "it ends more like its teacher than it began: " + early + " to " + late);
    }

    @Test
    void twoPractisedDrummersHoldTimeTogetherWithNoRecord(@TempDir Path dir) throws Exception {
        int close = 0, landed = 0; double slowest = 9;
        for (int seed = 0; seed < 10; seed++) {
            Path a = practised(dir, "a" + seed + ".life", 4, LESSON.bar()), b = practised(dir, "b" + seed + ".life", 4, "p...n...p.p.n...");
            Session.Duo d = Session.together(dir.resolve("t.wav"), a, b, 16, seed);
            assertTrue(d.peak() > 0.2f && d.peak() < 1.0f, "loud enough to hear, never clipped: " + d.peak());
            assertEquals(LESSON.bar(), d.first().pattern(), "each keeps its own pattern");
            assertEquals("p...n...p.p.n...", d.second().pattern());
            if (d.hitsTogether() > 60 && d.apartMs() < 15) close++;
            if (Math.abs(d.landedApartMs()) < 40) landed++;
            slowest = Math.min(slowest, d.tempoLastBars() / d.tempoFirstBars());
        }
        assertTrue(close >= 8, "within 15 ms of each other in " + close + " of 10 takes");
        assertTrue(landed >= 8, "their last hits within 40 ms of each other in " + landed + " of 10 takes");
        assertTrue(slowest > 0.88, "the tempo they share does not collapse: " + slowest);
    }

    @Test
    void aSessionTogetherIsWrittenIntoBothLives(@TempDir Path dir) throws Exception {
        Path a = practised(dir, "a.life", 4, LESSON.bar()), b = dir.resolve("b.life");
        Session.Duo d = Session.together(dir.resolve("t.wav"), a, b, 8, 5);
        assertEquals("the second drummer", d.second().name());
        assertEquals(1, d.second().ticksBefore(), "the second drummer is born here, with one tick and the pot on every pulse");
        List<String> first = Files.readAllLines(a), second = Files.readAllLines(b);
        assertTrue(first.get(first.size() - 1).contains("played 8 bars with the second drummer, no record"));
        assertTrue(second.get(second.size() - 1).startsWith("session 1: played 8 bars with the drummer, no record"));
        assertEquals(d.second().hits(), Life.open(b, "x").hitsPlayed());
        assertEquals(3000 + d.first().hits(), Life.open(a, "x").hitsPlayed());
    }

    @Test
    void theSameTwoLivesAndTheSameChanceGiveTheSameTakeTogether(@TempDir Path dir) throws Exception {
        for (String take : new String[] {"x", "y"})
            Session.together(dir.resolve(take + ".wav"), practised(dir, take + "a.life", 4, LESSON.bar()), practised(dir, take + "b.life", 2, "p...n...p.p.n..."), 6, 77);
        assertArrayEquals(Files.readAllBytes(dir.resolve("x.wav")), Files.readAllBytes(dir.resolve("y.wav")));
    }

    @Test
    void aPractisedDrummerStaysWithTheRecord(@TempDir Path dir) throws Exception {
        int close = 0, landed = 0;
        for (int seed = 0; seed < 10; seed++) {
            Session.Report r = Session.run(dir.resolve("t.wav"), practised(dir, seed + ".life", 4, LESSON.bar()), LESSON, 2, 8, seed);
            assertEquals((2 * Musician.TURN + 8) * Musician.SLOTS, r.lastHitPlace(), "it counts its bars and lands the one after them");
            if (r.hitsTogether() > 40 && r.apartMs() < 25) close++;
            if (Math.abs(r.landedApartMs()) < 40) landed++;
        }
        assertTrue(close >= 8, "most of its hits within 25 ms of the record's in " + close + " of 10 takes");
        assertTrue(landed >= 7, "their last hits within 40 ms of each other in " + landed + " of 10 takes");
    }

    @Test
    void itHearsOnlyWhatItsTicksCanReach(@TempDir Path dir) throws Exception {
        String[] whole = {null, "p...n...p...n...", "p...n...p.p.n...", null, "p..pn...p.p.n..n"};
        for (int ticks : new int[] {1, 2, 4}) {
            int right = 0;
            for (int seed = 0; seed < 10; seed++) {
                Session.Report r = Session.run(dir.resolve("t.wav"), practised(dir, ticks + "-" + seed + ".life", ticks), LESSON, 4, 0, seed);
                for (int i = 0; i < 16; i++)
                    if (i % (4 / r.ticksAfter()) != 0) assertEquals('.', r.lessonAsHeard().charAt(i), "a hit between its ticks is not heard as anything");
                if (r.lessonAsHeard().equals(whole[ticks]) || r.lessonAsHeard().equals(whole[r.ticksAfter()])) right++;   // it may earn a finer tick mid-session
            }
            assertTrue(right >= 6, "with " + ticks + " ticks to the pulse it should hear " + whole[ticks] + "; it did in " + right + " of 10 takes");
        }
    }

    @Test
    void itLandsOnTheOneAndRingsForOnePulseOfItsOwn(@TempDir Path dir) throws Exception {
        double shortest = 9, longest = 0;
        for (int seed = 0; seed < 12; seed++) {
            Session.Report r = Session.run(dir.resolve("t.wav"), dir.resolve(seed + ".life"), LESSON, 2, 0, seed);
            assertEquals(2 * Musician.TURN * Musician.SLOTS, r.lastHitPlace(), "the last hit is the one after its last answer");
            assertEquals(r.lastPulseSeconds(), r.ringSeconds(), 2.0 * Audio.BLOCK / Audio.RATE, "it rings for one pulse");
            shortest = Math.min(shortest, r.ringSeconds());
            longest = Math.max(longest, r.ringSeconds());
        }
        assertTrue(longest - shortest > 0.03, "the ring follows its tempo; it is not one fixed length");
    }

    @Test
    void theSameLifeAndTheSameChanceGiveTheSameTake(@TempDir Path dir) throws Exception {
        Session.run(dir.resolve("a.wav"), dir.resolve("a.life"), LESSON, 2, 0, 99);
        Session.run(dir.resolve("b.wav"), dir.resolve("b.life"), LESSON, 2, 0, 99);
        assertArrayEquals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("b.wav")));
        Session.run(dir.resolve("c.wav"), dir.resolve("c.life"), LESSON, 2, 0, 100);
        assertFalse(java.util.Arrays.equals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("c.wav"))));
    }

    @Test
    void whatItLearnedIsWrittenIntoItsLifeAndIsWhereTheNextSessionStarts(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("d.life");
        Session.Report first = Session.run(dir.resolve("1.wav"), file, LESSON, 3, 0, 40);
        Session.Report second = Session.run(dir.resolve("2.wav"), file, LESSON, 3, 0, 41);
        assertEquals(first.patternAfter(), second.patternBefore());
        assertEquals(first.ticksAfter(), second.ticksBefore());
        assertEquals(first.listeningAfter(), second.listeningBefore(), 0.006);
        List<String> lines = Files.readAllLines(file);
        assertEquals(2, lines.stream().filter(l -> l.startsWith("session ")).count());
        assertTrue(lines.get(lines.size() - 1).contains("heard \"first lesson\""));
        assertEquals(first.hits() + second.hits(), Life.open(file, "x").hitsPlayed());
    }

    @Test
    void livesFromEarlierStepsStillRead(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.life");
        Files.write(file, List.of("name: the drummer", "age: 0", "listens to itself: 0.75", "sounds: a pot (p) and a pan (n)",
            "ticks to the pulse: 4", "pattern: p...nnn.p.p.n.n.", "",
            "session 1: pulses=64, tempo 92.0 to 108.6 bpm, unsteadiness 15.1 ms at the start, 10.3 ms by the end, listening 0.27",
            "session 2: bars=16, hits=65, tempo 92.0 to 95.7 bpm, unsteadiness 20.7 ms at the start, 16.8 ms by the end, listening 0.39, ticks to the pulse 1, pattern p...p...n...p..."));
        Life life = Life.open(file, "x");
        assertEquals(64 + 65, life.hitsPlayed());
        assertEquals(4, life.ticks());
        assertEquals("p...nnn.p.p.n.n.", life.pattern());
        Session.run(dir.resolve("t.wav"), file, LESSON, 2, 0, 1);
        List<String> lines = Files.readAllLines(file);
        assertTrue(lines.contains("session 1: pulses=64, tempo 92.0 to 108.6 bpm, unsteadiness 15.1 ms at the start, 10.3 ms by the end, listening 0.27"), "what it lived stays as it was written");
        assertTrue(lines.get(lines.size() - 1).startsWith("session 3: heard"));
    }

    /** The claims that matter, over forty drummers living twenty-four sessions each: four answers, then eight bars together. */
    @Test
    void itGrowsIntoTheLesson(@TempDir Path dir) throws Exception {
        int drummers = 40, sessions = 24;
        double[] early = new double[sessions], listen = new double[sessions], like = new double[sessions], hits = new double[sessions], apart = new double[sessions];
        int[] landed = new int[sessions];
        int[] atTwo = new int[sessions], atFour = new int[sessions];
        for (int d = 0; d < drummers; d++) {
            Path life = dir.resolve("d" + d + ".life"), wav = dir.resolve("d" + d + ".wav");
            int had = 1;
            for (int s = 0; s < sessions; s++) {
                Session.Report r = Session.run(wav, life, LESSON, 4, 8, 1000L * d + s);
                early[s] += r.earlyUnsteadyMs() / drummers;
                listen[s] += r.listeningAfter() / drummers;
                like[s] += r.matchesAfter() / (double) drummers;
                hits[s] += r.hits() / (double) drummers;
                apart[s] += (r.hitsTogether() > 0 ? r.apartMs() : 80) / drummers;
                if (Math.abs(r.landedApartMs()) < 40) landed[s]++;
                if (r.ticksAfter() >= 2) atTwo[s]++;
                if (r.ticksAfter() == 4) atFour[s]++;

                assertTrue(r.ticksAfter() >= had && r.ticksAfter() <= 4 && r.ticksAfter() != 3, "ticks only ever get finer");
                had = r.ticksAfter();
                String p = r.patternAfter();
                int reach = Musician.SLOTS / Musician.PULSES / had;
                assertEquals('p', p.charAt(0), "the one stays on the pot");
                for (int i = 0; i < Musician.SLOTS; i++) {
                    if (i % reach != 0) assertEquals('.', p.charAt(i), "it only plays places its ticks can reach: " + p + " with " + had);
                    if (i % reach != 0) assertEquals('.', r.lessonAsHeard().charAt(i), "and only hears those places too");
                    if (i % 4 == 0) assertNotEquals('.', p.charAt(i), "a pulse is never left empty: " + p);
                }
                assertTrue(r.peak() > 0.2f && r.peak() < 1.0f, "loud enough to hear, never clipped: " + r.peak());
                assertTrue(r.endedBpm() > 55 && r.endedBpm() < 150, "its tempo does not run away: " + r.endedBpm());
            }
        }
        for (int s = 0; s < sessions; s++)
            System.out.printf(Locale.ROOT, "STUDY session %2d: together %.1f ms apart, last hits together in %d of %d, like the lesson in %.1f of 16, two ticks %d, four %d, unsteadiness %.1f ms, listening %.2f%n",
                s + 1, apart[s], landed[s], drummers, like[s], atTwo[s], atFour[s], early[s], listen[s]);
        assertEquals(0, atFour[0], "nobody is born playing sixteenths");
        assertTrue(atTwo[0] < drummers / 2, "most do not even find the half in their first session");
        assertTrue(atFour[sessions - 1] > drummers * 0.5, "with practice most of them get there");
        assertTrue(like[sessions - 1] > like[0] + 2, "and their pattern has grown toward the lesson");
        assertTrue(early[sessions - 1] < early[0] - 6, "and their time is far steadier");
        assertTrue(listen[sessions - 1] > listen[0] + 0.15, "because they learned to listen");
        assertTrue(apart[sessions - 1] < apart[0] - 6, "and they land closer to the record when they play with it");
        assertTrue(landed[sessions - 1] > landed[0], "and more of them end together with it");
    }

    private static Path bassPlayer(Path dir, String name, int ticks, int notes) throws Exception {
        Path file = dir.resolve(name);
        Files.write(file, List.of("name: the bass player", "age: 0", "listens to itself: 0.60", "ticks to the pulse: " + ticks, "notes: " + notes,
            "pattern: p...p...p...p...", "", "session 1: played 400 bars, hits=2000, made up for a test"));
        return file;
    }

    @Test
    void aPlayerKeepsWhatTheBandleaderAskedForEvenThoughTheSongDoesNotHaveIt(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        Path drummer = practised(dir, "d.life", 4), bass = bassPlayer(dir, "b.life", 2, 2);
        String ask = "......n.......n.";
        Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN).ask(ask, "the end of two and four, on an up tick");
        for (int s = 0; s < 4; s++) {
            Session.Along take = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "made up", song, heard, bar, 5, 24, 40 + s);
            Session.AlongPart b = take.parts().get(1);
            assertEquals('n', b.patternAfter().charAt(6), "take " + s + ": " + b.patternAfter());
            assertEquals('n', b.patternAfter().charAt(14), "take " + s + ": " + b.patternAfter());
            assertEquals('p', b.patternAfter().charAt(0), "the one is still the one");
            assertEquals(2, b.askedAndPlaying());
            assertEquals(0, b.askedAndWaiting());
            assertEquals(0, take.parts().get(0).askedAndPlaying(), "nobody asked the drummer for anything");
        }
        List<String> life = Files.readAllLines(bass);
        assertTrue(life.contains("asked for: " + ask), "it is written at the top of its life");
        assertTrue(life.stream().anyMatch(l -> l.startsWith("asked by the bandleader: " + ask) && l.contains("the end of two and four")), "with the bandleader's words");
        assertEquals(ask, Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN).asked(), "and it is still asked when its life is opened again");
    }

    @Test
    void whatAPlayerCannotReachYetWaits(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        Path drummer = practised(dir, "d.life", 4);
        Path oneNote = bassPlayer(dir, "one.life", 2, 1), twoTicks = bassPlayer(dir, "two.life", 2, 2);
        Life.open(oneNote, "the bass player", Musician.FIRST_BASS_PATTERN).ask("......n.......n.", "");
        Life.open(twoTicks, "the bass player", Musician.FIRST_BASS_PATTERN).ask("n......n......n.", "");   // the one, a sixteenth it cannot reach, and one it can
        Session.AlongPart a = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), oneNote, "made up", song, heard, bar, 5, 24, 7).parts().get(1);
        assertEquals(0, a.askedAndPlaying(), "it has one note, so it cannot play the high one: " + a.patternAfter());
        assertEquals(2, a.askedAndWaiting());
        Session.AlongPart b = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), twoTicks, "made up", song, heard, bar, 5, 24, 7).parts().get(1);
        assertEquals(1, b.askedAndPlaying(), b.patternAfter());
        assertEquals(2, b.askedAndWaiting(), "the one is not up for asking and two ticks do not reach a sixteenth");
        assertEquals('p', b.patternAfter().charAt(0));
        assertEquals('.', b.patternAfter().charAt(7));
        assertEquals('n', b.patternAfter().charAt(14));
    }

    @Test
    void anAskIsSixteenPlacesAndNothingElse(@TempDir Path dir) throws Exception {
        Path bass = bassPlayer(dir, "b.life", 2, 2);
        List<String> before = Files.readAllLines(bass);
        Life life = Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN);
        assertEquals("................", life.asked(), "nobody has asked it for anything yet");
        assertThrows(IllegalArgumentException.class, () -> life.ask("......n.", ""));
        assertThrows(IllegalArgumentException.class, () -> life.ask("......x.......n.", ""));
        assertEquals(before, Files.readAllLines(bass), "a bad ask leaves its life alone");
    }

    /** A made-up song with a bass line: the record's drums, a quiet hat, and a four-bar line of eighth notes under them. */
    private static float[] songWithALine(Lesson drums, int bars, int[] line) {
        Teacher record = new Teacher(drums, 0, bars, 0.25);
        float[] mix = new float[(int) (Audio.RATE * (bars * 4 * 60.0 / drums.tempo() + 3))], block = new float[Audio.BLOCK];
        for (int now = 0; now + Audio.BLOCK <= mix.length; now += Audio.BLOCK) {
            java.util.Arrays.fill(block, 0f);
            record.play(block, Audio.BLOCK, now);
            System.arraycopy(block, 0, mix, now, Audio.BLOCK);
        }
        java.util.SplittableRandom chance = new java.util.SplittableRandom(8);
        double eighth = 60.0 / drums.tempo() / 2;
        for (int e = 0; e < bars * 8; e++) {
            int at = (int) Audio.samples(0.25 + e * eighth);
            for (int i = 0; i < 900; i++) mix[at + i] += (float) (0.05 * (chance.nextDouble() * 2 - 1) * Math.exp(-i / 200.0));
            double hz = LineEar.hzOf(line[e % line.length]);
            for (int i = 0; i < 13000; i++) {
                double t = i / (double) Audio.RATE, fade = Math.min(1, i / 300.0) * Math.min(1, (13000 - i) / 600.0);
                mix[at + i] += (float) (0.22 * fade * Math.exp(-t / 0.4) * (Math.sin(2 * Math.PI * hz * t) + 0.6 * Math.sin(4 * Math.PI * hz * t) + 0.3 * Math.sin(6 * Math.PI * hz * t)));
            }
        }
        return mix;
    }

    static final int[] LINE = {33, 33, 33, 33, 33, 33, 33, 33,  36, 36, 36, 36, 36, 36, 36, 36,  38, 38, 38, 38, 40, 40, 40, 40,  31, 31, 31, 31, 31, 31, 35, 35};

    @Test
    void aBassPlayerLearnsTheNotesOfASongsBassLine(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = songWithALine(drums, 40, LINE);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        java.util.Set<String> inTheLine = new java.util.HashSet<>(List.of("A1", "C2", "D2", "E2", "G1", "B1", "."));
        Path drummer = practised(dir, "d.life", 4), bass = bassPlayer(dir, "b.life", 2, 2), oneNote = dir.resolve("newborn.life");
        Session.AlongPart first = null, last = null;
        for (int s = 0; s < 3; s++) {
            last = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "made up", song, heard, bar, 5, 24, 60 + s).parts().get(1);
            if (s == 0) first = last;
        }
        System.out.printf(Locale.ROOT, "LINE first take %d on the song's note, %d not; third take %d and %d. It believes %s (%d of %d places), out of tune by %.0f cents%n",
            first.withTheSong(), first.againstTheSong(), last.withTheSong(), last.againstTheSong(), last.line(), last.linePlaces(), last.linePlacesInReach(), last.outOfTuneCents());
        assertTrue(last.linePlacesInReach() == 32 || last.linePlacesInReach() == 64, "eight places in each of four bars, or sixteen once it has earned four ticks");
        assertEquals(4, last.phraseBars(), "it found by ear that the line comes round every four bars: " + last.phraseHeard());
        assertTrue(last.linePlaces() >= 24, "it believes it knows the note at most places: " + last.line());
        for (String note : last.line().replace("|", " ").trim().split("\\s+")) assertTrue(inTheLine.contains(note), note + " is not in the song's line: " + last.line());
        assertTrue(last.withTheSong() >= 40, "it played plenty of notes while it could hear the song's note: " + last.withTheSong());
        assertTrue(last.withTheSong() >= 9 * last.againstTheSong(), "and nine in ten were the song's note: " + last.withTheSong() + " to " + last.againstTheSong());
        assertTrue(last.outOfTuneCents() < 15, "and it still plays in tune, by its own ear, wherever its hand has gone: " + last.outOfTuneCents());
        assertTrue(Files.readAllLines(bass).stream().anyMatch(l -> l.startsWith("line from \"made up\": ")), "the line is written into its life");

        Session.AlongPart again = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "made up", song, heard, bar, 5 + 2 * 4 * 60.0 / 92, 24, 99).parts().get(1);
        assertTrue(again.withTheSong() >= 9 * again.againstTheSong(), "starting two bars further into the song it still knows where it is in the phrase: " + again.withTheSong() + " to " + again.againstTheSong());

        Session.AlongPart young = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), oneNote, "made up", song, heard, bar, 5, 24, 61).parts().get(1);
        assertEquals(0, young.linePlaces(), "a newborn bass player has not earned its second note and is not listening for a line yet");
        assertEquals(1, young.notes());
        assertEquals(0, young.withTheSong() + young.againstTheSong());
        assertTrue(Files.readAllLines(oneNote).stream().noneMatch(l -> l.startsWith("line")));
    }

    @Test
    void theEarOnTheLineNamesANoteUnderADrum() {
        LineEar ear = new LineEar();
        float[] block = new float[Audio.BLOCK];
        Kit kick = new Kit();
        int right = 0, clear = 0;
        for (int note = LineEar.LOWEST + 2; note <= LineEar.HIGHEST - 2; note++) {
            double hz = LineEar.hzOf(note);
            long start = (long) (note - LineEar.LOWEST) * Audio.RATE, tick = start + 4096;
            kick.strike(Kit.POT, 1f);
            ear.noteAt(tick);
            for (long now = start; !ear.ready(now); now += Audio.BLOCK) {
                for (int i = 0; i < Audio.BLOCK; i++) {
                    double t = (now + i - tick) / (double) Audio.RATE;
                    block[i] = (now + i >= tick ? 0.5f * kick.next() : 0f) + (float) (t < 0 ? 0 : 0.2 * Math.exp(-t / 0.4) * (Math.sin(2 * Math.PI * hz * t) + 0.6 * Math.sin(4 * Math.PI * hz * t)));
                }
                ear.listen(block, Audio.BLOCK, now);
            }
            if (ear.hear()) clear++;
            if (Math.round(ear.midi()) == note) right++;
        }
        assertTrue(right >= 19, "it named " + right + " of 21 notes, " + clear + " of them clearly");
    }

    @Test
    void aBassPlayerFindsHowLongThePhraseIsByEar(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        int[] twoBars = {33, 33, 33, 33, 36, 36, 36, 36,  38, 38, 38, 38, 31, 31, 35, 35};
        int[] oneBar = {33, 33, 33, 36, 36, 38, 31, 31};
        int[] eightBars = new int[64];
        for (int e = 0; e < 64; e++) eightBars[e] = new int[] {33, 36, 38, 40, 31, 35, 28, 29}[e / 8] + (e % 8 >= 6 ? 2 : 0);
        int[][] lines = {oneBar, twoBars, LINE, eightBars};
        int[] bars = {1, 2, 4, 8};
        Path drummer = practised(dir, "d.life", 4);
        for (int k = 0; k < lines.length; k++) {
            float[] song = songWithALine(drums, 48, lines[k]);
            Hearing.Heard heard = Hearing.listenLowAndHigh(song);
            Hearing.Bar bar = Hearing.oneBar(heard);
            Path bass = bassPlayer(dir, "b" + k + ".life", 2, 2);
            Session.AlongPart take = null;
            for (int s = 0; s < 2; s++) take = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "made up", song, heard, bar, 5, 32, 70 + 10 * k + s).parts().get(1);
            System.out.printf(Locale.ROOT, "PHRASE a line of %d bars: it believes %d (came round after 1, 2, 4, 8 bars: %s); %d on the song's note, %d not%n", bars[k], take.phraseBars(), take.phraseHeard(), take.withTheSong(), take.againstTheSong());
            assertEquals(bars[k], take.phraseBars(), "a line of " + bars[k] + " bars: " + take.phraseHeard());
            assertTrue(take.withTheSong() >= 9 * take.againstTheSong(), "and it plays it: " + take.withTheSong() + " to " + take.againstTheSong());
            assertTrue(Files.readAllLines(bass).stream().anyMatch(l -> l.startsWith("phrase from \"made up\": ")), "what it heard about the phrase is in its life");
        }
        assertEquals(1, Musician.onBass("new", 92.0, 0, 0.6, 1, Musician.FIRST_BASS_PATTERN, 1, 4, 1).phraseBars(), "until it has heard enough to say, a phrase is one bar");
    }

    @Test
    void whatAPlayerTakesFromOneSongItKeepsApartFromAnother(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        int[] otherLine = {30, 30, 30, 30, 34, 34, 34, 34,  37, 37, 37, 37, 42, 42, 42, 42};      // two bars, and no note the first song has
        float[] songA = songWithALine(drums, 40, LINE), songB = songWithALine(drums, 40, otherLine);
        Hearing.Heard heardA = Hearing.listenLowAndHigh(songA), heardB = Hearing.listenLowAndHigh(songB);
        Hearing.Bar barA = Hearing.oneBar(heardA), barB = Hearing.oneBar(heardB);
        java.util.Set<String> inA = new java.util.HashSet<>(List.of("A1", "C2", "D2", "E2", "G1", "B1", ".")), inB = new java.util.HashSet<>(List.of("F#1", "Bb1", "C#2", "F#2", "."));
        Path drummer = practised(dir, "d.life", 4), bass = bassPlayer(dir, "b.life", 2, 2);
        Session.AlongPart a = null, b = null;
        for (int s = 0; s < 3; s++) a = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "song a", songA, heardA, barA, 5, 24, 80 + s).parts().get(1);
        for (int s = 0; s < 3; s++) {
            b = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "song b", songB, heardB, barB, 5, 24, 90 + s).parts().get(1);
            for (String note : b.line().replace("|", " ").trim().split("\\s+")) assertTrue(inB.contains(note), "take " + s + " of the second song: " + note + " came from somewhere else: " + b.line());
        }
        assertEquals(2, b.phraseBars(), "the second song's phrase is two bars, whatever the first one's was: " + b.phraseHeard());
        assertTrue(b.withTheSong() >= 9 * b.againstTheSong(), b.withTheSong() + " to " + b.againstTheSong());
        Session.AlongPart back = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), bass, "song a", songA, heardA, barA, 5, 24, 99).parts().get(1);
        System.out.printf(Locale.ROOT, "APART after three takes of one song, three of another, then back: %d on the first song's note, %d not, from its first bar; phrase %d bars%n", back.withTheSong(), back.againstTheSong(), back.phraseBars());
        assertEquals(4, back.phraseBars(), "back with the first song, it still knows its phrase is four bars");
        assertTrue(back.withTheSong() >= 9 * back.againstTheSong(), "and its line, from the first bar: " + back.withTheSong() + " to " + back.againstTheSong());
        for (String note : back.line().replace("|", " ").trim().split("\\s+")) assertTrue(inA.contains(note), note + " is not in the first song: " + back.line());
        Life life = Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN);
        assertEquals(java.util.Set.of("song a", "song b"), life.songs());
        assertEquals("", life.line("a song it never heard"));
    }

    @Test
    void aLineFromBeforeSongsWereNamedBelongsToTheLastSongItPlayedWith(@TempDir Path dir) throws Exception {
        Path bass = dir.resolve("old.life");
        Files.write(bass, List.of("name: the bass player", "age: 0", "listens to itself: 0.60", "ticks to the pulse: 2", "notes: 2", "pattern: p...p...p...p...",
            "line: 36/5/6 - 38/4/4", "phrase: 1/10 2/10 9/10 8/9", "",
            "session 1: played along with \"an old song\" for 24 bars, hits=100, unsteadiness 8.0 ms"));
        Life life = Life.open(bass, "the bass player", Musician.FIRST_BASS_PATTERN);
        assertEquals("36/5/6 - 38/4/4", life.line("an old song"));
        assertEquals("1/10 2/10 9/10 8/9", life.phrase("an old song"));
        assertEquals("", life.line("a new song"));
        assertEquals(java.util.Set.of("an old song"), life.songs());
    }

    @Test
    void aPlayerBorrowsFromOneRecordWhileItIsWithAnotherAndKeepsWhatItCanHold(@TempDir Path dir) throws Exception {
        String a = "p..pn.p...ppn..n", b = "p.p.n...p.p.n...";
        Lesson first = new Lesson("record a", 92, a), second = new Lesson("record b", 92, b);
        Path drummer = practised(dir, "d.life", 4);
        for (int s = 0; s < 3; s++) Session.run(dir.resolve("t.wav"), drummer, first, 6, 8, 300 + s);
        Life life = Life.open(drummer, "the drummer");
        assertEquals(a, life.heard().get("record a"), "what it believes the first record plays is in its life, under its name");
        assertEquals("................", life.own(), "with one record there is nothing to borrow");
        int sessions = 0;
        while (sessions < 16 && Life.open(drummer, "the drummer").own().matches("\\.*")) Session.run(dir.resolve("t.wav"), drummer, second, 6, 8, 400 + sessions++);
        life = Life.open(drummer, "the drummer");
        System.out.printf(Locale.ROOT, "BORROW after %d sessions with a second record: its own %s, it plays %s (first record %s, second %s)%n", sessions, life.own(), life.pattern(), a, b);
        assertFalse(life.own().matches("\\.*"), "within sixteen sessions with the second record it holds something borrowed from the first");
        assertEquals(b, life.heard().get("record b"));
        for (int more = 0; more < 4; more++) {
            Session.run(dir.resolve("t.wav"), drummer, second, 6, 8, 500 + more);
            life = Life.open(drummer, "the drummer");
            int held = 0;
            for (int p = 0; p < 16; p++) {
                if (life.own().charAt(p) == '.') continue;
                held++;
                assertEquals(a.charAt(p), life.own().charAt(p), "place " + (p + 1) + " came from the first record");
                assertEquals('.', b.charAt(p), "and the second record has nothing there");
                assertEquals(life.own().charAt(p), life.pattern().charAt(p), "and it still plays it, session after session, though the record beside it does not: " + life.pattern());
            }
            assertTrue(held >= 1 && held <= 2, "it holds one or two places as its own: " + life.own());
        }
    }

    @Test
    void aPlayerWithNoOtherRecordHasNothingToBorrow(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = madeUpSong(drums, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        Path drummer = practised(dir, "d.life", 4);
        Session.AlongPart part = null;
        for (int s = 0; s < 3; s++) part = Session.playAlong(dir.resolve("t.wav"), drummer, "made up", song, heard, bar, 5, 24, 600 + s).parts().get(0);
        assertEquals(0, part.borrowed());
        assertEquals("................", part.own());
        assertEquals(java.util.Set.of("made up"), Life.open(drummer, "the drummer").heard().keySet(), "but what it heard in the song is kept under the song's name");
    }

    @Test
    void aPlayerTakesOnARecordsSwingUpToTheTriplet(@TempDir Path dir) throws Exception {
        String bar = "p.nnp.nnp.nnp.nn";
        double[] late = new double[16], hard = new double[16];
        for (int i = 0; i < 16; i++) { late[i] = i % 2 == 1 ? 46 : 0; hard[i] = i == 0 ? 1.0 : 0.7; }
        Lesson swung = new Lesson("swung", 92, bar, late, hard), straight = new Lesson("straight", 92, bar, new double[16], hard);
        assertEquals(64.1, Hearing.swing(bar, late, 92), 0.1, "46 ms late on a sixteenth of 163 ms is 64 on a drum machine's dial");
        assertEquals(50.0, Hearing.swing(bar, new double[16], 92), 1e-9);
        assertEquals(-1, Hearing.swing("p.n.p.n.p.n.p.n.", late, 92), 1e-9, "nothing off the eighths, no swing to speak of");
        assertThrows(IllegalArgumentException.class, () -> { double[] tooFar = late.clone(); tooFar[1] = 70; new Lesson("past the triplet", 92, bar, tooFar, hard); });

        Path a = practised(dir, "a.life", 4, bar), b = practised(dir, "b.life", 4, bar);
        Session.Report withSwing = null, without = null;
        for (int s = 0; s < 4; s++) {
            withSwing = Session.run(dir.resolve("t.wav"), a, swung, 6, 8, true, 700 + s);        // with a click: without one a swung record pulls a player's time apart
            without = Session.run(dir.resolve("t.wav"), b, straight, 6, 8, true, 700 + s);
        }
        double swings = Hearing.swing(withSwing.patternAfter(), withSwing.feelMs(), 92), doesNot = Hearing.swing(without.patternAfter(), without.feelMs(), 92);
        Teacher plays = new Teacher(swung, 0, 24, 0.25);     // the record on its own, with nothing straight laid over it
        float[] record = new float[(int) (Audio.RATE * (24 * 4 * 60.0 / 92 + 2))], block = new float[Audio.BLOCK];
        for (int now = 0; now + Audio.BLOCK <= record.length; now += Audio.BLOCK) {
            java.util.Arrays.fill(block, 0f);
            plays.play(block, Audio.BLOCK, now);
            System.arraycopy(block, 0, record, now, Audio.BLOCK);
        }
        Hearing.Bar heardBar = Hearing.oneBar(Hearing.listenLowAndHigh(record));
        double heard = heardBar.swing();
        System.out.printf(Locale.ROOT, "SWING the record swings 64.1; the ear on the recording says %.1f; the player with it plays %.1f (unsteadiness %.1f ms); the player with the straight record plays %.1f%n", heard, swings, withSwing.lateUnsteadyMs(), doesNot);
        assertEquals(64.1, swings, 3.0, "after four sessions it swings as the record does");
        assertTrue(withSwing.lateUnsteadyMs() < 12, "and holds its time while it does: " + withSwing.lateUnsteadyMs());
        assertEquals(50.0, doesNot, 2.0, "and the one with the straight record does not");
        assertEquals(64.1, heard, 4.0, "the ear names the swing of a recording");
    }

    @Test
    void aPlayerHoldsItsTimeWithASwungRecordAndNoClick(@TempDir Path dir) throws Exception {
        String bar = "p.nnp.nnp.nnp.nn";
        double[] late = new double[16], hard = new double[16];
        for (int i = 0; i < 16; i++) { late[i] = i % 2 == 1 ? 46 : 0; hard[i] = i == 0 ? 1.0 : 0.7; }
        Lesson swung = new Lesson("swung", 92, bar, late, hard);
        Path drummer = practised(dir, "d.life", 4, bar);
        double worst = 0, slowest = 999, fastest = 0, swing = 0, first = 0;
        for (int s = 0; s < 10; s++) {
            Session.Report r = Session.run(dir.resolve("t.wav"), drummer, swung, 6, 8, 700 + s);
            if (s < 4) { first = Math.max(first, r.lateUnsteadyMs()); continue; }   // the first sessions are where it finds out the record swings, and they are rough
            worst = Math.max(worst, r.lateUnsteadyMs()); slowest = Math.min(slowest, r.endedBpm()); fastest = Math.max(fastest, r.endedBpm());
            swing = Hearing.swing(r.patternAfter(), r.feelMs(), 92);
        }
        System.out.printf(Locale.ROOT, "SWING, NO CLICK first four sessions unsteady by up to %.1f ms; sessions five to ten: at worst %.1f ms, tempo ended between %.1f and %.1f, swing %.1f%n", first, worst, slowest, fastest, swing);
        assertTrue(worst < 15, "once it knows the record swings it holds its time: " + worst);
        assertTrue(slowest > 89.5 && fastest < 94, "and its tempo: " + slowest + " to " + fastest);
        assertEquals(64.1, swing, 3.0);
    }

    @Test
    void aNewbornCanGrowUpCountingInThrees(@TempDir Path dir) throws Exception {
        Lesson shuffle = Lesson.read(Path.of("lessons/shuffle.lesson"));
        assertEquals(12, shuffle.bar().length(), "a bar in threes has twelve places");
        assertThrows(IllegalArgumentException.class, () -> new Lesson("thirteen", 92, "p.nn.np.nn.n."));
        int drummers = 12, sessions = 30;
        double[] like = new double[sessions], apart = new double[sessions], unsteady = new double[sessions];
        int[] atThree = new int[sessions], landed = new int[sessions];
        for (int d = 0; d < drummers; d++) {
            Path life = dir.resolve("three" + d + ".life");
            int had = 1;
            for (int s = 0; s < sessions; s++) {
                Session.Report r = Session.run(dir.resolve("t.wav"), life, shuffle, 4, 8, 2000L * d + s);
                assertEquals(12, r.patternAfter().length(), "it counts in threes from its first record on");
                assertTrue(r.ticksAfter() == 1 || r.ticksAfter() == 3, "one tick to the pulse or three, nothing between: " + r.ticksAfter());
                assertTrue(r.ticksAfter() >= had, "ticks only ever get finer");
                had = r.ticksAfter();
                for (int i = 0; i < 12; i++) {
                    if (i % (3 / had) != 0) assertEquals('.', r.patternAfter().charAt(i), "it only plays places its ticks can reach: " + r.patternAfter());
                    if (i % 3 == 0) assertNotEquals('.', r.patternAfter().charAt(i), "a pulse is never left empty: " + r.patternAfter());
                }
                assertTrue(r.endedBpm() > 55 && r.endedBpm() < 150, "its tempo does not run away: " + r.endedBpm());
                assertTrue(r.peak() > 0.2f && r.peak() < 1.0f);
                like[s] += r.matchesAfter() / (double) drummers;
                apart[s] += (r.hitsTogether() > 0 ? r.apartMs() : 80) / drummers;
                unsteady[s] += r.earlyUnsteadyMs() / drummers;
                if (had == 3) atThree[s]++;
                if (Math.abs(r.landedApartMs()) < 40) landed[s]++;
            }
        }
        for (int s : new int[] {0, 9, 19, 29})
            System.out.printf(Locale.ROOT, "THREES session %2d: like the shuffle in %.1f of 12, three ticks %d of %d, together %.1f ms apart, last hits together in %d, unsteadiness %.1f ms%n", s + 1, like[s], atThree[s], drummers, apart[s], landed[s], unsteady[s]);
        assertTrue(atThree[0] <= 2, "hardly anyone finds the triplet in a first session: " + atThree[0]);
        assertTrue(atThree[sessions - 1] > drummers / 2, "with practice most of them get there: " + atThree[sessions - 1]);
        assertTrue(like[sessions - 1] > like[0] + 2, "and their pattern has grown toward the shuffle: " + like[0] + " to " + like[sessions - 1]);
        assertTrue(apart[sessions - 1] < 25, "and they play it with the record: " + apart[sessions - 1]);

        Path fours = practised(dir, "fours.life", 4);
        assertThrows(IllegalArgumentException.class, () -> Session.run(dir.resolve("t.wav"), fours, shuffle, 4, 8, 1), "a player that counts in fours cannot sit with a record in threes yet");
        assertThrows(IllegalArgumentException.class, () -> Session.run(dir.resolve("t.wav"), dir.resolve("three0.life"), LESSON, 4, 8, 1), "nor one that counts in threes with a record in fours");
    }

    @Test
    void playersWhoCountInThreesTeachAndPlayWithEachOther(@TempDir Path dir) throws Exception {
        String shuffle = "p.nn.np.nn.n";
        int pairs = 6, sessions = 24;
        double likeFirst = 0, likeLast = 0, apartLast = 0;
        int threeTicks = 0;
        for (int k = 0; k < pairs; k++) {
            Path teacher = practised(dir, "teacher" + k + ".life", 3, shuffle), pupil = dir.resolve("pupil" + k + ".life");
            Session.Taught t = null;
            for (int s = 0; s < sessions; s++) {
                t = Session.teach(dir.resolve("t.wav"), teacher, pupil, 4, 8, 92, 3000L * k + s);
                assertEquals(12, t.patternAfter().length(), "a pupil counts as its teacher does");
                if (s == 0) likeFirst += t.matchesAfter() / (double) pairs;
            }
            likeLast += t.matchesAfter() / (double) pairs;
            if (t.ticksAfter() == 3) threeTicks++;
            Session.Duo duo = Session.together(dir.resolve("t.wav"), teacher, pupil, 8, 92, 77 + k);
            assertTrue(duo.hitsTogether() > 20, "they play together: " + duo.hitsTogether());
            apartLast += duo.apartMs() / pairs;
        }
        System.out.printf(Locale.ROOT, "THREES TAUGHT a pupil is like its teacher's shuffle in %.1f of 12 places after one session, %.1f after %d; %d of %d pupils have three ticks; together they are %.1f ms apart%n", likeFirst, likeLast, sessions, threeTicks, pairs, apartLast);
        assertTrue(likeLast >= 9.5 && likeLast > likeFirst + 1, "pupils grow toward their teacher's shuffle: " + likeFirst + " to " + likeLast);
        assertTrue(threeTicks >= pairs - 1, "and earn the triplet: " + threeTicks);
        assertTrue(apartLast < 25, "and play it with their teacher: " + apartLast);

        Path fours = practised(dir, "fours.life", 4);
        assertThrows(IllegalArgumentException.class, () -> Session.together(dir.resolve("t.wav"), fours, dir.resolve("pupil0.life"), 8, 92, 1), "one who counts in fours and one who counts in threes cannot play together yet");
        assertThrows(IllegalArgumentException.class, () -> Session.teach(dir.resolve("t.wav"), dir.resolve("teacher0.life"), fours, 4, 8, 92, 1));
    }

    @Test
    void aBassPlayerGrowsUpInThreesWithTwoShuffleDrummers(@TempDir Path dir) throws Exception {
        String shuffle = "p.nn.np.nn.n";
        int bands = 5, sessions = 40, threeTicks = 0, twoNotes = 0;
        double notesFirst = 0, notesLast = 0, apartLast = 0, tuneLast = 0, onDrums = 0;
        for (int k = 0; k < bands; k++) {
            Path a = practised(dir, "a" + k + ".life", 3, shuffle), b = practised(dir, "b" + k + ".life", 3, shuffle), bass = dir.resolve("bass" + k + ".life");
            Session.Band band = null;
            for (int s = 0; s < sessions; s++) {
                band = Session.band(dir.resolve("t.wav"), List.of(a, b), bass, 8, 92, 4000L * k + s);
                assertEquals(12, band.bassAfter().length(), "a newborn bass counts as the drummers do");
                assertEquals('p', band.bassAfter().charAt(0), "the one is a low note");
                assertTrue(band.peak() < 1.0f);
                if (s == 0) notesFirst += band.bassNotes() / (double) bands;
            }
            Session.Part part = band.parts().get(2);
            notesLast += band.bassNotes() / (double) bands;
            onDrums += (band.bassNotesOnAPot() + band.bassHighNotesOnAPan()) / (double) Math.max(1, band.bassNotes()) / bands;
            apartLast += band.bassApartMs() / bands;
            tuneLast += band.bassOutOfTuneCents() / bands;
            if (part.ticksAfter() == 3) threeTicks++;
            if (band.bassNotesKnownAfter() == 2) twoNotes++;
        }
        System.out.printf(Locale.ROOT, "THREES BASS %.1f notes in the bar after one session, %.1f after %d; %.0f%% of them where a drummer plays the same kind of hit; %d of %d have three ticks, %d have both notes; %.1f ms from the first drummer; out of tune by %.0f cents%n",
            notesFirst, notesLast, sessions, 100 * onDrums, threeTicks, bands, twoNotes, apartLast, tuneLast);
        assertTrue(notesLast > notesFirst, "its part grows: " + notesFirst + " to " + notesLast);
        assertTrue(threeTicks >= 3, "most of them earn the triplet: " + threeTicks);
        assertTrue(apartLast < 30, "and plays with the drummers: " + apartLast);

        Path fours = dir.resolve("fours-bass.life");
        Files.write(fours, List.of("name: the bass player", "ticks to the pulse: 2", "notes: 2", "pattern: p...p...p...p..."));
        Path a0 = dir.resolve("a0.life"), b0 = dir.resolve("b0.life");
        assertThrows(IllegalArgumentException.class, () -> Session.band(dir.resolve("t.wav"), List.of(a0, b0), fours, 8, 92, 1), "a bass player that counts in fours cannot join drummers who count in threes");
    }

    @Test
    void theEarHearsThreesInARecordingAndPlayersInThreesTakeDifferentPartsFromIt(@TempDir Path dir) throws Exception {
        float[] song = MadeUp.shuffle(92, 40);
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        assertEquals(12, Hearing.oneBar(heard, true).bar().length(), "heard for players who count in threes it is in threes, of course");
        assertEquals(12, bar.bar().length(), "the ear hears the made-up shuffle in threes without being told: " + bar.bar());
        assertEquals(92.0, bar.bpm(), 0.3, "at its own tempo, not at three quarters of it");
        assertEquals(-1, bar.swing(), 1e-9, "and swing is not a question in threes");
        Hearing.Bar straight = Hearing.oneBar(Hearing.listenLowAndHigh(madeUpSong(new Lesson("made up", 92, "p...n...p.p.n..."), 40)));
        assertEquals(16, straight.bar().length(), "a recording in fours is still heard in fours");
        assertEquals(16, Hearing.oneBar(Hearing.listenLowAndHigh(madeUpSong(new Lesson("made up", 92, "p...n...p.p.n..."), 40)), true).bar().length(), "and cannot be counted in threes even if asked");
        assertEquals(92.0, straight.bpm(), 0.3);

        String shuffle = "p.nn.np.nn.n";
        Path kit = practised(dir, "kit.life", 3, shuffle), hats = practised(dir, "hats.life", 3, shuffle), bass = bassPlayer(dir, "bass.life", 3, 2);
        Files.write(bass, List.of("name: the bass player", "listens to itself: 0.60", "ticks to the pulse: 3", "notes: 2", "pattern: " + "p..p..p..p..", "", "session 1: played 400 bars, hits=2000, made up for a test"));
        Session.Along take = null;
        for (int s = 0; s < 4; s++) take = Session.playAlong(dir.resolve("t.wav"), List.of(kit), hats, bass, "made-up shuffle", song, heard, bar, 3, 24, 900 + s);
        Session.AlongPart k = take.parts().get(0), h = take.parts().get(1), b = take.parts().get(2);
        System.out.printf(Locale.ROOT, "THREES ROLES kit hears %s and plays %s; hat hears %s and plays %s; bass hears %s and plays %s, %d on the song's note and %d not, phrase %d bars, line %s%n",
            k.believes(), k.patternAfter(), h.believes(), h.patternAfter(), b.believes(), b.patternAfter(), b.withTheSong(), b.againstTheSong(), b.phraseBars(), b.line());
        for (Session.AlongPart p : take.parts()) assertEquals(12, p.patternAfter().length());
        assertEquals("p..n..p..n..", k.believes(), "the drummer on the kit hears the kick and the snare");
        assertEquals(-1, h.patternAfter().indexOf('p'), "the hat player plays nothing but its hat");
        for (int place : new int[] {2, 5, 8, 11}) assertEquals('n', h.believes().charAt(place), "the hat player hears the hat on the last of every three: " + h.believes());
        assertEquals(-1, b.believes().indexOf('n'), "the bass player hears the bottom and none of the top: " + b.believes());
        assertTrue(b.withTheSong() >= 9 * b.againstTheSong() && b.withTheSong() >= 30, "and plays the song's notes: " + b.withTheSong() + " to " + b.againstTheSong());
        java.util.Set<String> written = new java.util.HashSet<>(List.of("."));
        for (int note : MadeUp.LINE) { written.add(Musician.nameOf(note)); written.add(Musician.nameOf(note - 2)); }
        for (String note : b.line().replace("|", " ").trim().split("\\s+")) assertTrue(written.contains(note), note + " is not a note of the made-up line (the count above is by its own ear, so this is the check): " + b.line());
        assertEquals(4, b.phraseBars(), "and it finds the line is four bars long: " + b.phraseHeard());
        assertNotEquals(k.patternAfter(), h.patternAfter(), "they do not all play the same thing");
        assertNotEquals(k.patternAfter(), b.patternAfter());

        Path fours = practised(dir, "fours.life", 4);
        assertThrows(IllegalArgumentException.class, () -> Session.playAlong(dir.resolve("t.wav"), fours, "made-up shuffle", song, heard, bar, 3, 8, 1), "a player that counts in fours cannot play along with a song in threes");
    }

    @Test
    void theClickIsSetFromTheStretchThatIsPlayedNotFromTheWholeSong() {
        String drums = "p...n...p.p.n...";
        float[] slow = madeUpSong(new Lesson("made up", 92, drums), 30), fast = madeUpSong(new Lesson("made up", 93, drums), 30);
        int keep = (int) (Audio.RATE * (0.25 + 30 * 4 * 60.0 / 92));        // the first thirty bars, and then the same thing a little faster
        float[] song = new float[keep + fast.length];
        System.arraycopy(slow, 0, song, 0, keep);
        System.arraycopy(fast, 0, song, keep, fast.length);
        Hearing.Bar whole = Hearing.oneBar(Hearing.listenLowAndHigh(song));
        Hearing.Bar early = Hearing.near(song, whole, 8, 16), late = Hearing.near(song, whole, Audio.seconds(keep) + 8, 16);
        System.out.printf(Locale.ROOT, "STRETCH a song that goes from 92 to 93: the whole of it %.2f, an early stretch %.2f, a late stretch %.2f%n", whole.bpm(), early.bpm(), late.bpm());
        assertEquals(92.0, early.bpm(), 0.2);
        assertEquals(93.0, late.bpm(), 0.2);
        assertEquals(whole.bar(), late.bar(), "what the bar is like is still the whole song's");
        Hearing.Bar steady = Hearing.oneBar(Hearing.listenLowAndHigh(slow));
        Hearing.Bar same = Hearing.near(slow, steady, 8, 16);
        assertEquals(steady.bpm(), same.bpm(), 0.1, "a song that holds its tempo is heard the same in any stretch");
        double bar = 4 * 60.0 / steady.bpm(), apart = Math.abs(same.aOneAtSeconds() - steady.aOneAtSeconds()) % bar;
        assertTrue(Math.min(apart, bar - apart) < 0.02, "and its one is in the same place: " + apart);
        assertSame(steady, Hearing.near(slow, steady, 8, 2), "too short a stretch to say: the whole song's pulse is kept");
    }

    @Test
    void aPlayerPicksUpWhereItLeftOffWithEachSong(@TempDir Path dir) throws Exception {
        float[] songA = madeUpSong(new Lesson("made up", 92, "p...n...p.p.n..."), 40), songB = madeUpSong(new Lesson("made up", 92, "p.p.n..p..p.n.n."), 40);
        Hearing.Heard heardA = Hearing.listenLowAndHigh(songA), heardB = Hearing.listenLowAndHigh(songB);
        Hearing.Bar barA = Hearing.oneBar(heardA), barB = Hearing.oneBar(heardB);
        Path drummer = practised(dir, "d.life", 4);
        Session.AlongPart a = null, b = null;
        for (int s = 0; s < 4; s++) a = Session.playAlong(dir.resolve("t.wav"), drummer, "song a", songA, heardA, barA, 5, 24, 1100 + s).parts().get(0);
        for (int s = 0; s < 4; s++) b = Session.playAlong(dir.resolve("t.wav"), drummer, "song b", songB, heardB, barB, 5, 24, 1200 + s).parts().get(0);
        assertNotEquals(a.patternAfter(), b.patternAfter(), "the two songs left it playing different things: " + a.patternAfter() + " and " + b.patternAfter());
        Life life = Life.open(drummer, "the drummer");
        assertEquals(b.patternAfter(), life.pattern(), "what it plays now is what it played last");
        assertEquals(a.patternAfter(), life.playsWith("song a"), "but what it played with the first song is still in its life");
        assertEquals(life.pattern(), life.playsWith("a song it has never met"));
        Session.AlongPart back = Session.playAlong(dir.resolve("t.wav"), drummer, "song a", songA, heardA, barA, 5, 24, 1300).parts().get(0);
        String own = Life.open(drummer, "the drummer").own();
        int differ = 0;
        for (int p = 0; p < 16; p++) if (back.patternBefore().charAt(p) != a.patternAfter().charAt(p)) { differ++; assertEquals(own.charAt(p), back.patternBefore().charAt(p), "it comes back to the first song with what it played there, and anything that has become its own since: " + back.patternBefore()); }
        System.out.printf(Locale.ROOT, "EACH SONG with the first %s, with the second %s; back to the first it begins with %s (%d places its own since)%n", a.patternAfter(), b.patternAfter(), back.patternBefore(), differ);
        assertTrue(differ <= 2);
    }

    @Test
    void aKeysPlayerIsBornAndLearnsNotesFromTheMiddleOfASong(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = songWithALine(drums, 40, LINE);
        int[] tune = {60, 60, 63, 65,   60, 60, 63, 67,   65, 65, 63, 60,   58, 58, 62, 67};     // a tune in the middle, one note to the pulse, four bars long
        double pulse = 60.0 / 92;
        for (int k = 0; k < 40 * 4; k++) {
            int at = (int) Audio.samples(0.25 + k * pulse);
            double hz = LineEar.hzOf(tune[k % tune.length]);
            for (int i = 0; i < 24000 && at + i < song.length; i++) {
                double t = i / (double) Audio.RATE, fade = Math.min(1, i / 200.0) * Math.min(1, (24000 - i) / 800.0);
                song[at + i] += (float) (0.25 * fade * Math.exp(-t / 0.5) * (Math.sin(2 * Math.PI * hz * t) + 0.5 * Math.sin(4 * Math.PI * hz * t) + 0.2 * Math.sin(6 * Math.PI * hz * t)));
            }
        }
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        java.util.Set<String> inTheTune = new java.util.HashSet<>(List.of("."));
        for (int note : tune) inTheTune.add(Musician.nameOf(note));
        Path drummer = practised(dir, "d.life", 4), bass = bassPlayer(dir, "b.life", 2, 2), keys = dir.resolve("keys.life");
        Session.AlongPart first = null, last = null;
        float peak = 0;
        for (int s = 0; s < 8; s++) {
            Session.Along take = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), null, bass, keys, "made up", song, heard, bar, 5, 24, 1400 + s);
            last = take.parts().get(2);
            if (s == 0) first = last;
            peak = Math.max(peak, take.peak());
            assertEquals("the bass player", take.parts().get(1).name());
        }
        int right = 0, believed = 0;
        for (String note : last.line().replace("|", " ").trim().split("\\s+")) if (!note.equals(".")) { believed++; if (inTheTune.contains(note)) right++; }
        System.out.printf(Locale.ROOT, "KEYS born with %s; after eight takes it plays %s, %d ticks to the pulse; it believes %d notes of the tune's four bars, %d of them notes the tune has; home note %s; %d on the song's note and %d not; unsteadiness %.1f ms; first take it left %d places unplayed%n",
            first.patternBefore(), last.patternAfter(), last.ticksAfter(), believed, right, last.home(), last.withTheSong(), last.againstTheSong(), last.unsteadyMs(), first.laidOut());
        assertEquals("the keys player", last.name());
        assertEquals("p...............", first.patternBefore(), "a newborn keys player has one note, on the one");
        assertTrue(first.laidOut() > 0, "until it has heard some of the song it presses nothing");
        assertTrue(last.plays().startsWith("a note on the keys"));
        assertTrue(believed >= 6, "it comes to believe notes in the middle of the song: " + last.line());
        assertTrue(right >= 0.8 * believed, "and they are the tune's notes, not the bass line's: " + last.line());
        assertTrue(inTheTune.contains(last.home()), "the note it falls back on is one of the tune's: " + last.home());
        assertTrue(last.withTheSong() > 2 * last.againstTheSong(), "most of what it plays is the note the tune is on: " + last.withTheSong() + " to " + last.againstTheSong());
        assertTrue(peak < 1.0f, "four players and a song without clipping: " + peak);
        List<String> life = Files.readAllLines(keys);
        assertTrue(life.stream().anyMatch(l -> l.startsWith("line from \"made up\": ")), "its line is in its life under the song's name");
        assertEquals(4, last.phraseBars(), "and it finds the tune is four bars long: " + last.phraseHeard());
    }

    @Test
    void aSingerIsBornLearnsATuneAndLearnsToSingItInTune(@TempDir Path dir) throws Exception {
        Lesson drums = new Lesson("made up", 92, "p...n...p.p.n...");
        float[] song = songWithALine(drums, 40, LINE);
        int[] tune = {67, 67, 70, 72,   67, 67, 70, 74,   72, 72, 70, 67,   65, 65, 69, 74};     // a tune above middle C, one note to the pulse, four bars long
        double pulse = 60.0 / 92;
        for (int k = 0; k < 40 * 4; k++) {
            int at = (int) Audio.samples(0.25 + k * pulse);
            double hz = LineEar.hzOf(tune[k % tune.length]);
            for (int i = 0; i < 24000 && at + i < song.length; i++) {
                double t = i / (double) Audio.RATE, fade = Math.min(1, i / 200.0) * Math.min(1, (24000 - i) / 800.0);
                song[at + i] += (float) (0.25 * fade * Math.exp(-t / 0.5) * (Math.sin(2 * Math.PI * hz * t) + 0.5 * Math.sin(4 * Math.PI * hz * t)));
            }
        }
        Hearing.Heard heard = Hearing.listenLowAndHigh(song);
        Hearing.Bar bar = Hearing.oneBar(heard);
        java.util.Set<String> inTheTune = new java.util.HashSet<>(List.of("."));
        for (int note : tune) inTheTune.add(Musician.nameOf(note));
        Path drummer = practised(dir, "d.life", 4), bass = bassPlayer(dir, "b.life", 2, 2), singer = dir.resolve("singer.life");
        Session.AlongPart first = null, last = null;
        float peak = 0;
        for (int s = 0; s < 12; s++) {
            Session.Along take = Session.playAlong(dir.resolve("t.wav"), List.of(drummer), null, bass, null, singer, "made up", song, heard, bar, 5, 24, 1500 + s);
            last = take.parts().get(2);
            if (s == 0) first = last;
            peak = Math.max(peak, take.peak());
        }
        int right = 0, believed = 0;
        for (String note : last.line().replace("|", " ").trim().split("\\s+")) if (!note.equals(".")) { believed++; if (inTheTune.contains(note)) right++; }
        System.out.printf(Locale.ROOT, "SINGER after twelve takes it sings %s, %d ticks to the pulse; it believes %d notes, %d of them the tune's; home note %s; %d on the song's note and %d not; out of tune by %.0f cents in its first take and %.0f in its last; unsteadiness %.1f ms%n",
            last.patternAfter(), last.ticksAfter(), believed, right, last.home(), last.withTheSong(), last.againstTheSong(), first.outOfTuneCents(), last.outOfTuneCents(), last.unsteadyMs());
        assertEquals("the singer", last.name());
        assertTrue(last.plays().startsWith("a sung note"));
        assertEquals("p...............", first.patternBefore(), "a newborn singer has one note, on the one");
        assertTrue(believed >= 6, "it comes to believe notes of the tune: " + last.line());
        assertTrue(right >= 0.8 * believed, "and they are the tune's: " + last.line());
        assertTrue(last.withTheSong() > 2 * last.againstTheSong(), "most of what it sings is the note the tune is on: " + last.withTheSong() + " to " + last.againstTheSong());
        assertTrue(first.outOfTuneCents() > 12, "a newborn singer is out of tune: " + first.outOfTuneCents());
        assertTrue(last.outOfTuneCents() < first.outOfTuneCents(), "and gets better at it: " + first.outOfTuneCents() + " to " + last.outOfTuneCents());
        assertTrue(peak < 1.0f);
    }

    @Test
    void aVoiceHoldsItsNoteAndIsHeardAtThePitchItIsGiven() {
        Bass voice = Bass.voice();
        for (double hz : new double[] {261.63, 392.0, 523.25}) {
            voice.tune(hz);
            voice.detune(0);
            voice.strike(Kit.POT, 1f);
            float[] sound = new float[(int) Audio.samples(1.0)];
            float peak = 0, late = 0;
            for (int i = 0; i < sound.length; i++) { sound[i] = voice.next(); peak = Math.max(peak, Math.abs(sound[i])); if (i > Audio.samples(0.8)) late = Math.max(late, Math.abs(sound[i])); }
            int crossings = 0, from = (int) Audio.samples(0.05), to = (int) Audio.samples(0.15);
            double re = 0, im = 0;
            for (int i = from; i < to; i++) { re += sound[i] * Math.cos(2 * Math.PI * hz * i / Audio.RATE); im += sound[i] * Math.sin(2 * Math.PI * hz * i / Audio.RATE); }
            double atPitch = Math.hypot(re, im), re2 = 0, im2 = 0, off = hz * Math.pow(2, 0.5 / 12);
            for (int i = from; i < to; i++) { re2 += sound[i] * Math.cos(2 * Math.PI * off * i / Audio.RATE); im2 += sound[i] * Math.sin(2 * Math.PI * off * i / Audio.RATE); }
            assertTrue(peak > 0.05 && peak < 0.6, "a voice is neither silent nor too loud: " + peak);
            assertTrue(late > 0.2 * peak, "it is still holding its note after most of a second: " + late + " of " + peak);
            assertTrue(atPitch > 3 * Math.hypot(re2, im2), "its bottom tone is at the pitch it was given, not a quarter tone away");
        }
    }
}
