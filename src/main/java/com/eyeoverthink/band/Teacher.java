package com.eyeoverthink.band;

/**
 * The record. It plays the lesson for two bars and leaves two bars of room for an answer, as many times as
 * asked. Then it plays the lesson straight through for some bars more, and ends on the one after them.
 * It has its own clock, it does not listen, and it does not wait.
 */
public final class Teacher implements Player, Hits {
    private static final int CALL = Musician.TURN * Musician.SLOTS, CYCLE = 2 * CALL;

    private final Clock clock;
    private final Kit kit = new Kit();
    private final byte[] bar = new byte[Musician.SLOTS];
    private final long straightFrom, lastTick;
    private long tick;
    private long strikeAt = -1;
    private byte strikeSound;
    private int strikePlace;
    private final long[] hits;
    private int hitCount;

    public Teacher(Lesson lesson, int calls, int barsTogether, double firstBeatSeconds) {
        this.clock = new Clock(Audio.periodOf(lesson.tempo()), Audio.samples(firstBeatSeconds));
        for (int i = 0; i < Musician.SLOTS; i++) bar[i] = Kit.read(lesson.bar().charAt(i));
        this.straightFrom = (long) calls * CYCLE;
        this.lastTick = straightFrom + (long) barsTogether * Musician.SLOTS;
        this.hits = new long[(calls * Musician.TURN + barsTogether + 1) * Musician.SLOTS];
    }

    @Override public String name() { return "the record"; }
    @Override public void hear(float[] others, float[] own, float[] click, int n, long blockStart) { }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        while (strikeAt < 0 && tick <= lastTick && clock.dueBefore(blockEnd)) {
            int place = (int) (tick % Musician.SLOTS);
            boolean playing = tick < straightFrom ? tick % CYCLE < CALL : tick < lastTick;
            boolean ending = tick == lastTick && lastTick > straightFrom;
            byte sound = ending ? Kit.POT : playing ? bar[place] : Kit.REST;
            if (sound != Kit.REST) {
                strikeAt = Math.max(Math.round(clock.nextBeat()), blockStart);
                strikeSound = sound;
                strikePlace = place;
            }
            clock.advance(1.0 / (Musician.SLOTS / Musician.PULSES));
            tick++;
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                kit.strike(strikeSound, (float) (Kit.weight(strikePlace) * 0.94));
                if (hitCount < hits.length) hits[hitCount++] = blockStart + i;
                strikeAt = -1;
            }
            out[i] += kit.next();
        }
    }

    @Override public int hitCount() { return hitCount; }
    @Override public long hitAt(int i) { return hits[i]; }
}
