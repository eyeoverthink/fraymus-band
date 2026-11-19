package com.eyeoverthink.band;

/**
 * The infant drummer's one sound: a hand on a pot. A short low tone that falls in pitch and dies away,
 * with a click at the front. It is worked out once and then only read.
 */
public final class Thump implements Sound {
    private final float[] shape;

    public Thump() {
        int n = (int) Audio.samples(0.28);
        shape = new float[n];
        java.util.SplittableRandom click = new java.util.SplittableRandom(7);
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) Audio.RATE;
            double pitch = 85 + 110 * Math.exp(-t / 0.035);
            phase += 2 * Math.PI * pitch / Audio.RATE;
            double body = Math.sin(phase) * Math.exp(-t / 0.085);
            double tick = (click.nextDouble() * 2 - 1) * Math.exp(-t / 0.0025) * 0.35;
            double fadeOut = Math.min(1.0, (n - i) / Audio.samples(0.01));
            shape[i] = (float) ((body + tick) * fadeOut * 0.8);
        }
    }

    @Override public int length() { return shape.length; }
    @Override public float at(int i) { return shape[i]; }
}
