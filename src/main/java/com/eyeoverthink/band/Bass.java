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

    private final boolean voice;
    /** Where a singer's voice sits when it knows no better: the C above middle C. */
    public static final double VOICE_OPEN = 523.25;
    private static final double[] MOUTH = {500, 900, 2500}, MOUTH_WIDE = {90, 110, 160}, MOUTH_LOUD = {1.0, 0.6, 0.25};   // an open "oh"
    private final double[] sung = new double[24];        // how loud each tone of the sung note is, set when the note starts
    private int tones;

    public Bass() { this(false, false); }
    /** A voice: a sung "oh" with no words, held until the next note or until the breath runs out, with a little waver once it has settled. It can be out of tune, as a bass with no frets can. */
    public static Bass voice() { return new Bass(false, true); }
    public boolean isVoice() { return voice; }
    private Bass(boolean keys, boolean voice) {
        this.keys = keys; this.voice = voice;
        PARTIAL = keys ? KEYS_PARTIAL : BASS_PARTIAL;
        LENGTH = (int) Audio.samples(voice ? 1.3 : keys ? 0.9 : 0.55);
        ring = keys ? 0.45 : 0.30;
        open = voice ? VOICE_OPEN : keys ? KEYS_OPEN : LOW;
        low = open;
    }
    /** The same kind of thing with keys under the hand: a struck tone in the middle of the band, an electric piano more or less. Its notes are where they are; it cannot be out of tune. */
    public static Bass keys() { return new Bass(true, false); }
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
        if (voice) {                                         // the mouth shapes the note: each tone is as loud as the mouth lets it be
            tones = (int) Math.min(sung.length, 5000 / hz);
            double most = 1e-9;
            for (int k = 0; k < tones; k++) {
                double f = hz * (k + 1), a = 0;
                for (int m = 0; m < MOUTH.length; m++) { double away = (f - MOUTH[m]) / MOUTH_WIDE[m]; a += MOUTH_LOUD[m] / (1 + away * away); }
                sung[k] = a / Math.sqrt(k + 1) + 0.03 / (k + 1); most = Math.max(most, sung[k]);
            }
            for (int k = 0; k < tones; k++) sung[k] /= most;
            phase = 0; since = 0; gain = 1;
        }
    }

    private double second, third;         // the other two notes of a chord, as so many times the pitch of the bottom one; nothing if it plays one note

    /** A keys player's hand takes a chord: the note it is tuned to and two above it, so many semitones up. Nothing above, and it plays the one note. */
    public void chord(int semitonesToTheSecond, int semitonesToTheThird) {
        second = !keys || semitonesToTheSecond == 0 ? 0 : Math.pow(2, semitonesToTheSecond / 12.0);
        third = !keys || semitonesToTheThird == 0 ? 0 : Math.pow(2, semitonesToTheThird / 12.0);
    }

    private double phase, gain = 1;
    private int since;                                   // samples since the note began, or since the singer last leaned on it

    /** True while a note is still sounding. */
    public boolean sounding() { return at >= 0; }

    /** A singer already on the note it wants carries on with it: no new start, the breath behind it renewed. */
    public void hold() { if (voice && at >= 0) since = 0; }

    @Override
    public float next() {
        if (at < 0) return 0f;
        if (voice) {
            double t = at / (double) Audio.RATE;
            double waver = t < 0.2 ? 0 : Math.min(1, (t - 0.2) / 0.3) * 0.009 * Math.sin(2 * Math.PI * 5.5 * t);   // it wavers once the note has settled
            phase += 2 * Math.PI * hz * (1 + waver) / Audio.RATE;
            double sum = 0;
            for (int k = 0; k < tones; k++) sum += sung[k] * Math.sin(phase * (k + 1));
            gain += (Math.exp(-since / (1.1 * Audio.RATE)) - gain) * 0.0008;   // it swells back gently when the note is held on, with no bump
            double breath = Math.min(1.0, t / 0.015) * gain * Math.min(1.0, (LENGTH - since) / Audio.samples(0.08));
            at++;
            if (++since >= LENGTH) at = -1;
            return (float) (sum * breath * 0.16 * loud);
        }
        double t = at / (double) Audio.RATE, sum = 0;
        for (int tone = 0; tone < 3; tone++) {               // one note, or the three of a chord under a keys player's hand
            double of = tone == 0 ? 1 : tone == 1 ? second : third;
            if (of == 0) continue;
            for (int k = 0; k < PARTIAL.length; k++)
                sum += (second == 0 ? 1 : 0.6) * PARTIAL[k] * Math.sin(2 * Math.PI * hz * of * (k + 1) * t) * Math.exp(-t / (ring / (1 + 0.6 * k)));
        }
        double pluck = Math.min(1.0, t / 0.003);
        double fadeOut = Math.min(1.0, (LENGTH - at) / Audio.samples(0.02));
        if (++at >= LENGTH) at = -1;
        return (float) (sum * pluck * fadeOut * LEVEL * loud * 0.5);
    }

    @Override public String describe() { return voice ? "a sung note (p) and, once it has earned it, the same note an octave up (n)" : keys ? "a chord on the keys (p) and, once it has earned it, the same note an octave up (n)" : "a low note (p) and, once it has earned it, the same note an octave up (n)"; }
}
