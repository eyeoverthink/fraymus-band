package com.eyeoverthink.band;

/**
 * A closed hi-hat: one short, bright sound. Worked out once and then only read. Whatever a pattern asks
 * for, a hat player has this one sound to give, so its pattern is written with n alone.
 */
public final class Hats implements Instrument {
    private final float[] tick;
    private int at = -1;
    private float loud;

    public Hats() {
        int n = (int) Audio.samples(0.07);
        tick = new float[n];
        java.util.SplittableRandom hiss = new java.util.SplittableRandom(23);
        double a = 0, b = 0;
        float peak = 0;
        for (int i = 0; i < n; i++) {
            double noise = hiss.nextDouble() * 2 - 1;
            double once = noise - a; a = noise;              // twice through "what changed since the last sample":
            double twice = once - b; b = once;               // that keeps the top of the hiss and drops the rest
            tick[i] = (float) (twice * Math.exp(-i / Audio.samples(0.014)) * Math.min(1.0, (n - i) / Audio.samples(0.005)));
            peak = Math.max(peak, Math.abs(tick[i]));
        }
        for (int i = 0; i < n; i++) tick[i] *= 0.7f / peak;
    }

    @Override public void strike(byte sound, float loudness) { at = 0; loud = loudness; }

    @Override
    public float next() {
        if (at < 0) return 0f;
        float s = tick[at] * loud * 0.5f;
        if (++at >= tick.length) at = -1;
        return s;
    }

    @Override public String describe() { return "a hi-hat (n)"; }
}
