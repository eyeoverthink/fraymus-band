package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Something to learn from: one bar of sixteen places, the tempo it is played at, and how it is played.
 * p is the pot, n is the pan, a dot is nothing. Write your own in a text file:
 *
 *   name: laid back
 *   tempo: 96
 *   bar:    p..pn...p.p.n..n
 *   push:   0 0 0 -8 22 0 0 0 0 0 0 0 22 0 0 12
 *   weight: 1.0 0 0 0.45 0.85 0 0 0 0.7 0 0.5 0 0.85 0 0 0.4
 *
 * {@code push} is the feel: how many milliseconds after the tick each place is played (a minus sign means
 * ahead of it). {@code weight} is how hard each place is hit, from 0 to 1. Both are optional; without
 * them the bar is played dead on the tick with the usual accents. Keep the one the heaviest hit of the
 * bar: that is how a listener finds it.
 */
public record Lesson(String name, double tempo, String bar, double[] pushMs, double[] weight) {
    public Lesson {
        if (bar.length() != Musician.SLOTS) throw new IllegalArgumentException("A bar has " + Musician.SLOTS + " places, this one has " + bar.length() + ": " + bar);
        if (!bar.matches("[pn.]+")) throw new IllegalArgumentException("A bar is written with p, n and dots only: " + bar);
        if (tempo < 40 || tempo > 200) throw new IllegalArgumentException("Tempo should be between 40 and 200: " + tempo);
        if (pushMs.length != Musician.SLOTS || weight.length != Musician.SLOTS) throw new IllegalArgumentException("push and weight need one number for each of the " + Musician.SLOTS + " places");
        double most = 60000.0 / tempo / 12 + 0.5;            // a third of a sixteenth: as far as the triplet
        for (double p : pushMs) if (Math.abs(p) > most) throw new IllegalArgumentException(String.format(java.util.Locale.ROOT, "A push of more than %.0f ms at this tempo is past the triplet: a different place in the bar, not a feel: %s", most, p));
    }

    /** A bar played dead on the tick with the usual accents. */
    public Lesson(String name, double tempo, String bar) { this(name, tempo, bar, new double[Musician.SLOTS], usualWeights()); }

    private static double[] usualWeights() {
        double[] w = new double[Musician.SLOTS];
        for (int i = 0; i < w.length; i++) w[i] = Kit.weight(i);
        return w;
    }

    public static Lesson read(Path file) throws IOException {
        String name = file.getFileName().toString(), bar = null;
        double tempo = 96;
        double[] push = new double[Musician.SLOTS], weight = usualWeights();
        for (String line : Files.readAllLines(file)) {
            String s = line.trim();
            if (s.startsWith("name:")) name = s.substring(5).trim();
            else if (s.startsWith("tempo:")) tempo = Double.parseDouble(s.substring(6).trim());
            else if (s.startsWith("bar:")) bar = s.substring(4).trim();
            else if (s.startsWith("push:")) push = numbers(s.substring(5), file);
            else if (s.startsWith("weight:")) weight = numbers(s.substring(7), file);
        }
        if (bar == null) throw new IOException("No 'bar:' line in " + file);
        return new Lesson(name, tempo, bar, push, weight);
    }

    private static double[] numbers(String text, Path file) throws IOException {
        String[] parts = text.trim().split("\\s+");
        if (parts.length != Musician.SLOTS) throw new IOException("Expected " + Musician.SLOTS + " numbers and found " + parts.length + " in " + file);
        double[] out = new double[parts.length];
        for (int i = 0; i < parts.length; i++) out[i] = Double.parseDouble(parts[i]);
        return out;
    }

    /** How many of the sixteen places of a pattern are the same as this lesson's. */
    public int matches(String pattern) {
        int same = 0;
        for (int i = 0; i < Musician.SLOTS; i++) if (pattern.charAt(i) == bar.charAt(i)) same++;
        return same;
    }
}
