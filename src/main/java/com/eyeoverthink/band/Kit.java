package com.eyeoverthink.band;

/** A pot and a pan, and the sound of each still ringing. Claimed once; nothing is made while playing. */
public final class Kit implements Instrument {
    public static final byte REST = 0, POT = 1, PAN = 2;

    private final Sound[] sounds = {null, new Thump(), new Pan()};
    private final int[] at = {-1, -1, -1};
    private final float[] loud = new float[3];

    @Override public String describe() { return "a pot (p) and a pan (n)"; }

    @Override public void strike(byte sound, float loudness) { at[sound] = 0; loud[sound] = loudness; }

    /** The next sample of whatever is ringing. */
    @Override public float next() {
        float sum = 0;
        for (int v = POT; v <= PAN; v++) {
            if (at[v] < 0) continue;
            sum += sounds[v].at(at[v]) * loud[v];
            if (++at[v] >= sounds[v].length()) at[v] = -1;
        }
        return sum * 0.5f;
    }

    /** How hard a place in the bar is hit: the one hardest, the pulses next, the small ticks softest. */
    public static double weight(int place) {
        return place == 0 ? 1.0 : place % 4 == 0 ? 0.75 : place % 2 == 0 ? 0.62 : 0.52;
    }

    public static byte read(char c) { return c == 'p' ? POT : c == 'n' ? PAN : REST; }
    public static char write(byte b) { return b == POT ? 'p' : b == PAN ? 'n' : '.'; }
}
