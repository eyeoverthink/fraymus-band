package com.eyeoverthink.band;

/**
 * A click track. It ticks once a pulse, perfectly evenly, and never listens. It is not in the room: every
 * player hears it in its own ears, the way a band wears a click, and it is not on the recording. It counts
 * in for one bar before anyone plays.
 */
public final class Metronome {
    public static final int COUNT_IN = Drummer.PULSES;

    private final Clock clock;
    private final float[] tick;
    private int at = -1;

    public Metronome(double bpm, double firstClickSeconds) {
        this.clock = new Clock(Audio.periodOf(bpm), Audio.samples(firstClickSeconds));
        int n = (int) Audio.samples(0.004);
        tick = new float[n];
        for (int i = 0; i < n; i++) tick[i] = (float) (0.6 * Math.sin(2 * Math.PI * 2400 * i / Audio.RATE) * Math.exp(-i / Audio.samples(0.001)));
    }

    /** When the players it counted in should play their first hit: one bar after its first click. */
    public double firstBarSeconds() { return Audio.seconds(Math.round(clock.nextBeat())) + COUNT_IN * clock.period() / Audio.RATE; }

    /** Write this block of the click. */
    public void play(float[] out, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            if (blockStart + i >= clock.nextBeat()) { at = 0; clock.advance(); }
            out[i] = at >= 0 ? tick[at] : 0f;
            if (at >= 0 && ++at >= tick.length) at = -1;
        }
    }
}
