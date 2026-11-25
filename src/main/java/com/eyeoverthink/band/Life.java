package com.eyeoverthink.band;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A player's lineage: a plain text file that is read before every session and added to after it.
 * The lines at the top say where the player is now. The session lines below are what it lived, in order.
 * Nothing is written here that the player did not play.
 */
public final class Life {
    private final Path file;
    private final List<String> lines = new ArrayList<>();
    private String name = "unnamed";
    private long hitsPlayed;
    private int sessions;
    private double listening = Musician.NEWBORN_LISTENING;
    private int ticks = 1;
    private double leaning;
    private int notes = 1;
    private String habits = "0000000000000000";
    private String feel = "";
    private final java.util.Map<String, String> lineFrom = new java.util.LinkedHashMap<>(), phraseFrom = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, String> heardFrom = new java.util.LinkedHashMap<>();
    private String own = "................";
    private final java.util.Map<String, String> patternWith = new java.util.LinkedHashMap<>(), feelWith = new java.util.LinkedHashMap<>();
    private String lineFromSomewhere = "", phraseFromSomewhere = "", lastSong;
    private String asked = "................";
    private String sounds = "a pot (p) and a pan (n)";
    private String pattern = Musician.FIRST_PATTERN;

    private Life(Path file) { this.file = file; }

    public static Life open(Path file, String nameIfNew) throws IOException { return open(file, nameIfNew, Musician.FIRST_PATTERN); }

    /** The same, for a player whose first pattern is not a drummer's. */
    public static Life open(Path file, String nameIfNew, String patternIfNew) throws IOException {
        Life life = new Life(file);
        life.pattern = patternIfNew;
        if (Files.exists(file)) {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                life.lines.add(line);
                String s = line.trim();
                if (s.startsWith("name:")) life.name = s.substring(5).trim();
                else if (s.startsWith("listens to itself:")) life.listening = Double.parseDouble(s.substring(18).trim());
                else if (s.startsWith("leans on the others:")) life.leaning = Double.parseDouble(s.substring(20).trim());
                else if (s.startsWith("asked for:")) life.asked = s.substring(10).trim();
                else if (s.startsWith("heard from \"") && s.indexOf("\":") > 0) life.heardFrom.put(s.substring(12, s.indexOf("\":")), s.substring(s.indexOf("\":") + 2).trim());
                else if (s.startsWith("plays with \"") && s.indexOf("\":") > 0) life.patternWith.put(s.substring(12, s.indexOf("\":")), s.substring(s.indexOf("\":") + 2).trim());
                else if (s.startsWith("feel with \"") && s.indexOf("\":") > 0) life.feelWith.put(s.substring(11, s.indexOf("\":")), s.substring(s.indexOf("\":") + 2).trim());
                else if (s.startsWith("its own:")) life.own = s.substring(8).trim();
                else if (s.startsWith("phrase from \"") && s.indexOf("\":") > 0) life.phraseFrom.put(s.substring(13, s.indexOf("\":")), s.substring(s.indexOf("\":") + 2).trim());
                else if (s.startsWith("line from \"") && s.indexOf("\":") > 0) life.lineFrom.put(s.substring(11, s.indexOf("\":")), s.substring(s.indexOf("\":") + 2).trim());
                else if (s.startsWith("phrase:")) life.phraseFromSomewhere = s.substring(7).trim();     // how steps 21 and 22 wrote it, with no song named
                else if (s.startsWith("line:")) life.lineFromSomewhere = s.substring(5).trim();
                else if (s.startsWith("feel:")) life.feel = s.substring(5).trim();
                else if (s.startsWith("habits:")) life.habits = s.substring(7).trim();
                else if (s.startsWith("notes:")) life.notes = Integer.parseInt(s.substring(6).trim());
                else if (s.startsWith("ticks to the pulse:")) life.ticks = Integer.parseInt(s.substring(19).trim());
                else if (s.startsWith("pattern:")) life.pattern = s.substring(8).trim();
                else if (s.startsWith("session ")) {
                    int song = s.indexOf("played along with \"");
                    if (song >= 0 && s.indexOf('"', song + 19) > 0) life.lastSong = s.substring(song + 19, s.indexOf('"', song + 19));
                    life.sessions++;
                    int at = s.indexOf("hits=");
                    if (at < 0) at = s.indexOf("pulses=") + 2;      // how the first sessions were written
                    if (at >= 2) life.hitsPlayed += Long.parseLong(s.substring(at + 5).split("[ ,]")[0]);
                }
            }
        } else {
            life.name = nameIfNew;
            life.lines.add("name: " + nameIfNew);
            life.lines.add("age: 0");
            life.lines.add("");
        }
        return life;
    }

    public String name() { return name; }
    public int sessions() { return sessions; }
    /** Every hit this player has ever played, across all sessions. This is its experience. */
    public long hitsPlayed() { return hitsPlayed; }
    /** How strongly it has learned to follow what it hears, from 0 (not at all) to 1. */
    public double listening() { return listening; }
    /** How finely it can cut its pulse: 1, 2 or 4 ticks. */
    public int ticks() { return ticks; }
    /** How far it has learned to lean on the other players when it has a click, from 0 to 0.6. */
    public double leaning() { return leaning; }
    /** Note where its leaning has got to. It is written with the next session. */
    public void leaning(double now) { leaning = now; set("leans on the others", String.format(Locale.ROOT, "%.2f", now)); }

    /**
     * What a bass player has heard about how long a phrase is in this song: for one, two, four and eight bars,
     * how often the line came round of how often it compared. Nothing, for a song it has not played along with.
     */
    public String phrase(String song) { return phraseFrom.getOrDefault(song, song.equals(lastSong) ? phraseFromSomewhere : ""); }
    public void phrase(String song, String now) { namedNow(); phraseFrom.put(song, now); set("phrase from \"" + song + "\"", now); }

    /**
     * A bass player's line for this song: for each place in eight bars, the note, how often it heard that
     * note, how often it listened. What it takes from one song it keeps under that song's name, apart from
     * what it took from any other.
     */
    public String line(String song) { return lineFrom.getOrDefault(song, song.equals(lastSong) ? lineFromSomewhere : ""); }
    /** Note where its line for this song has got to. It is written with the next session. */
    public void line(String song, String now) { namedNow(); lineFrom.put(song, now); set("line from \"" + song + "\"", now); }
    /** The songs it has a line from. */
    public java.util.Set<String> songs() { namedNow(); return java.util.Collections.unmodifiableSet(lineFrom.keySet()); }

    /** A line written before lines were kept by song belongs to the last song it played along with. */
    private void namedNow() {
        if (lastSong != null && !lineFromSomewhere.isEmpty() && !lineFrom.containsKey(lastSong)) {
            lineFrom.put(lastSong, lineFromSomewhere);
            for (int i = 0; i < lines.size(); i++) if (lines.get(i).trim().startsWith("line:")) lines.set(i, "line from \"" + lastSong + "\": " + lineFromSomewhere);
        }
        if (lastSong != null && !phraseFromSomewhere.isEmpty() && !phraseFrom.containsKey(lastSong)) {
            phraseFrom.put(lastSong, phraseFromSomewhere);
            for (int i = 0; i < lines.size(); i++) if (lines.get(i).trim().startsWith("phrase:")) lines.set(i, "phrase from \"" + lastSong + "\": " + phraseFromSomewhere);
        }
        lineFromSomewhere = ""; phraseFromSomewhere = "";
    }

    /** What it believes each record it has lived with plays, by the record's name. */
    public java.util.Map<String, String> heard() { return java.util.Collections.unmodifiableMap(heardFrom); }
    /** Note what it now believes this record plays. It is written with the next session. */
    public void heard(String record, String bar) { if (bar.matches("\\.*")) return; heardFrom.put(record, bar); set("heard from \"" + record + "\"", bar); }

    /**
     * What it played with this song when it last played along with it, and the feel it had for it. A player
     * that goes from one song to another and back picks up where it left off with each. For a song it has not
     * met, what it plays now.
     */
    public String playsWith(String song) { String was = patternWith.get(song); return was != null && was.length() == pattern.length() ? was : pattern; }
    public String feelWith(String song) { return feelWith.getOrDefault(song, feel); }
    public void playsWith(String song, String bar, String itsFeel) {
        patternWith.put(song, bar); feelWith.put(song, itsFeel);
        set("plays with \"" + song + "\"", bar); set("feel with \"" + song + "\"", itsFeel);
    }

    /** The places it holds as its own: p or n at each, a dot elsewhere. */
    public String own() { return own; }
    public void own(String places) { if (places.equals(own)) return; own = places; set("its own", places); }

    /** What the bandleader has asked it for: p or n at each place asked, a dot elsewhere. */
    public String asked() { return asked; }

    /** The bandleader asks for something new. It replaces whatever was asked before and is written down at once. */
    public void ask(String places, String inWords) throws IOException {
        if (places.length() != Musician.SLOTS || !places.matches("[pn.]+")) throw new IllegalArgumentException("Ask with sixteen places of p, n and dots: " + places);
        asked = places;
        set("asked for", places);
        lines.add("asked by the bandleader: " + places + (inWords == null || inWords.isBlank() ? "" : " (\"" + inWords.trim() + "\")"));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /** Its feel: for each place, milliseconds after the tick / how hard / how often heard. Empty until it has one. */
    public String feel() { return feel; }
    /** Note where its feel has got to. It is written with the next session. */
    public void feel(String now) { feel = now; set("feel", now); }

    /** How settled each of its sixteen places is, a digit from 0 to 9 for each. */
    public String habits() { return habits; }
    /** Note where its habits have got to. It is written with the next session. */
    public void habits(String now) { habits = now; set("habits", now); }

    /** How many notes a bass player has earned. */
    public int notes() { return notes; }
    /** Note how many it has now. It is written with the next session. */
    public void notes(int now) { notes = now; set("notes", Integer.toString(now)); }

    /** What its hands are on. A player who has moved to a different instrument has that written into its life. */
    public void plays(String what) { if (!what.equals(sounds)) { sounds = what; if (lines.stream().anyMatch(l -> l.trim().startsWith("sounds:"))) set("sounds", what); } }

    /** The sixteen places of its bar: p for the pot, n for the pan, a dot for nothing. */
    public String pattern() { return pattern; }

    private void set(String key, String value) {
        String line = key + ": " + value;
        for (int i = 0; i < lines.size(); i++)
            if (lines.get(i).trim().startsWith(key + ":")) { lines.set(i, line); return; }
        int blank = lines.indexOf("");
        lines.add(blank < 0 ? lines.size() : blank, line);
    }

    /** A session playing along with a recording. */
    public void rememberAlong(String recording, int bars, int hits, double unsteadyMs, double listeningNow, int ticksNow,
                              String patternNow, int landedWithIt, double apartMs) throws IOException {
        sessions++;
        hitsPlayed += hits;
        listening = listeningNow; ticks = ticksNow; pattern = patternNow;
        set("listens to itself", String.format(Locale.ROOT, "%.2f", listeningNow));
        set("ticks to the pulse", Integer.toString(ticksNow));
        set("pattern", patternNow);
        lines.add(String.format(Locale.ROOT,
            "session %d: played along with \"%s\" for %d bars, hits=%d, unsteadiness %.1f ms, listening %.2f, ticks to the pulse %d, pattern %s, %d of its hits landed with a hit in the recording, %.0f ms apart",
            sessions, recording, bars, hits, unsteadyMs, listeningNow, ticksNow, patternNow, landedWithIt, apartMs));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /** A session in which this player gave the lesson. */
    public void rememberTaught(String other, int calls, int barsTogether, int hits, double unsteadyMs, double listeningNow, int ticksNow,
                               String patternNow, int otherMatches) throws IOException {
        sessions++;
        hitsPlayed += hits;
        listening = listeningNow; ticks = ticksNow; pattern = patternNow;
        set("listens to itself", String.format(Locale.ROOT, "%.2f", listeningNow));
        set("ticks to the pulse", Integer.toString(ticksNow));
        set("pattern", patternNow);
        lines.add(String.format(Locale.ROOT,
            "session %d: played its pattern for %s, %d calls and %d bars together, hits=%d, unsteadiness %.1f ms, listening %.2f, ticks to the pulse %d, pattern %s, and %s ended like it in %d of 16 places",
            sessions, other, calls, barsTogether, hits, unsteadyMs, listeningNow, ticksNow, patternNow, other, otherMatches));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /** A session played with another drummer and no record. */
    public void rememberTogether(String other, int bars, int hits, double unsteadyMs, double listeningNow, int ticksNow,
                                 String patternNow, int hitsTogether, double apartMs) throws IOException {
        sessions++;
        hitsPlayed += hits;
        listening = listeningNow; ticks = ticksNow; pattern = patternNow;
        lines.removeIf(l -> l.trim().startsWith("sound:"));
        if (lines.stream().noneMatch(l -> l.trim().startsWith("sounds:"))) set("sounds", sounds);
        set("listens to itself", String.format(Locale.ROOT, "%.2f", listeningNow));
        set("ticks to the pulse", Integer.toString(ticksNow));
        set("pattern", patternNow);
        lines.add(String.format(Locale.ROOT,
            "session %d: played %d bars with %s, no record, hits=%d, unsteadiness %.1f ms, listening %.2f, ticks to the pulse %d, pattern %s, ",
            sessions, bars, other, hits, unsteadyMs, listeningNow, ticksNow, patternNow)
            + (hitsTogether == 0 ? "never landed near each other" : String.format(Locale.ROOT, "%.0f ms apart", apartMs)));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public void remember(String lesson, int answers, int hits, double startedBpm, double endedBpm, double earlyUnsteadyMs, double lateUnsteadyMs,
                         double listeningNow, int ticksNow, String patternNow, int matches, int barsTogether, double apartMs) throws IOException {
        sessions++;
        hitsPlayed += hits;
        listening = listeningNow; ticks = ticksNow; pattern = patternNow;
        lines.removeIf(l -> l.trim().startsWith("sound:"));
        if (lines.stream().noneMatch(l -> l.trim().startsWith("sounds:"))) set("sounds", sounds);
        set("listens to itself", String.format(Locale.ROOT, "%.2f", listeningNow));
        set("ticks to the pulse", Integer.toString(ticksNow));
        set("pattern", patternNow);
        lines.add(String.format(Locale.ROOT,
            "session %d: heard \"%s\", answers=%d, hits=%d, tempo %.1f to %.1f bpm, unsteadiness %.1f ms at the start, %.1f ms by the end, listening %.2f, ticks to the pulse %d, pattern %s, like the lesson in %d of 16 places",
            sessions, lesson, answers, hits, startedBpm, endedBpm, earlyUnsteadyMs, lateUnsteadyMs, listeningNow, ticksNow, patternNow, matches)
            + (barsTogether == 0 ? "" : apartMs < 0 ? String.format(Locale.ROOT, ", %d bars with the record but never landed near it", barsTogether)
                                                    : String.format(Locale.ROOT, ", %d bars with the record, %.0f ms apart", barsTogether, apartMs)));
        if (file.getParent() != null) Files.createDirectories(file.getParent());
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
