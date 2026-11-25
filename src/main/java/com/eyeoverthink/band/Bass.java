package com.eyeoverthink.band;

/**
 * A plucked bass. Its low note is an E unless the player moves its hand ({@link #tune}); its high note is
 * the octave above the low one. It has no frets: the player's hand
 * decides how far off the note lands, and {@link #detune} is how the hand tells it. The sound is worked
 * out as it rings, from a few numbers claimed once; nothing is made while playing. The upper partials are
 * kept strong on purpose, so a note can still be heard on a small speaker that cannot move enough air for
 * the bottom of it.
 */
public final class Bass implements Instrument {
    public static final double LOW = 82.41, HIGH = 164.81;
    private static final double[] BASS_PARTIAL = {1.0, 0.8, 0.55, 0.35, 0.2}, KEYS_PARTIAL = {1.0, 0.45, 0.12, 0.2, 0.05};
    /** Where a keys player's hand rests when it knows no better: the E below middle C. */
    public static final double KEYS_OPEN = 164.81;
    private final double[] PARTIAL;
    private final int LENGTH;
    private final double ring, open;
    private final boolean keys;

    public Bass() { this(false); }
    private Bass(boolean keys) {
        this.keys = keys;
        PARTIAL = keys ? KEYS_PARTIAL : BASS_PARTIAL;
        LENGTH = (int) Audio.samples(keys ? 0.9 : 0.55);
        ring = keys ? 0.45 : 0.30;
        open = keys ? KEYS_OPEN : LOW;
        low = open;
    }
    /** The same kind of thing with keys under the hand: a struck tone in the middle of the band, an electric piano more or less. Its notes are where they are; it cannot be out of tune. */
    public static Bass keys() { return new Bass(true); }
    public boolean isKeys() { return keys; }
    /** The pitch of its low note when its hand has not been moved. */
    public double open() { return open; }
    private static final double LEVEL = 0.8 / 1.9;      // so its loudest moment matches the old one-note bass

    private double cents;
    private double low;
    private double hz;
    private int at = -1;
    private float loud;

    /** How far off, in hundredths of a semitone, the next note will land. */
    public void detune(double cents) { this.cents = cents; }

    /** Where the hand is on the neck: the pitch of its low note for the next strike. The high note is the octave above it. */
    public void tune(double lowHz) { low = lowHz; }

    public static double pitchOf(byte sound) { return sound == Kit.PAN ? HIGH : LOW; }

    @Override
    public void strike(byte sound, float loudness) {
        hz = (sound == Kit.PAN ? 2 * low : low) * Math.pow(2, cents / 1200.0);
        at = 0;
        loud = loudness;
    }

    @Override
    public float next() {
        if (at < 0) return 0f;
        double t = at / (double) Audio.RATE, sum = 0;
        for (int k = 0; k < PARTIAL.length; k++)
            sum += PARTIAL[k] * Math.sin(2 * Math.PI * hz * (k + 1) * t) * Math.exp(-t / (ring / (1 + 0.6 * k)));
        double pluck = Math.min(1.0, t / 0.003);
        double fadeOut = Math.min(1.0, (LENGTH - at) / Audio.samples(0.02));
        if (++at >= LENGTH) at = -1;
        return (float) (sum * pluck * fadeOut * LEVEL * loud * 0.5);
    }

    @Override public String describe() { return keys ? "a note on the keys (p) and, once it has earned it, the same note an octave up (n)" : "a low note (p) and, once it has earned it, the same note an octave up (n)"; }
}
