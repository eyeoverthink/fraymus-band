package com.eyeoverthink.band;

/**
 * The record. It plays the lesson for two bars and leaves two bars of room for an answer, as many times as
 * asked. Then it plays the lesson straight through for some bars more, and ends on the one after them.
 * It has its own clock, it does not listen, and it does not wait.
 */
public final class Teacher implements Player, Hits {
    private static final long AHEAD = Math.round(Audio.samples(0.035));
    private final int places, call, cycle;   // places in the record's bar (sixteen, or twelve in threes), and in a call and a call-and-answer

    private final Clock clock;
    private final Kit kit = new Kit();
    private final byte[] bar = new byte[Musician.SLOTS];
    private final double[] push = new double[Musician.SLOTS];   // the feel: how far after its tick each place is played, in samples
    private final double[] weight = new double[Musician.SLOTS];
    private final long straightFrom, lastTick;
    private long tick;
    private long strikeAt = -1;
    private byte strikeSound;
    private int strikePlace;
    private final long[] hits;
    private int hitCount;

    public Teacher(Lesson lesson, int calls, int barsTogether, double firstBeatSeconds) {
        this.clock = new Clock(Audio.periodOf(lesson.tempo()), Audio.samples(firstBeatSeconds));
        this.places = lesson.bar().length(); this.call = Musician.TURN * places; this.cycle = 2 * call;
        for (int i = 0; i < places; i++) {
            bar[i] = Kit.read(lesson.bar().charAt(i));
            push[i] = Audio.samples(lesson.pushMs()[i] / 1000.0);
            weight[i] = lesson.weight()[i];
        }
        this.straightFrom = (long) calls * cycle;
        this.lastTick = straightFrom + (long) barsTogether * places;
        this.hits = new long[(calls * Musician.TURN + barsTogether + 1) * Musician.SLOTS];
    }

    @Override public String name() { return "the record"; }
    @Override public void hear(float[] others, float[] own, float[] click, float[] track, int n, long blockStart) { }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        while (strikeAt < 0 && tick <= lastTick && clock.dueBefore(blockEnd + AHEAD)) {   // early enough to play a place ahead of its tick
            int place = (int) (tick % places);
            boolean playing = tick < straightFrom ? tick % cycle < call : tick < lastTick;
            boolean ending = tick == lastTick && lastTick > straightFrom;
            byte sound = ending ? Kit.POT : playing ? bar[place] : Kit.REST;
            if (sound != Kit.REST) {
                strikeAt = Math.max(Math.round(clock.nextBeat() + (ending ? 0 : push[place])), blockStart);
                strikeSound = sound;
                strikePlace = place;
            }
            clock.advance(1.0 / (places / Musician.PULSES));
            tick++;
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                kit.strike(strikeSound, (float) (weight[strikePlace] * 0.94));
                if (hitCount < hits.length) hits[hitCount++] = blockStart + i;
                strikeAt = -1;
            }
            out[i] += kit.next();
        }
    }

    @Override public int hitCount() { return hitCount; }
    @Override public long hitAt(int i) { return hits[i]; }
}
