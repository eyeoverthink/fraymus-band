package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * A player's lineage: a plain text file that is read before every session and added to after it.
 * What is in it was lived, in order. Nothing is written here that the player did not play.
 */
public final class Life {
    private final Path file;
    private final List<String> lines = new ArrayList<>();
    private String name = "unnamed";
    private int age;
    private long pulsesPlayed;
    private int sessions;
    private double listening = Drummer.NEWBORN_LISTENING;

    private Life(Path file) { this.file = file; }

    public static Life open(Path file, String nameIfNew) throws IOException {
        Life life = new Life(file);
        if (Files.exists(file)) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                life.lines.add(line);
                String s = line.trim();
                if (s.startsWith("name:")) life.name = s.substring(5).trim();
                else if (s.startsWith("age:")) life.age = Integer.parseInt(s.substring(4).trim());
                else if (s.startsWith("listens to itself:")) life.listening = Double.parseDouble(s.substring(18).trim());
                else if (s.startsWith("session ")) {
                    life.sessions++;
                    int at = s.indexOf("pulses=");
                    if (at >= 0) life.pulsesPlayed += Long.parseLong(s.substring(at + 7).split("[ ,]")[0]);
                }
            }
        } else {
            life.name = nameIfNew;
            life.lines.add("name: " + nameIfNew);
            life.lines.add("age: 0");
            life.lines.add("sound: one, a hand on a pot");
            life.lines.add(String.format(java.util.Locale.ROOT, "listens to itself: %.2f", life.listening));
            life.lines.add("");
        }
        return life;
    }

    public String name() { return name; }
    public int age() { return age; }
    public int sessions() { return sessions; }
    /** Every pulse this player has ever played, across all sessions. This is its experience. */
    public long pulsesPlayed() { return pulsesPlayed; }
    /** How strongly it has learned to follow what it hears, from 0 (not at all) to 1. */
    public double listening() { return listening; }

    public void remember(int pulses, double startedBpm, double endedBpm, double earlyUnsteadyMs, double lateUnsteadyMs, double listeningNow) throws IOException {
        sessions++;
        pulsesPlayed += pulses;
        listening = listeningNow;
        String habit = String.format(java.util.Locale.ROOT, "listens to itself: %.2f", listeningNow);
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) if (lines.get(i).trim().startsWith("listens to itself:")) { lines.set(i, habit); found = true; }
        if (!found) lines.add(Math.min(3, lines.size()), habit);
        lines.add(String.format(java.util.Locale.ROOT,
            "session %d: pulses=%d, tempo %.1f to %.1f bpm, unsteadiness %.1f ms at the start, %.1f ms by the end, listening %.2f",
            sessions, pulses, startedBpm, endedBpm, earlyUnsteadyMs, lateUnsteadyMs, listeningNow));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
