package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Path;

/**
 * One sitting. The room carries sound to the players, they hear, they play, and the take is written down.
 * Every buffer is claimed in {@link #run} before the first block; the loop itself only moves samples.
 * Nobody here keeps time for anyone else, and the session does not decide when the take ends.
 */
public final class Session {
    /** What happened, measured from the hits themselves. */
    public record Report(Lesson lesson, int answers, int hits, double seconds, double startedBpm, double endedBpm,
                         double earlyUnsteadyMs, double lateUnsteadyMs, double listeningBefore, double listeningAfter,
                         int ticksBefore, int ticksAfter, String patternBefore, String patternAfter, String lessonAsHeard,
                         int matchesBefore, int matchesAfter, int changesKept, int changesTakenBack, int copied, int ownIdeas,
                         int cutShort, int lastHitPlace, double ringSeconds, double lastPulseSeconds, float peak, Path wav,
                         int barsTogether, int hitsTogether, double apartMs, double leanMs, int alone, double landedApartMs,
                         double[] feelMs, double[] feelWeight) {}

    /** How long a player's own sound takes to reach it, and how long everyone else's does. */
    public static final double NEAR = 0.003, ACROSS = 0.009;
    /** What is in a player's headphones when there is no track: nothing. Never written to. */
    private static final float[] quiet = new float[Audio.BLOCK];

    public static Report run(Path wav, Path lifeFile, Lesson lesson, int answers, int barsTogether, long seed) throws IOException {
        return run(wav, lifeFile, lesson, answers, barsTogether, false, seed);
    }

    /** The same, with or without a click in the drummer's ears. The record was made to the same click. */
    public static Report run(Path wav, Path lifeFile, Lesson lesson, int answers, int barsTogether, boolean click, long seed) throws IOException {
        Metronome metronome = click ? new Metronome(lesson.tempo(), 0.25) : null;
        double start = click ? metronome.firstBarSeconds() : 0.25;
        float[] clicks = new float[Audio.BLOCK];
        Life life = Life.open(lifeFile, "the drummer");
        Teacher teacher = new Teacher(lesson, answers, barsTogether, start);
        Musician drummer = new Musician(life.name(), 92.0, life.hitsPlayed(), life.listening(), life.ticks(), life.pattern(), answers, barsTogether, seed);
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromRecord = new float[Audio.BLOCK], fromDrummer = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];
        drummer.leaning(life.leaning()); drummer.habits(life.habits()); drummer.feel(life.feel()); drummer.asked(life.asked()); recall(drummer, life, lesson.name());
        double startedBpm = drummer.tempoNow();

        long now = 0;
        try (WavWriter out = new WavWriter(wav)) {
            long tooLong = (long) Audio.samples(60.0 + 30.0 * answers + 8.0 * barsTogether);   // nothing here should ever need this
            while (!drummer.done(now)) {
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");                     // no objects are made inside this loop
                if (click) metronome.play(clicks, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                drummer.hear(others, own, clicks, quiet, Audio.BLOCK, now);
                java.util.Arrays.fill(fromRecord, 0f);
                java.util.Arrays.fill(fromDrummer, 0f);
                teacher.play(fromRecord, Audio.BLOCK, now);
                drummer.play(fromDrummer, Audio.BLOCK, now);
                room.fill(0, fromRecord, Audio.BLOCK, now);
                room.fill(1, fromDrummer, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromRecord[i] + fromDrummer[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            int n = drummer.hitCount(), window = Math.min(24, n / 2);
            double early = unsteadiness(drummer, 0, window);
            double late = unsteadiness(drummer, n - window, n);
            Apart with = apart(drummer, drummer.togetherFrom(), teacher);
            int together = with.together, alone = with.alone;
            double apart = with.sum, lean = with.lean;
            double landedApart = barsTogether == 0 ? 0 : with.landed;
            double[] feelMs = new double[Musician.SLOTS], feelWeight = new double[Musician.SLOTS];
            for (int i = 0; i < Musician.SLOTS; i++) { feelMs[i] = drummer.feelMs(i); feelWeight[i] = drummer.feelWeight(i); }
            Report report = new Report(lesson, answers, n, Audio.seconds(out.samples()),
                startedBpm, drummer.tempoNow(), early, late, life.listening(), drummer.listening(),
                life.ticks(), drummer.ticks(), life.pattern(), drummer.pattern(), drummer.lessonAsHeard(),
                lesson.matches(life.pattern()), lesson.matches(drummer.pattern()),
                drummer.changesKept(), drummer.changesTakenBack(), drummer.copied(), drummer.ownIdeas(), drummer.cutShort(),
                drummer.hitSlot(n - 1), Audio.seconds(out.samples() - drummer.hitAt(n - 1)), drummer.lastPulse() / Audio.RATE,
                out.peak(), wav,
                barsTogether, together, together == 0 ? 0 : apart / together, together == 0 ? 0 : lean / together, alone, landedApart,
                feelMs, feelWeight);
            life.leaning(drummer.leaning()); life.habits(drummer.habits()); life.feel(drummer.feel());
            keep(drummer, life, lesson.name());
            life.remember(lesson.name(), answers, n, startedBpm, drummer.tempoNow(), early, late, drummer.listening(), drummer.ticks(),
                drummer.pattern(), lesson.matches(drummer.pattern()), barsTogether, together == 0 ? -1 : apart / together);
            return report;
        }
    }

    /** How close one drummer's hits landed to someone else's. */
    record Apart(int together, int alone, double sum, double lean, double landed) {
        double ms() { return together == 0 ? 0 : sum / together; }
        double leanMs() { return together == 0 ? 0 : lean / together; }
    }

    /** Each of the drummer's hits from place {@code from} on, against the other player's nearest hit. Called after the take. */
    static Apart apart(Musician d, int from, Hits other) {
        int together = 0, alone = 0, t = 0, n = d.hitCount();
        double sum = 0, lean = 0;
        if (other.hitCount() == 0 || n == 0) return new Apart(0, n, 0, 0, 0);
        for (int i = 0; i < n; i++) {
            if (d.hitSlot(i) < from) continue;
            while (t + 1 < other.hitCount() && Math.abs(other.hitAt(t + 1) - d.hitAt(i)) <= Math.abs(other.hitAt(t) - d.hitAt(i))) t++;
            double ms = (d.hitAt(i) - other.hitAt(t)) * 1000.0 / Audio.RATE;
            if (Math.abs(ms) > 80) { alone++; continue; }
            together++; sum += Math.abs(ms); lean += ms;
        }
        double landed = (d.hitAt(n - 1) - other.hitAt(other.hitCount() - 1)) * 1000.0 / Audio.RATE;
        return new Apart(together, alone, sum, lean, landed);
    }

    /** What happened when two drummers played together, with no record. */
    public record Duo(int bars, double seconds, Part first, Part second, int hitsTogether, double apartMs, double leanMs,
                      double landedApartMs, double tempoFirstBars, double tempoLastBars, float peak, Path wav) {}

    /** One drummer's side of it. */
    public record Part(String name, int hits, double unsteadyMs, double listeningBefore, double listeningAfter,
                       int ticksBefore, int ticksAfter, String pattern, int alone, double leaningBefore, double leaningAfter) {}

    /**
     * Two drummers and no record. The first starts; the second hears a bar and comes in. Each has its own
     * clock and its own life, and each listens to the other. Nobody keeps time for them.
     */
    public static Duo together(Path wav, Path firstLife, Path secondLife, int bars, long seed) throws IOException {
        return together(wav, firstLife, secondLife, bars, 0, seed);
    }

    /** The same, with a click at {@code clickBpm} in both drummers' ears, or none if that is 0. */
    public static Duo together(Path wav, Path firstLife, Path secondLife, int bars, double clickBpm, long seed) throws IOException {
        Metronome metronome = clickBpm > 0 ? new Metronome(clickBpm, 0.25) : null;
        double start = metronome != null ? metronome.firstBarSeconds() : 0.25;
        float[] clicks = new float[Audio.BLOCK];
        Life lifeA = Life.open(firstLife, "the drummer"), lifeB = Life.open(secondLife, "the second drummer");
        Musician a = Musician.withAnother(lifeA.name(), 92.0, lifeA.hitsPlayed(), lifeA.listening(), lifeA.ticks(), lifeA.pattern(), bars, true, start, seed);
        Musician b = Musician.withAnother(lifeB.name(), 92.0, lifeB.hitsPlayed(), lifeB.listening(), lifeB.ticks(), lifeB.pattern(), bars, false, start, seed ^ 0x9E3779B97F4A7C15L);
        a.leaning(lifeA.leaning()); b.leaning(lifeB.leaning()); a.habits(lifeA.habits()); b.habits(lifeB.habits()); a.feel(lifeA.feel()); b.feel(lifeB.feel()); a.asked(lifeA.asked()); b.asked(lifeB.asked()); recall(a, lifeA, null); recall(b, lifeB, null);
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromA = new float[Audio.BLOCK], fromB = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(30.0 + 8.0 * bars);
        try (WavWriter out = new WavWriter(wav)) {
            while (!(a.done(now) && b.done(now))) {          // no objects are made inside this loop
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                if (metronome != null) metronome.play(clicks, Audio.BLOCK, now);
                room.carry(0, others, own, Audio.BLOCK, now);
                a.hear(others, own, clicks, quiet, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                b.hear(others, own, clicks, quiet, Audio.BLOCK, now);
                java.util.Arrays.fill(fromA, 0f);
                java.util.Arrays.fill(fromB, 0f);
                a.play(fromA, Audio.BLOCK, now);
                b.play(fromB, Audio.BLOCK, now);
                room.fill(0, fromA, Audio.BLOCK, now);
                room.fill(1, fromB, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromA[i] + fromB[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            Apart bToA = apart(b, 0, a), aToB = apart(a, SLOTS_IN_A_BAR, b);   // the first drummer's opening bar is played alone
            double uA = unsteadiness(a, 0, a.hitCount()), uB = unsteadiness(b, 0, b.hitCount());
            Part first = new Part(lifeA.name(), a.hitCount(), uA, lifeA.listening(), a.listening(), lifeA.ticks(), a.ticks(), a.pattern(), aToB.alone(), lifeA.leaning(), a.leaning());
            Part second = new Part(lifeB.name(), b.hitCount(), uB, lifeB.listening(), b.listening(), lifeB.ticks(), b.ticks(), b.pattern(), bToA.alone(), lifeB.leaning(), b.leaning());
            Duo duo = new Duo(bars, Audio.seconds(out.samples()), first, second, bToA.together(), bToA.ms(), bToA.leanMs(), bToA.landed(),
                tempoOver(a, 0, 2), tempoOver(a, bars - 2, bars), out.peak(), wav);
            lifeA.leaning(a.leaning()); lifeB.leaning(b.leaning()); lifeA.habits(a.habits()); lifeB.habits(b.habits());
            keep(a, lifeA, null); keep(b, lifeB, null);
            lifeA.rememberTogether(lifeB.name(), bars, a.hitCount(), uA, a.listening(), a.ticks(), a.pattern(), bToA.together(), bToA.ms());
            lifeB.rememberTogether(lifeA.name(), bars, b.hitCount(), uB, b.listening(), b.ticks(), b.pattern(), bToA.together(), bToA.ms());
            return duo;
        }
    }

    /** What happened when one drummer gave the lesson to another. */
    public record Taught(int calls, int barsTogether, double seconds, String teacher, String teacherPattern, int teacherHits, double teacherUnsteadyMs,
                         String learner, int learnerHits, int ticksBefore, int ticksAfter, String heardAs, String patternBefore, String patternAfter,
                         int matchesBefore, int matchesAfter, int copied, int ownIdeas, int changesKept, int changesTakenBack, int cutShort,
                         double listeningBefore, double listeningAfter, double learnerUnsteadyMs,
                         int hitsTogether, double apartMs, int alone, double landedApartMs, float peak, Path wav) {}

    /**
     * One drummer teaches another. The teacher plays its own pattern for two bars and leaves two bars of
     * room, on its own clock; the learner answers. After the last answer they play on together.
     */
    public static Taught teach(Path wav, Path teacherLife, Path learnerLife, int calls, int barsTogether, long seed) throws IOException {
        return teach(wav, teacherLife, learnerLife, calls, barsTogether, 0, seed);
    }

    /** The same, with a click at {@code clickBpm} in both drummers' ears, or none if that is 0. */
    public static Taught teach(Path wav, Path teacherLife, Path learnerLife, int calls, int barsTogether, double clickBpm, long seed) throws IOException {
        Metronome metronome = clickBpm > 0 ? new Metronome(clickBpm, 0.25) : null;
        double start = metronome != null ? metronome.firstBarSeconds() : 0.25;
        float[] clicks = new float[Audio.BLOCK];
        Life lifeT = Life.open(teacherLife, "the drummer"), lifeL = Life.open(learnerLife, "the second drummer");
        Musician t = Musician.teaching(lifeT.name(), 92.0, lifeT.hitsPlayed(), lifeT.listening(), lifeT.ticks(), lifeT.pattern(), calls, barsTogether, start, seed);
        Musician l = new Musician(lifeL.name(), 92.0, lifeL.hitsPlayed(), lifeL.listening(), lifeL.ticks(), lifeL.pattern(), calls, barsTogether, seed ^ 0x9E3779B97F4A7C15L);
        t.leaning(lifeT.leaning()); l.leaning(lifeL.leaning()); t.habits(lifeT.habits()); l.habits(lifeL.habits()); t.feel(lifeT.feel()); l.feel(lifeL.feel()); t.asked(lifeT.asked()); l.asked(lifeL.asked()); recall(t, lifeT, null); recall(l, lifeL, null);
        Lesson given = new Lesson(lifeT.name(), 92.0, lifeT.pattern());      // only used afterwards, to count how alike they ended
        Room room = new Room(2, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK];
        float[] fromT = new float[Audio.BLOCK], fromL = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(60.0 + 30.0 * calls + 8.0 * barsTogether);
        try (WavWriter out = new WavWriter(wav)) {
            while (!(t.done(now) && l.done(now))) {          // no objects are made inside this loop
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                if (metronome != null) metronome.play(clicks, Audio.BLOCK, now);
                room.carry(0, others, own, Audio.BLOCK, now);
                t.hear(others, own, clicks, quiet, Audio.BLOCK, now);
                room.carry(1, others, own, Audio.BLOCK, now);
                l.hear(others, own, clicks, quiet, Audio.BLOCK, now);
                java.util.Arrays.fill(fromT, 0f);
                java.util.Arrays.fill(fromL, 0f);
                t.play(fromT, Audio.BLOCK, now);
                l.play(fromL, Audio.BLOCK, now);
                room.fill(0, fromT, Audio.BLOCK, now);
                room.fill(1, fromL, Audio.BLOCK, now);
                for (int i = 0; i < Audio.BLOCK; i++) mix[i] = fromT[i] + fromL[i];
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            Apart with = apart(l, l.togetherFrom(), t);
            double uT = unsteadiness(t, 0, t.hitCount()), uL = unsteadiness(l, 0, l.hitCount());
            int before = given.matches(lifeL.pattern()), after = given.matches(l.pattern());
            Taught taught = new Taught(calls, barsTogether, Audio.seconds(out.samples()), lifeT.name(), t.pattern(), t.hitCount(), uT,
                lifeL.name(), l.hitCount(), lifeL.ticks(), l.ticks(), l.lessonAsHeard(), lifeL.pattern(), l.pattern(), before, after,
                l.copied(), l.ownIdeas(), l.changesKept(), l.changesTakenBack(), l.cutShort(), lifeL.listening(), l.listening(), uL,
                with.together(), with.ms(), with.alone(), barsTogether == 0 ? 0 : with.landed(), out.peak(), wav);
            lifeT.leaning(t.leaning()); lifeL.leaning(l.leaning()); lifeT.habits(t.habits()); lifeL.habits(l.habits()); lifeL.feel(l.feel());
            keep(t, lifeT, null); keep(l, lifeL, null);
            lifeT.rememberTaught(lifeL.name(), calls, barsTogether, t.hitCount(), uT, t.listening(), t.ticks(), t.pattern(), after);
            lifeL.remember(lifeT.name(), calls, l.hitCount(), 92.0, l.tempoNow(), unsteadiness(l, 0, Math.min(24, l.hitCount() / 2)),
                unsteadiness(l, l.hitCount() - Math.min(24, l.hitCount() / 2), l.hitCount()), l.listening(), l.ticks(), l.pattern(), after,
                barsTogether, with.together() == 0 ? -1 : with.ms());
            return taught;
        }
    }

    /** What happened when players played along with a recording. */
    public record Along(String recording, double tempo, double fromSeconds, int bars, double seconds, java.util.List<AlongPart> parts, float peak, Path wav) {}

    /** One player's side of it. */
    /** Before a session: a player is reminded of the places that are its own, and of every record it has lived with but the one it is with now. */
    private static void recall(Musician m, Life life, String with) {
        m.own(life.own());
        for (var heard : life.heard().entrySet()) if (!heard.getKey().equals(with)) m.influence(heard.getKey(), heard.getValue());
    }

    /** After a session: what it now believes this record plays, and what is its own, go into its life. */
    private static void keep(Musician m, Life life, String with) {
        if (with != null) life.heard(with, m.believes());
        life.own(m.own());
    }

    public record AlongPart(String name, String plays, int hits, int landedWithIt, double apartMs, int heard, String believes,
                            String patternBefore, String patternAfter, int copied, int ownIdeas, int kept, int takenBack,
                            double unsteadyMs, double listeningBefore, double listeningAfter, int ticksBefore, int ticksAfter, int notes,
                            int askedAndPlaying, int askedAndWaiting, String line, int linePlaces, int linePlacesInReach,
                            int withTheSong, int againstTheSong, double outOfTuneCents, int phraseBars, String phraseHeard,
                            int borrowed, int borrowedKept, String borrowedFrom, String own, double swing) {}

    /** One drummer plays along. */
    public static Along playAlong(Path wav, Path lifeFile, String recordingName, float[] sound, Hearing.Heard heard, Hearing.Bar bar,
                                  double fromSeconds, int bars, long seed) throws IOException {
        return playAlong(wav, java.util.List.of(lifeFile), null, recordingName, sound, heard, bar, fromSeconds, bars, seed);
    }

    /**
     * Players play along with a real recording: one or more drummers and, if a life is given for one, a bass
     * player. The track is in their headphones with a click at its tempo, lined up with its one, and they all
     * start together after the count-in. They hear each other across the room. {@code heard} is what the
     * ear made of the whole recording beforehand: only its tempo and where a one falls are used to set the
     * click, and its hits to measure the take afterwards. The players are told nothing about what is in it.
     */
    public static Along playAlong(Path wav, java.util.List<Path> drummerLives, Path bassLife, String recordingName, float[] sound,
                                  Hearing.Heard heard, Hearing.Bar bar, double fromSeconds, int bars, long seed) throws IOException {
        return playAlong(wav, drummerLives, null, bassLife, recordingName, sound, heard, bar, fromSeconds, bars, seed);
    }

    /** The same, with a hat player as well if a life is given for one. The order is drummers, hat player, bass player. */
    public static Along playAlong(Path wav, java.util.List<Path> drummerLives, Path hatsLife, Path bassLife, String recordingName, float[] sound,
                                  Hearing.Heard heard, Hearing.Bar bar, double fromSeconds, int bars, long seed) throws IOException {
        Metronome metronome = new Metronome(bar.bpm(), 0.25);
        double start = metronome.firstBarSeconds(), barSeconds = Musician.PULSES * 60.0 / bar.bpm();
        double one = bar.aOneAtSeconds() + Math.ceil((fromSeconds - bar.aOneAtSeconds()) / barSeconds) * barSeconds;   // the first one at or after there
        float loudest = 1e-9f;                               // found once, before anything plays
        for (float v : sound) loudest = Math.max(loudest, Math.abs(v));
        int n = drummerLives.size() + (hatsLife == null ? 0 : 1) + (bassLife == null ? 0 : 1), bassAt = bassLife == null ? -1 : n - 1;
        int hatsAt = hatsLife == null ? -1 : drummerLives.size();
        float trackLevel = (n == 1 ? 0.55f : 0.45f) / loudest, playerLevel = n == 1 ? 1f : n <= 3 ? 0.55f : 0.45f;   // room for everyone on top without clipping
        Recording recording = new Recording(sound, one - start, 1f / loudest);
        Life[] lives = new Life[n];
        Musician[] players = new Musician[n];
        String[] nameIfNew = {"the drummer", "the second drummer", "the third drummer"};
        for (int i = 0; i < n; i++) {
            lives[i] = i == bassAt ? Life.open(bassLife, "the bass player", Musician.FIRST_BASS_PATTERN)
                     : i == hatsAt ? Life.open(hatsLife, "the hat player", Musician.FIRST_HATS_PATTERN)
                     : Life.open(drummerLives.get(i), nameIfNew[Math.min(i, 2)]);
            long chance = seed + 0x9E3779B97F4A7C15L * i;
            players[i] = i == bassAt
                ? Musician.onBass(lives[i].name(), 92.0, lives[i].hitsPlayed(), lives[i].listening(), lives[i].ticks(), lives[i].pattern(), lives[i].notes(), bars, chance)
                : i == hatsAt
                ? Musician.onHats(lives[i].name(), 92.0, lives[i].hitsPlayed(), lives[i].listening(), lives[i].ticks(), lives[i].pattern(), bars, chance)
                : Musician.alongARecording(lives[i].name(), 92.0, lives[i].hitsPlayed(), lives[i].listening(), lives[i].ticks(), lives[i].pattern(), bars, start, chance);
            players[i].alongATrackFrom(start);
            players[i].leaning(lives[i].leaning()); players[i].habits(lives[i].habits()); players[i].feel(lives[i].feel()); players[i].asked(lives[i].asked()); recall(players[i], lives[i], recordingName);
            if (i == bassAt) { players[i].line(lives[i].line(recordingName)); players[i].phrase(lives[i].phrase(recordingName)); players[i].phraseFrom((int) Math.round((one - bar.aOneAtSeconds()) / barSeconds)); }
            lives[i].plays(players[i].plays());
        }
        Room room = new Room(n, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK], clicks = new float[Audio.BLOCK];
        float[] track = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];
        float[][] from = new float[n][Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(30.0 + 8.0 * bars);
        try (WavWriter out = new WavWriter(wav)) {
            while (true) {                                   // no objects are made inside this loop
                boolean allDone = true;
                for (int i = 0; i < n; i++) allDone &= players[i].done(now);
                if (allDone) break;
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                metronome.play(clicks, Audio.BLOCK, now);
                recording.play(track, Audio.BLOCK, now);     // into their headphones, and onto the take
                for (int i = 0; i < n; i++) {
                    room.carry(i, others, own, Audio.BLOCK, now);
                    players[i].hear(others, own, clicks, track, Audio.BLOCK, now);
                }
                for (int k = 0; k < Audio.BLOCK; k++) mix[k] = track[k] * trackLevel * loudest;
                for (int i = 0; i < n; i++) {
                    java.util.Arrays.fill(from[i], 0f);
                    players[i].play(from[i], Audio.BLOCK, now);
                    room.fill(i, from[i], Audio.BLOCK, now);
                    for (int k = 0; k < Audio.BLOCK; k++) mix[k] += from[i][k] * playerLevel;
                }
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            java.util.List<AlongPart> parts = new java.util.ArrayList<>();
            double[] times = heard.hitTimes();
            for (int i = 0; i < n; i++) {
                Musician d = players[i];
                int hits = d.hitCount(), landed = 0, t = 0;
                double apart = 0;
                for (int k = 0; k < hits && times.length > 0; k++) {   // each of its hits against the nearest hit the ear found in the recording
                    double at = Audio.seconds(d.hitAt(k)) - start + one;
                    while (t + 1 < times.length && Math.abs(times[t + 1] - at) <= Math.abs(times[t] - at)) t++;
                    double ms = Math.abs(times[t] - at) * 1000;
                    if (ms <= 25) { landed++; apart += ms; }
                }
                double unsteady = unsteadiness(d, 0, hits);
                parts.add(new AlongPart(lives[i].name(), d.plays(), hits, landed, landed == 0 ? 0 : apart / landed, d.heardWhilePlaying(), d.believes(),
                    lives[i].pattern(), d.pattern(), d.copied(), d.ownIdeas(), d.changesKept(), d.changesTakenBack(),
                    unsteady, lives[i].listening(), d.listening(), lives[i].ticks(), d.ticks(), d.notes(), d.askedAndPlaying(), d.askedAndWaiting(),
                    d.lineInWords(), d.linePlaces(), d.linePlacesInReach(), d.withTheSong(), d.againstTheSong(), d.outOfTuneCents(), d.phraseBars(), d.phrase(),
                    d.borrowed(), d.borrowedKept(), d.borrowedFrom(), d.own(), d.swing()));
                lives[i].leaning(d.leaning()); lives[i].habits(d.habits()); lives[i].feel(d.feel());
                if (i == bassAt) { lives[i].notes(d.notes()); if (d.notes() >= 2) { lives[i].line(recordingName, d.line()); lives[i].phrase(recordingName, d.phrase()); } }
                keep(d, lives[i], recordingName);
                lives[i].rememberAlong(recordingName, bars, hits, unsteady, d.listening(), d.ticks(), d.pattern(), landed, landed == 0 ? 0 : apart / landed);
            }
            return new Along(recordingName, bar.bpm(), one, bars, Audio.seconds(out.samples()), parts, out.peak(), wav);
        }
    }

    /** What happened when drummers and a bass player played together. */
    public record Band(int bars, double seconds, java.util.List<Part> parts, double tempoFirstBars, double tempoLastBars,
                       String bassBefore, String bassAfter, int bassNotes, int bassNotesOnAPot, int bassHits, double bassApartMs,
                       int bassCopied, int bassOwnIdeas, int bassKept, int bassTakenBack, double landedApartMs, float peak, Path wav,
                       int bassNotesKnownBefore, int bassNotesKnownAfter, double bassOutOfTuneCents, int bassHighNotes, int bassHighNotesOnAPan) {}

    /**
     * One or two drummers and a bass player, with no record. The first drummer starts; everyone else hears
     * a bar and comes in. Each has its own clock and its own life. The bass player listens for the pots and
     * moves its notes toward them while it plays. The last life given is the bass player's.
     */
    public static Band band(Path wav, java.util.List<Path> drummerLives, Path bassLife, int bars, double clickBpm, long seed) throws IOException {
        Metronome metronome = clickBpm > 0 ? new Metronome(clickBpm, 0.25) : null;
        double start = metronome != null ? metronome.firstBarSeconds() : 0.25;
        int n = drummerLives.size() + 1, bassAt = n - 1;
        Life[] lives = new Life[n];
        Musician[] players = new Musician[n];
        String[] nameIfNew = {"the drummer", "the second drummer", "the third drummer"};
        for (int i = 0; i < n; i++) {
            lives[i] = i == bassAt ? Life.open(bassLife, "the bass player", Musician.FIRST_BASS_PATTERN) : Life.open(drummerLives.get(i), nameIfNew[Math.min(i, 2)]);
            long chance = seed + 0x9E3779B97F4A7C15L * i;
            players[i] = i == bassAt
                ? Musician.onBass(lives[i].name(), 92.0, lives[i].hitsPlayed(), lives[i].listening(), lives[i].ticks(), lives[i].pattern(), lives[i].notes(), bars, chance)
                : Musician.withAnother(lives[i].name(), 92.0, lives[i].hitsPlayed(), lives[i].listening(), lives[i].ticks(), lives[i].pattern(), bars, i == 0, start, chance);
            players[i].leaning(lives[i].leaning()); players[i].habits(lives[i].habits()); players[i].feel(lives[i].feel()); players[i].asked(lives[i].asked()); recall(players[i], lives[i], null);
            lives[i].plays(players[i].plays());
        }
        Room room = new Room(n, NEAR, ACROSS);
        float[] others = new float[Audio.BLOCK], own = new float[Audio.BLOCK], clicks = new float[Audio.BLOCK], mix = new float[Audio.BLOCK];
        float[][] from = new float[n][Audio.BLOCK];

        long now = 0, tooLong = (long) Audio.samples(30.0 + 8.0 * bars);
        try (WavWriter out = new WavWriter(wav)) {
            while (true) {                                   // no objects are made inside this loop
                boolean allDone = true;
                for (int i = 0; i < n; i++) allDone &= players[i].done(now);
                if (allDone) break;
                if (now > tooLong) throw new IllegalStateException("The take never ended. That is a bug.");
                if (metronome != null) metronome.play(clicks, Audio.BLOCK, now);
                for (int i = 0; i < n; i++) {
                    room.carry(i, others, own, Audio.BLOCK, now);
                    players[i].hear(others, own, clicks, quiet, Audio.BLOCK, now);
                }
                java.util.Arrays.fill(mix, 0f);
                for (int i = 0; i < n; i++) {
                    java.util.Arrays.fill(from[i], 0f);
                    players[i].play(from[i], Audio.BLOCK, now);
                    room.fill(i, from[i], Audio.BLOCK, now);
                    for (int k = 0; k < Audio.BLOCK; k++) mix[k] += from[i][k] * BAND_LEVEL;
                }
                out.write(mix, Audio.BLOCK);
                now += Audio.BLOCK;
            }
            Musician first = players[0], bass = players[bassAt];
            java.util.List<Part> parts = new java.util.ArrayList<>();
            StringBuilder names = new StringBuilder();
            for (int i = 0; i < n; i++) {
                Apart a = i == 0 ? apart(first, SLOTS_IN_A_BAR, players[1]) : apart(players[i], 0, first);
                parts.add(new Part(lives[i].name(), players[i].hitCount(), unsteadiness(players[i], 0, players[i].hitCount()),
                    lives[i].listening(), players[i].listening(), lives[i].ticks(), players[i].ticks(), players[i].pattern(), a.alone(),
                    lives[i].leaning(), players[i].leaning()));
            }
            Apart bassToFirst = apart(bass, 0, first);
            int notes = 0, onAPot = 0;
            int high = 0, onAPan = 0;
            for (int s = 0; s < Musician.SLOTS; s++) {
                char played = bass.pattern().charAt(s);
                if (played == '.') continue;
                notes++;
                if (played == 'n') high++;
                for (int i = 0; i < bassAt; i++) if (players[i].pattern().charAt(s) == played) { if (played == 'p') onAPot++; else onAPan++; break; }
            }
            Band band = new Band(bars, Audio.seconds(out.samples()), parts, tempoOver(first, 0, 2), tempoOver(first, bars - 2, bars),
                lives[bassAt].pattern(), bass.pattern(), notes, onAPot, bassToFirst.together(), bassToFirst.ms(),
                bass.copied(), bass.ownIdeas(), bass.changesKept(), bass.changesTakenBack(), bassToFirst.landed(), out.peak(), wav,
                lives[bassAt].notes(), bass.notes(), bass.outOfTuneCents(), high, onAPan);
            for (int i = 0; i < n; i++) {
                names.setLength(0);
                for (int k = 0; k < n; k++) if (k != i) names.append(names.length() == 0 ? "" : " and ").append(lives[k].name());
                Apart a = i == 0 ? apart(first, SLOTS_IN_A_BAR, players[1]) : apart(players[i], 0, first);
                lives[i].leaning(players[i].leaning()); lives[i].habits(players[i].habits());
                if (i == bassAt) lives[i].notes(players[i].notes());
                keep(players[i], lives[i], null);
                lives[i].rememberTogether(names.toString(), bars, players[i].hitCount(), parts.get(i).unsteadyMs(), players[i].listening(),
                    players[i].ticks(), players[i].pattern(), a.together(), a.ms());
            }
            return band;
        }
    }

    /** With three or more players the mix is turned down so that all of them hitting at once cannot clip. */
    static final float BAND_LEVEL = 0.7f;

    private static final int SLOTS_IN_A_BAR = Musician.SLOTS;

    /** The tempo a drummer actually played from one bar to another, from when its hits landed. */
    static double tempoOver(Musician d, int fromBar, int toBar) {
        int first = -1, last = -1;
        for (int i = 0; i < d.hitCount(); i++) {
            int bar = d.hitSlot(i) / Musician.SLOTS;
            if (bar >= fromBar && bar <= toBar) { if (first < 0) first = i; last = i; }
        }
        if (first < 0 || last == first) return 0;
        double places = d.hitSlot(last) - d.hitSlot(first);
        double pulse = (d.hitAt(last) - d.hitAt(first)) / places * (Musician.SLOTS / Musician.PULSES);
        return Audio.bpmOf(pulse);
    }

    /**
     * How uneven the playing is, in milliseconds: how far each hit lands from where the two hits before it
     * said it would, allowing for how many places apart they are. Only hits inside the same answer are
     * compared. A slow change of tempo does not count against the player; a lurch does.
     */
    static double unsteadiness(Musician d, int from, int to) {
        int count = 0, answer = Musician.TURN * Musician.SLOTS;
        double sumSquares = 0;
        for (int i = from + 2; i < to; i++) {
            if (d.hitSlot(i) / answer != d.hitSlot(i - 2) / answer) continue;
            // where each hit would have been without the feel it meant
            double t2 = d.hitAt(i - 2) - d.hitPush(i - 2), t1 = d.hitAt(i - 1) - d.hitPush(i - 1), t0 = d.hitAt(i) - d.hitPush(i);
            double gapA = t1 - t2, gapB = t0 - t1;
            double placesA = d.hitSlot(i - 1) - d.hitSlot(i - 2), placesB = d.hitSlot(i) - d.hitSlot(i - 1);
            if (placesA <= 0 || placesB <= 0) continue;      // it lost count of its bars there; nothing to compare
            double off = gapB - gapA * placesB / placesA;
            sumSquares += off * off;
            count++;
        }
        return count == 0 ? 0 : Math.sqrt(sumSquares / count) / Audio.RATE * 1000.0;
    }
}
