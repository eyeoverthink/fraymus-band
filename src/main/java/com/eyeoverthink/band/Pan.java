package com.eyeoverthink.band;

/**
 * The second sound: a spoon on a pan. Bright and short, made of tones that do not line up with each
 * other the way a string's do, which is what makes metal sound like metal.
 */
public final class Pan implements Sound {
    private final float[] shape;

    public Pan() {
        int n = (int) Audio.samples(0.22);
        shape = new float[n];
        double[] tone = {587, 1423, 2289, 3411, 4873};
        double[] dies = {0.11, 0.085, 0.065, 0.05, 0.035};
        double[] loud = {1.0, 0.8, 0.6, 0.45, 0.3};
        java.util.SplittableRandom click = new java.util.SplittableRandom(11);
        float peak = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) Audio.RATE;
            double ring = 0;
            for (int k = 0; k < tone.length; k++) ring += loud[k] * Math.sin(2 * Math.PI * tone[k] * t) * Math.exp(-t / dies[k]);
            double tick = (click.nextDouble() * 2 - 1) * Math.exp(-t / 0.0015) * 1.2;
            double fadeOut = Math.min(1.0, (n - i) / Audio.samples(0.01));
            shape[i] = (float) ((ring + tick) * fadeOut);
            peak = Math.max(peak, Math.abs(shape[i]));
        }
        for (int i = 0; i < n; i++) shape[i] *= 0.55f / peak;
    }

    @Override public int length() { return shape.length; }
    @Override public float at(int i) { return shape[i]; }
}
