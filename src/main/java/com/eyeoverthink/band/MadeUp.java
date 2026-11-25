package com.eyeoverthink.band;

/**
 * A made-up recording, for players who have no real song to play along with yet. It is written here, by
 * hand, and is nobody's music: a shuffle in threes with four parts that do different things, so that
 * players who play along with it have different things to take. A kick on one and three, a snare on two
 * and four, a hat on the first and last of every three, and a bass line four bars long.
 * What the players make of it they have to hear; nothing tells them what is in it.
 */
public final class MadeUp {
    private MadeUp() {}

    /** The bass line, one note to the pulse and a pick-up on the last of the three before each bar: piano key numbers. */
    static final int[] LINE = {33, 33, 36, 38,   33, 33, 36, 40,   38, 38, 36, 33,   31, 31, 35, 40};

    public static float[] shuffle(double bpm, int bars) { return shuffle(bpm, bars, 0.8, 0.15, 0.6, 0.2); }

    /** The same, with how loud the kick, the snare, the hat and the bass are in it. */
    static float[] shuffle(double bpm, int bars, double kickLevel, double snareLevel, double hatLevel, double bassLevel) {
        double tick = 60.0 / bpm / 3, lead = 0.5;
        float[] mix = new float[(int) (Audio.RATE * (lead + bars * 12 * tick + 2))];
        Hats hats = new Hats();
        int ticks = bars * 12, next = 0;
        long nextAt = (long) Audio.samples(lead);
        double phase = 0, hz = 0, since = 1e9, snare = 0, noiseBefore = 0, kick = 0, kickSince = 0, kickPhase = 0;   // the kick is a low thud that falls in pitch, well under the bass
        java.util.SplittableRandom hiss = new java.util.SplittableRandom(3);   // the snare is a burst of hiss with no bottom to it, so it is not taken for a kick
        for (int i = 0; i < mix.length; i++) {
            if (next < ticks && i >= nextAt) {
                int place = next % 12, inPulse = place % 3, pulse = place / 3;
                if (inPulse == 0 && (pulse == 0 || pulse == 2)) { kick = pulse == 0 ? 1.0 : 0.85; kickSince = 0; kickPhase = 0; }
                if (inPulse == 0 && (pulse == 1 || pulse == 3)) snare = 1;
                if (inPulse != 1) hats.strike(Kit.PAN, inPulse == 0 ? 0.6f : 0.5f);
                if (inPulse == 0) { hz = LineEar.hzOf(LINE[(next / 3) % LINE.length]); since = 0; phase = 0; }
                else if (place == 11) { hz = LineEar.hzOf(LINE[((next + 1) / 3) % LINE.length] - 2); since = 0; phase = 0; }   // the pick-up, a step under the note it leads to
                next++;
                nextAt = (long) Audio.samples(lead + next * tick);
            }
            double t = since / Audio.RATE;
            double bass = hz == 0 ? 0 : Math.min(1, since / 200.0) * Math.exp(-t / 0.35) * (Math.sin(phase) + 0.5 * Math.sin(2 * phase) + 0.25 * Math.sin(3 * phase));
            phase += 2 * Math.PI * hz / Audio.RATE; since++;
            double noise = hiss.nextDouble() * 2 - 1, crack = snare * (noise - noiseBefore) / 2;
            noiseBefore = noise; snare *= Math.exp(-1.0 / (0.06 * Audio.RATE));
            double thud = kick * Math.exp(-kickSince / (0.045 * Audio.RATE)) * Math.sin(kickPhase);   // short, so it is gone before the bass note under it is listened for
            kickPhase += 2 * Math.PI * (45 + 60 * Math.exp(-kickSince / (0.03 * Audio.RATE))) / Audio.RATE; kickSince++;
            mix[i] = (float) (kickLevel * thud + snareLevel * crack + hatLevel * hats.next() + bassLevel * bass);
        }
        return mix;
    }
}
