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

/** Step four: after the answers, the record and the drummer play at the same time, each on its own clock. */
class StepFourTest {
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
    void aPractisedDrummerStaysWithTheRecord(@TempDir Path dir) throws Exception {
        int close = 0, landed = 0;
        for (int seed = 0; seed < 10; seed++) {
            Session.Report r = Session.run(dir.resolve("t.wav"), practised(dir, seed + ".life", 4, LESSON.bar()), LESSON, 2, 8, seed);
            assertEquals((2 * Drummer.TURN + 8) * Drummer.SLOTS, r.lastHitPlace(), "it counts its bars and lands the one after them");
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
            assertEquals(2 * Drummer.TURN * Drummer.SLOTS, r.lastHitPlace(), "the last hit is the one after its last answer");
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
        assertTrue(lines.get(7).startsWith("session 1: pulses=64"), "what it lived stays as it was written");
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
                int reach = Drummer.SLOTS / Drummer.PULSES / had;
                assertEquals('p', p.charAt(0), "the one stays on the pot");
                for (int i = 0; i < Drummer.SLOTS; i++) {
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
        assertTrue(landed[sessions - 1] > landed[0] + drummers / 5, "and far more of them end together with it");
    }
}
