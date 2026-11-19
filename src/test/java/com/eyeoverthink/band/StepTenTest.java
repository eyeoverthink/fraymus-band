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

/** Step ten: the bass player earns a second note by playing in tune. */
class StepTenTest {
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
    void withAClickTheyLearnToLeanOnEachOtherAndLandCloser(@TempDir Path dir) throws Exception {
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
        assertTrue(late < apart[0] - 1.5, "they land closer: " + apart[0] + " ms at first, " + late + " ms later");
        assertTrue(leaning[sessions - 1] > 0.1, "because they learned to lean: " + leaning[sessions - 1]);
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
        assertTrue(late > early + 0.8, "it ends more like its teacher than it began: " + early + " to " + late);
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
        assertTrue(atFour[sessions - 1] > drummers * 0.7, "with practice most of them get there");
        assertTrue(like[sessions - 1] > like[0] + 2, "and their pattern has grown toward the lesson");
        assertTrue(early[sessions - 1] < early[0] - 6, "and their time is far steadier");
        assertTrue(listen[sessions - 1] > listen[0] + 0.2, "because they learned to listen");
        assertTrue(apart[sessions - 1] < apart[0] - 6, "and they land closer to the record when they play with it");
        assertTrue(landed[sessions - 1] >= landed[0] && landed[sessions - 1] > drummers * 0.8, "and nearly all of them end together with it");
    }
}
