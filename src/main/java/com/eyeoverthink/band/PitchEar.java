package com.eyeoverthink.band;

/**
 * How a player hears whether its own note was in tune. It keeps the last moments of its own sound, and a
 * little after a note begins it works out the pitch from that.
 * Nothing here is told what was played; it only measures. Everything is claimed once.
 */
public final class PitchEar {
    private static final int SKIP = (int) Audio.samples(0.015);     // let the pluck go by
    private static final int PIECE = (int) Audio.samples(0.073), APART = (int) Audio.samples(0.040);
    private static final int LOOK = PIECE + APART;
    private final float[] lately = new float[8192];
    private final float[] window = new float[LOOK];
    private long onset = -1;

    /** Take in this block of its own sound. */
    public void listen(float[] own, int n, long blockStart) {
        for (int i = 0; i < n; i++) lately[(int) ((blockStart + i) & 8191)] = own[i];
    }

    /** A note of its own began at this moment. */
    public void noteBegan(long at) { onset = at; }

    /** True once enough of the note has gone by to judge it. */
    public boolean ready(long now) { return onset >= 0 && now >= onset + SKIP + LOOK; }

    /**
     * The pitch of the note that began, looking near the pitch it meant to play. Call once it is {@link #ready}.
     * It listens for the bottom tone of the note at two moments a little apart and sees how far that tone
     * has got ahead of, or fallen behind, where the meant pitch would be. The upper tones of the note die
     * away at different speeds, so it does not trust the shape of the whole sound.
     */
    public double hz(double meant) {
        for (int i = 0; i < LOOK; i++) window[i] = lately[(int) ((onset + SKIP + i) & 8191)];
        onset = -1;
        double step = 2 * Math.PI * meant / Audio.RATE;
        double re1 = 0, im1 = 0, re2 = 0, im2 = 0;
        for (int i = 0; i < PIECE; i++) {
            double x = 2 * Math.PI * i / (PIECE - 1);
            double soft = 0.42 - 0.5 * Math.cos(x) + 0.08 * Math.cos(2 * x);    // fade each piece in and out, so the upper tones do not leak in
            double c = Math.cos(step * i), sn = Math.sin(step * i);
            re1 += soft * window[i] * c;          im1 -= soft * window[i] * sn;
            re2 += soft * window[i + APART] * c;  im2 -= soft * window[i + APART] * sn;
        }
        // The second piece started APART samples later; take out what the meant pitch alone would have turned by then.
        double turned = Math.atan2(im2 * re1 - re2 * im1, re2 * re1 + im2 * im1) - step * APART;
        turned -= 2 * Math.PI * Math.rint(turned / (2 * Math.PI));
        return meant + turned / (2 * Math.PI) * Audio.RATE / APART;
    }

    /** How far apart two pitches are, in hundredths of a semitone. */
    public static double cents(double heard, double meant) { return 1200.0 * Math.log(heard / meant) / Math.log(2); }
}
