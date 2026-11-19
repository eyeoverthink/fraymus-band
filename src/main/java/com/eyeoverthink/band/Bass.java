package com.eyeoverthink.band;

/**
 * A bass with one note: a plucked low E. The note is worked out once and then only read. The upper
 * partials are kept strong on purpose, so the note can still be heard on a small speaker that cannot
 * move enough air for the bottom of it.
 */
public final class Bass implements Instrument {
    private final float[] note;
    private int at = -1;
    private float loud;

    public Bass() {
        int n = (int) Audio.samples(0.55);
        note = new float[n];
        double hz = 82.41;
        double[] partial = {1.0, 0.8, 0.55, 0.35, 0.2};
        float peak = 0;
        for (int i = 0; i < n; i++) {
            double t = i / (double) Audio.RATE, sum = 0;
            for (int k = 0; k < partial.length; k++)
                sum += partial[k] * Math.sin(2 * Math.PI * hz * (k + 1) * t) * Math.exp(-t / (0.30 / (1 + 0.6 * k)));
            double pluck = Math.min(1.0, t / 0.003);
            double fadeOut = Math.min(1.0, (n - i) / Audio.samples(0.02));
            note[i] = (float) (sum * pluck * fadeOut);
            peak = Math.max(peak, Math.abs(note[i]));
        }
        for (int i = 0; i < n; i++) note[i] *= 0.8f / peak;
    }

    @Override public void strike(byte sound, float loudness) { at = 0; loud = loudness; }

    @Override
    public float next() {
        if (at < 0) return 0f;
        float s = note[at] * loud * 0.5f;
        if (++at >= note.length) at = -1;
        return s;
    }

    @Override public String describe() { return "one low note (p)"; }
}
