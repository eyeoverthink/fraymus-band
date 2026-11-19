package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Something to learn from: one bar of sixteen places, and the tempo it is played at.
 * p is the pot, n is the pan, a dot is nothing. Write your own in a text file:
 *
 *   name: first lesson
 *   tempo: 96
 *   bar: p..pn...p.p.n..n
 */
public record Lesson(String name, double tempo, String bar) {
    public Lesson {
        if (bar.length() != Musician.SLOTS) throw new IllegalArgumentException("A bar has " + Musician.SLOTS + " places, this one has " + bar.length() + ": " + bar);
        if (!bar.matches("[pn.]+")) throw new IllegalArgumentException("A bar is written with p, n and dots only: " + bar);
        if (tempo < 40 || tempo > 200) throw new IllegalArgumentException("Tempo should be between 40 and 200: " + tempo);
    }

    public static Lesson read(Path file) throws IOException {
        String name = file.getFileName().toString(), bar = null;
        double tempo = 96;
        for (String line : Files.readAllLines(file)) {
            String s = line.trim();
            if (s.startsWith("name:")) name = s.substring(5).trim();
            else if (s.startsWith("tempo:")) tempo = Double.parseDouble(s.substring(6).trim());
            else if (s.startsWith("bar:")) bar = s.substring(4).trim();
        }
        if (bar == null) throw new IOException("No 'bar:' line in " + file);
        return new Lesson(name, tempo, bar);
    }

    /** How many of the sixteen places of a pattern are the same as this lesson's. */
    public int matches(String pattern) {
        int same = 0;
        for (int i = 0; i < Musician.SLOTS; i++) if (pattern.charAt(i) == bar.charAt(i)) same++;
        return same;
    }
}
