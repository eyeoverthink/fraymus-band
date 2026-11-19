package com.eyeoverthink.band;

/** The fixed facts of the sound every player shares. */
public final class Audio {
    /** Samples per second. */
    public static final int RATE = 44_100;
    /** Samples handled per step. Everything is sized from this once, before the first note. */
    public static final int BLOCK = 256;

    private Audio() {}

    public static double seconds(long samples) { return samples / (double) RATE; }
    public static double samples(double seconds) { return seconds * RATE; }
    public static double bpmOf(double periodSamples) { return 60.0 * RATE / periodSamples; }
    public static double periodOf(double bpm) { return 60.0 * RATE / bpm; }
}
