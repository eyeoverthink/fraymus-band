package com.eyeoverthink.band;

/**
 * Three practice recordings for a keys player, written here by hand from pieces that have been out of
 * copyright for well over a century. They are studies made from the pieces, not the pieces: each is
 * slowed to eighth notes and laid out so that one chord lasts a bar, with the bass note of the chord under
 * it, because that is what this player's ear can follow.
 *
 * - "bach": the opening eight chords of the Prelude in C from the first book of the Well-Tempered Clavier,
 *   each for a bar, in the prelude's own figure: up through the chord, then the top three notes again.
 * - "mozart": the opening of the Sonata in C, K. 545, as its left hand has it: bottom, top, middle, top.
 * - "chopin": the first eight chords of the Prelude in C minor, Op. 28 No. 20, each given a bar and struck
 *   whole on every pulse, as the prelude strikes them.
 *
 * The chords are written from memory and are the plain three-note ones: sevenths and inversions are left out.
 */
public final class Classics {
    private Classics() {}

    public static final String[] PIECES = {"bach", "mozart", "chopin"};

    /** Each chord: the note it is built on (0 is C) and whether its third is the smaller one. */
    private static final int[][] BACH = {{0, 0}, {2, 1}, {7, 0}, {0, 0}, {9, 1}, {2, 0}, {7, 0}, {0, 0}};
    private static final int[][] MOZART = {{0, 0}, {7, 0}, {0, 0}, {5, 0}, {0, 0}, {7, 0}, {0, 0}, {0, 0}};
    private static final int[][] CHOPIN = {{0, 1}, {5, 1}, {7, 0}, {0, 1}, {8, 0}, {1, 0}, {3, 0}, {8, 0}};

    /** The figure of each piece, an eighth note at a time: r, 3, 5 for the notes of the chord, capitals an octave up, c for the whole chord, a dot for nothing. */
    public static String figureOf(String piece) {
        return switch (piece) {
            case "bach" -> "r35RT5RT";
            case "mozart" -> "r535r535";
            case "chopin" -> "c.c.c.c.";
            default -> throw new IllegalArgumentException("No such piece: " + piece);
        };
    }

    public static float[] render(String piece, double bpm) {
        int[][] chords = switch (piece) { case "bach" -> BACH; case "mozart" -> MOZART; case "chopin" -> CHOPIN; default -> throw new IllegalArgumentException("No such piece: " + piece); };
        int barsEach = 1, times = 4;                         // eight chords, a bar each, four times through: a phrase this player can keep
        String figure = figureOf(piece);
        double eighth = 60.0 / bpm / 2, lead = 0.5;
        int bars = chords.length * barsEach * times;
        float[] mix = new float[(int) (Audio.RATE * (lead + bars * 8 * eighth + 2))];
        for (int bar = 0; bar < bars; bar++) {
            int[] chord = chords[(bar / barsEach) % chords.length];
            int root = 48 + chord[0], third = chord[1] == 1 ? 3 : 4;
            if (root > 55) root -= 12;                       // keep the figure in the middle of the keyboard
            double barAt = lead + bar * 8 * eighth;
            boolean struck = piece.equals("chopin");          // under struck chords the bass note is short too, or its ring is taken for a note of the figure
            note(mix, barAt, 36 + chord[0] - (chord[0] > 7 ? 12 : 0), struck ? 0.2 : 1.2, struck ? 0.22 : 0.30);   // the bass note under it
            for (int e = 0; e < 8; e++) {
                char tone = figure.charAt(e);
                double at = barAt + e * eighth;
                switch (tone) {
                    case 'r' -> note(mix, at, root, 0.22, 0.26);
                    case '3' -> note(mix, at, root + third, 0.22, 0.26);
                    case '5' -> note(mix, at, root + 7, 0.22, 0.26);
                    case 'R' -> note(mix, at, root + 12, 0.22, 0.26);
                    case 'T' -> note(mix, at, root + 12 + third, 0.22, 0.26);
                    case 'c' -> { note(mix, at, root, 0.11, 0.2); note(mix, at, root + third, 0.11, 0.22); note(mix, at, root + 7, 0.11, 0.22); }   // short, so that the eighth between two chords is heard as empty
                    default -> { }
                }
            }
            for (int e = 0; e < 8; e++) tap(mix, barAt + e * eighth, e == 0 ? 0.5 : e % 2 == 0 ? 0.25 : 0.12);   // something to count by: the one, the pulses, and the eighths between, or four struck chords to the bar could as well be three
        }
        return mix;
    }

    /** One struck note: a few tones over the key's pitch, dying away at the given rate. */
    private static void note(float[] mix, double atSeconds, int midi, double ring, double level) {
        int at = (int) Audio.samples(atSeconds), length = (int) Audio.samples(ring * 6);
        double hz = LineEar.hzOf(midi);
        for (int i = 0; i < length && at + i < mix.length; i++) {
            double t = i / (double) Audio.RATE, start = Math.min(1, i / 120.0);
            mix[at + i] += (float) (level * start * Math.exp(-t / ring) * (Math.sin(2 * Math.PI * hz * t) + 0.5 * Math.sin(4 * Math.PI * hz * t) + 0.2 * Math.sin(6 * Math.PI * hz * t)));
        }
    }

    /** A dull tap on the pulse, heavier on the one, so that the ear can find the bar. */
    private static void tap(float[] mix, double atSeconds, double level) {
        int at = (int) Audio.samples(atSeconds);
        for (int i = 0; i < 3000 && at + i < mix.length; i++) {
            double t = i / (double) Audio.RATE;
            mix[at + i] += (float) (level * Math.exp(-t / 0.02) * Math.sin(2 * Math.PI * (50 + 70 * Math.exp(-t / 0.02)) * t));
        }
    }
}
