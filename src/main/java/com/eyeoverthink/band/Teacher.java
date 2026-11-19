package com.eyeoverthink.band;

/**
 * The record. It plays the lesson for two bars, leaves two bars of room for an answer, and comes back in
 * on its own one whether or not the answer is finished. It has its own clock, it does not listen, and it
 * does not wait.
 */
public final class Teacher implements Player {
    private static final int CALL = 2 * Drummer.SLOTS, CYCLE = 4 * Drummer.SLOTS;

    private final Clock clock;
    private final Kit kit = new Kit();
    private final byte[] bar = new byte[Drummer.SLOTS];
    private final int calls;
    private long tick;
    private long strikeAt = -1;
    private byte strikeSound;
    private int strikePlace;

    public Teacher(Lesson lesson, int calls, double firstBeatSeconds) {
        this.clock = new Clock(Audio.periodOf(lesson.tempo()), Audio.samples(firstBeatSeconds));
        for (int i = 0; i < Drummer.SLOTS; i++) bar[i] = Kit.read(lesson.bar().charAt(i));
        this.calls = calls;
    }

    @Override public String name() { return "the record"; }
    @Override public void hear(float[] heard, int n, long blockStart) { }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        while (strikeAt < 0 && clock.dueBefore(blockEnd)) {
            int inCycle = (int) (tick % CYCLE);
            if (tick / CYCLE < calls && inCycle < CALL && bar[inCycle % Drummer.SLOTS] != Kit.REST) {
                strikeAt = Math.max(Math.round(clock.nextBeat()), blockStart);
                strikeSound = bar[inCycle % Drummer.SLOTS];
                strikePlace = inCycle % Drummer.SLOTS;
            }
            clock.advance(1.0 / (Drummer.SLOTS / Drummer.PULSES));
            tick++;
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                kit.strike(strikeSound, (float) (Kit.weight(strikePlace) * 0.94));
                strikeAt = -1;
            }
            out[i] += kit.next();
        }
    }
}
