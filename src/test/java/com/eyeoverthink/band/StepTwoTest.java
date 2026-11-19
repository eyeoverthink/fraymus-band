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

/** Step two: the tick, a second sound, a pattern built in rounds, and an ending the drummer owns. */
class StepTwoTest {

    @Test
    void theTakeIsARealWavFile(@TempDir Path dir) throws Exception {
        Path wav = dir.resolve("take.wav");
        Session.Report r = Session.run(wav, dir.resolve("d.life"), 8, 7);
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
        assertTrue(r.seconds() > 15, "8 bars near 92 bpm last well over fifteen seconds");
    }

    @Test
    void aNewbornHasOneTickToThePulseAndThePotOnEveryPulse(@TempDir Path dir) throws Exception {
        Session.Report r = Session.run(dir.resolve("take.wav"), dir.resolve("d.life"), 4, 5);
        assertEquals(1, r.ticksBefore());
        assertEquals("p...p...p...p...", r.patternBefore());
        assertEquals(r.hits(), r.heard(), "and it hears every hit come back");
    }

    @Test
    void thePanIsBrighterThanThePot() {
        assertTrue(crossings(new Pan()) > 4 * crossings(new Thump()));
    }

    private static int crossings(Sound s) {
        int n = 0;
        for (int i = 1; i < s.length(); i++) if (s.at(i - 1) < 0 != s.at(i) < 0) n++;
        return n;
    }

    @Test
    void itLandsOnTheOneAndRingsForOnePulseOfItsOwn(@TempDir Path dir) throws Exception {
        double shortest = 9, longest = 0;
        for (int seed = 0; seed < 12; seed++) {
            Session.Report r = Session.run(dir.resolve("t.wav"), dir.resolve(seed + ".life"), 8, seed);
            assertEquals(8 * Drummer.SLOTS, r.lastHitPlace(), "the last hit is the one after the last bar");
            assertEquals(r.lastPulseSeconds(), r.ringSeconds(), 2.0 * Audio.BLOCK / Audio.RATE, "it rings for one pulse");
            shortest = Math.min(shortest, r.ringSeconds());
            longest = Math.max(longest, r.ringSeconds());
        }
        assertTrue(longest - shortest > 0.03, "the ring follows its tempo; it is not one fixed length");
    }

    @Test
    void theSameLifeAndTheSameChanceGiveTheSameTake(@TempDir Path dir) throws Exception {
        Session.run(dir.resolve("a.wav"), dir.resolve("a.life"), 8, 99);
        Session.run(dir.resolve("b.wav"), dir.resolve("b.life"), 8, 99);
        assertArrayEquals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("b.wav")));
        Session.run(dir.resolve("c.wav"), dir.resolve("c.life"), 8, 100);
        assertFalse(java.util.Arrays.equals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("c.wav"))));
    }

    @Test
    void whatItLearnedIsWrittenIntoItsLifeAndIsWhereTheNextSessionStarts(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("d.life");
        Session.Report first = Session.run(dir.resolve("1.wav"), file, 8, 40);
        Session.Report second = Session.run(dir.resolve("2.wav"), file, 8, 41);
        assertEquals(first.patternAfter(), second.patternBefore());
        assertEquals(first.ticksAfter(), second.ticksBefore());
        assertEquals(first.listeningAfter(), second.listeningBefore(), 0.006);
        List<String> lines = Files.readAllLines(file);
        assertEquals(2, lines.stream().filter(l -> l.startsWith("session ")).count());
        Life life = Life.open(file, "someone else");
        assertEquals("the drummer", life.name());
        assertEquals(first.hits() + second.hits(), life.hitsPlayed());
    }

    @Test
    void aLifeFromStepOneStillReads(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("old.life");
        Files.write(file, List.of("name: the drummer", "age: 0", "sound: one, a hand on a pot", "listens to itself: 0.27", "",
            "session 1: pulses=64, tempo 92.0 to 108.6 bpm, unsteadiness 15.1 ms at the start, 10.3 ms by the end, listening 0.27"));
        Life life = Life.open(file, "x");
        assertEquals(64, life.hitsPlayed());
        assertEquals(0.27, life.listening(), 1e-9);
        assertEquals(1, life.ticks());
        Session.Report r = Session.run(dir.resolve("t.wav"), file, 4, 1);
        assertEquals(0.27, r.listeningBefore(), 1e-9);
        List<String> lines = Files.readAllLines(file);
        assertTrue(lines.get(5 + 2).startsWith("session 1: pulses=64"), "what it lived stays as it was written");
        assertTrue(lines.get(lines.size() - 1).startsWith("session 2: bars=4"));
    }

    /** The claims that matter, over sixty drummers living twenty sessions each. */
    @Test
    void itHasToGrowIntoItsTicks(@TempDir Path dir) throws Exception {
        int drummers = 60, sessions = 20;
        double[] early = new double[sessions], listen = new double[sessions], ticks = new double[sessions], hits = new double[sessions], unheard = new double[sessions];
        int[] atTwo = new int[sessions], atFour = new int[sessions];
        for (int d = 0; d < drummers; d++) {
            Path life = dir.resolve("d" + d + ".life"), wav = dir.resolve("d" + d + ".wav");
            int had = 1;
            for (int s = 0; s < sessions; s++) {
                Session.Report r = Session.run(wav, life, 16, 1000L * d + s);
                early[s] += r.earlyUnsteadyMs() / drummers;
                listen[s] += r.listeningAfter() / drummers;
                ticks[s] += r.ticksAfter() / (double) drummers;
                hits[s] += r.hits() / (double) drummers;
                unheard[s] += (r.hits() - r.heard()) / (double) r.hits() / drummers;
                if (r.ticksAfter() >= 2) atTwo[s]++;
                if (r.ticksAfter() == 4) atFour[s]++;

                assertTrue(r.ticksAfter() == had || r.ticksAfter() == 2 * had || r.ticksAfter() == 4 * had && had == 1, "ticks only ever get finer");
                assertTrue(r.ticksAfter() <= 4);
                had = r.ticksAfter();
                String p = r.patternAfter();
                int reach = Drummer.SLOTS / Drummer.PULSES / had;
                assertEquals('p', p.charAt(0), "the one stays on the pot");
                for (int i = 0; i < Drummer.SLOTS; i++) {
                    if (i % reach != 0) assertEquals('.', p.charAt(i), "it only plays places its ticks can reach: " + p + " with " + had);
                    if (i % 4 == 0) assertNotEquals('.', p.charAt(i), "a pulse is never left empty: " + p);
                }
                assertTrue(r.peak() > 0.2f && r.peak() < 1.0f, "loud enough to hear, never clipped: " + r.peak());
                assertTrue(r.endedBpm() > 65 && r.endedBpm() < 125, "it stays near where it is comfortable: " + r.endedBpm());
            }
        }
        for (int s = 0; s < sessions; s++)
            System.out.printf(Locale.ROOT, "STUDY session %2d: unsteadiness %.1f ms, listening %.2f, at two ticks %d, at four %d, hits %.0f, unheard %.1f%%%n",
                s + 1, early[s], listen[s], atTwo[s], atFour[s], hits[s], 100 * unheard[s]);
        assertEquals(0, atFour[0], "nobody is born playing sixteenths");
        assertTrue(atTwo[0] < drummers / 4, "few even find the half in their first session");
        assertTrue(atFour[sessions - 1] > drummers * 0.9, "with practice nearly all of them get there");
        assertTrue(early[sessions - 1] < early[0] - 6, "and their time is far steadier");
        assertTrue(listen[sessions - 1] > listen[0] + 0.2, "because they learned to listen");
        assertTrue(hits[sessions - 1] > hits[0] * 1.5, "and the pattern has built up");
        for (double u : unheard) assertTrue(u < 0.02, "the ear hears nearly every hit");
    }
}
