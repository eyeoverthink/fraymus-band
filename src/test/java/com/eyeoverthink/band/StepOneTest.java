package com.eyeoverthink.band;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Step one: one infant drummer, one sound, its own clock, a take you can hear. */
class StepOneTest {

    @Test
    void theTakeIsARealWavFile(@TempDir Path dir) throws Exception {
        Path wav = dir.resolve("take.wav");
        Session.Report r = Session.run(wav, dir.resolve("d.life"), 32, 7);
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
        assertTrue(r.seconds() > 10, "32 pulses near 92 bpm last well over ten seconds");
    }

    @Test
    void itPlaysWhatWasAskedAndHearsEveryHitComeBack(@TempDir Path dir) throws Exception {
        Session.Report r = Session.run(dir.resolve("take.wav"), dir.resolve("d.life"), 64, 11);
        assertEquals(64, r.pulses());
        assertEquals(64, r.heard());
    }

    @Test
    void itIsLoudEnoughToHearAndNeverClips(@TempDir Path dir) throws Exception {
        Session.Report r = Session.run(dir.resolve("take.wav"), dir.resolve("d.life"), 32, 3);
        assertTrue(r.peak() > 0.2f, "peak " + r.peak());
        assertTrue(r.peak() < 1.0f, "peak " + r.peak());
    }

    @Test
    void nobodyTellsItTheTempo(@TempDir Path dir) throws Exception {
        Session.Report a = Session.run(dir.resolve("a.wav"), dir.resolve("a.life"), 64, 1);
        Session.Report b = Session.run(dir.resolve("b.wav"), dir.resolve("b.life"), 64, 2);
        assertNotEquals(a.endedBpm(), b.endedBpm(), "two takes drift to different tempos");
        assertTrue(a.endedBpm() > 70 && a.endedBpm() < 115, "but it stays near where it is comfortable: " + a.endedBpm());
    }

    @Test
    void theSameLifeAndTheSameChanceGiveTheSameTake(@TempDir Path dir) throws Exception {
        Session.run(dir.resolve("a.wav"), dir.resolve("a.life"), 32, 99);
        Session.run(dir.resolve("b.wav"), dir.resolve("b.life"), 32, 99);
        assertArrayEquals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("b.wav")));
        Session.run(dir.resolve("c.wav"), dir.resolve("c.life"), 32, 100);
        assertFalse(java.util.Arrays.equals(Files.readAllBytes(dir.resolve("a.wav")), Files.readAllBytes(dir.resolve("c.wav"))));
    }

    @Test
    void everySessionIsWrittenIntoItsLife(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("d.life");
        Session.Report last = null;
        for (int s = 0; s < 3; s++) last = Session.run(dir.resolve("s" + s + ".wav"), file, 32, 40 + s);
        List<String> lines = Files.readAllLines(file);
        assertEquals(3, lines.stream().filter(l -> l.trim().startsWith("session ")).count());
        Life life = Life.open(file, "someone else");
        assertEquals("the drummer", life.name());
        assertEquals(3, life.sessions());
        assertEquals(96, life.pulsesPlayed());
        assertEquals(last.listeningAfter(), life.listening(), 0.006, "the habit it learned is where the next session starts");
    }

    /** The claim that matters: it gets steadier by living, and keeps what it learned between sessions. */
    @Test
    void practiceMakesItSteadierAndTheNextSessionStartsWhereTheLastLeftOff(@TempDir Path dir) throws Exception {
        int drummers = 60, sessions = 8;
        double[] early = new double[sessions], late = new double[sessions], listen = new double[sessions];
        for (int d = 0; d < drummers; d++) {
            Path life = dir.resolve("d" + d + ".life");
            Path wav = dir.resolve("d" + d + ".wav");
            for (int s = 0; s < sessions; s++) {
                Session.Report r = Session.run(wav, life, 64, 1000L * d + s);
                early[s] += r.earlyUnsteadyMs() / drummers;
                late[s] += r.lateUnsteadyMs() / drummers;
                listen[s] += r.listeningAfter() / drummers;
            }
        }
        for (int s = 0; s < sessions; s++)
            System.out.printf(java.util.Locale.ROOT, "STUDY session %d: start %.2f ms, end %.2f ms, listening %.2f%n", s + 1, early[s], late[s], listen[s]);
        assertTrue(early[sessions - 1] < early[0] - 2.0, "session 8 starts steadier than session 1");
        assertTrue(late[sessions - 1] < late[0] - 2.0, "and ends steadier too");
        assertTrue(listen[sessions - 1] > listen[0] + 0.2, "because it has learned to listen to itself");
    }
}
