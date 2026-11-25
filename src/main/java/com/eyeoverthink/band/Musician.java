package com.eyeoverthink.band;

import java.util.SplittableRandom;

/**
 * A young drummer with a pot, a pan, and a pulse of its own.
 *
 * The pulse. Its hand is unsteady: the pulse wanders and each hit lands a little off. It hears its own hits
 * come back through the room and pulls its pulse toward what it heard. How much it listens is something it
 * learns by trying a little more or a little less and keeping whichever sounded steadier.
 *
 * The tick. A bar is four pulses. A newborn can only feel the pulse itself: one tick to the pulse. To earn
 * the next cut (two ticks to the pulse, then four) it has to be playing the ticks it already has, and those
 * gaps have to sound even enough, for two bars running. Four ticks to each of four pulses is sixteen places
 * in the bar where a hit can go. Listening alone does not get it there. Its hand has to get surer too, and
 * the hand gets surer only by hitting things: that one rule is given to it, not learned.
 *
 * The lesson. A record plays two bars and leaves two bars of room. While the record plays, the drummer is
 * quiet. It takes the first hit it hears as the one, ticks along, and leans its clock toward the record's.
 * Each hit it hears, it puts at the nearest tick it has, and notes whether it was bright or dark. A hit
 * that falls between its ticks it cannot place, so a drummer with one tick to the pulse hears only the
 * pulses of the lesson. If a hit turns out heavier than the one it took for the one, it takes the heavy
 * hit as the one and shifts everything to fit. One hearing can be wrong, so it believes a place only if it
 * has heard it there in at least half the calls so far. That is all it knows of the lesson.
 *
 * The answer. Then it plays two bars of its own. It starts life with the pot on every pulse. Before each
 * answer it changes one place its ticks can reach. Most times it moves one place toward what it just heard;
 * one time in four it tries an idea of its own. If the answer sounded clearly more uneven than its playing
 * usually does, the change was more than its hand could hold, and it takes it back. If the record comes back in before the answer
 * is finished, it stops and listens. If it waits two bars and the record does not come back, it comes in
 * on its own.
 *
 * Together. After the last answer the record plays straight through, and this time the drummer keeps
 * playing. It still puts every hit of the record's at the nearest tick it has and shifts its own tick part
 * of the way toward it, and it leans its pulse toward the record's. Nothing else holds the two clocks
 * together. If a heavy hit lands where it has no tick, it takes that as the one and counts again from there.
 *
 * With another drummer. There is no record. One of them starts; the other takes the first hit it hears as
 * the one, ticks along for a bar, and comes in. From then on each does to the other exactly what it did to
 * the record: shifts its tick part of the way toward the other's hits and leans its pulse toward the
 * other's. If the other's one lands where it has no tick, it moves half way to it; the other is doing the
 * same. Neither is in charge. Each still plays its own pattern.
 *
 * Teaching. A drummer can be the one the lesson comes from. It plays its own pattern for two bars and
 * leaves two bars of room, as the record did, but on its own unsteady clock, and it holds its own time
 * while the other answers. After the last answer it plays on and listens, as it would with any drummer.
 * The one learning treats it exactly as it treated the record. It hears only what its ticks can reach.
 *
 * The click. If there is a click in its ears, that is where it takes its time from. It leans its pulse
 * toward the gaps between clicks and shifts its nearest pulse part of the way toward each click, and it
 * stops taking its time from the other players. It still listens to them for what they play. How strongly
 * it leans is its own listening, so a newborn follows a click badly and a practised drummer well. The click
 * says nothing about where the one is.
 *
 * The click and the others. Its pulse comes from the click, but where exactly it puts its tick can also
 * lean toward the other players, so their hits land together. How much it leans on them is not given to it.
 * It starts at nothing. Every so often it tries leaning a little more or a little less, and keeps whichever
 * made the others' hits land closer to its own ticks. Leaning on a steady player pays; leaning on an
 * unsteady one does not, so it learns whom to follow without being told.
 *
 * Other instruments. The same player can sit at a bass that has one note. Everything about time is the
 * same: its own clock, its listening, its ticks, the click. What differs is what it plays and what it
 * listens for. A newborn bass player has its one note on the one and nothing else. It listens for the dark
 * hits, the pots, and ignores the bright ones. While it plays, every two bars, it changes one place its
 * ticks can reach: most times it puts a note where it has been hearing a pot, or takes one away where it
 * has listened four times and never heard one; one time in four it tries an idea of its own. It keeps a change unless those two bars sounded
 * clearly rougher than its playing usually does.
 *
 * Earning a note. A bass has no frets, so a note can land sharp or flat. A newborn's hand is far off; it
 * gets surer with every note by a fixed rule, as its timing hand does. It hears the pitch of each note it
 * plays, from its own sound. When its last eight notes were close enough to in tune, two bars running, it
 * has earned its second note: the same note an octave up. From then on it stops ignoring the bright hits.
 * It puts the low note where it hears a pot and the high note where it hears a pan.
 *
 * Settling. Each of the sixteen places in its bar has a habit, from 0 to 9. Every round, a place where what
 * it plays agrees with what it hears gets one more settled; a place where it does not gets two less; a
 * place it has just changed starts again from nothing. An idea of its own at a place only goes ahead if
 * chance beats that place's habit, so a place at 9 takes a new idea one time in ten and a place at 0
 * always. Copying what it hears is never held back. So a young player is restless everywhere, and an old
 * one mostly leaves alone what has worked for a long time. Its habits are written into its life.
 *
 * Feel. Where a hit falls against the tick, a little ahead or a little behind, and how hard it is hit, is
 * the feel of a record. When it listens to a record it notices both for every hit it can place, and each
 * place in its bar slowly takes on what it has heard there: a tenth of the way each time. When it plays,
 * it plays each place that far off its tick and that hard, as far as it has heard that place often enough
 * to trust it (twenty hearings for full trust). Its feel goes with it wherever it plays and is written
 * into its life. When it judges how even its own playing is, it allows for the feel it meant.
 *
 * Playing along with a recording. The track is in its headphones, with the click, as a band wears them
 * to practise to a record; the other players it still hears across the room. A real song is a full mix,
 * and to one ear nearly all of it sounds bright. So to the track it listens with two ears: one on the bottom of the sound (below about 150
 * Hz: kick and bass), which it takes for dark hits, and one on the top (above about 5,000 Hz: hats, the
 * crack of a snare), which it takes for bright ones. What lies between goes to neither. Each ear keeps
 * turning itself up or down so that quiet and loud passages are heard alike. It takes notice only of the
 * heavy hits: in the bottom of a song the kick stands above the bass line, and in the top the snare stands
 * above the hats. It plays its own pattern from
 * the first bar, takes its time from the click, and every two bars moves one place toward what it has been
 * hearing, the way the bass player does with the drummers. It takes on the feel of what it hears as well.
 * A bass player playing along wants the whole of the bottom of the track, not just the heavy hits: that is
 * the bass line, and none of the top. With a track on, everyone learns their part from the track and not
 * from the players beside them.
 *
 * Roles. What a player listens for follows from what its hands are on. A drummer at the kit takes the heavy
 * hits from a track: the kick and the snare. A bass player takes the whole bottom of it. A hat player takes
 * the top of it, light hits and all but the faintest, and nothing from the bottom; with no track it listens for the
 * bright hits of the players around it and ignores the dark ones. A hat player has one sound, so its
 * pattern is written with n alone, and its one is a hat. A hat player or a bass player believes a place is
 * part of what it follows only if something was there in at least half of all the bars it has heard; a
 * voice or a stray note comes and goes, but a part comes round every bar. A drummer who moves to the hats brings its old
 * pattern with it: every place it used to hit, it now hits the hat.
 *
 * Influences, and ideas of its own. What a player comes to believe a record plays, it keeps under that
 * record's name. When it is with one record and tries an idea of its own, the idea is borrowed: a place where
 * some other record it has lived with had something that this record has not, and that it does not play. If
 * it can hold it, the place becomes its own. It keeps a place of its own the way it keeps one it was asked
 * for: it does not give it back to what it hears. It can hold two at once; a third pushes out the oldest.
 * With nothing to borrow it tries a place by chance, as before, and that is not kept as its own.
 *
 * What it plays with a song, and its feel for it, it keeps under that song's name, and picks up again when
 * the song comes back. The places that are its own, and the places it was asked for, go with it everywhere.
 *
 * Threes. A bar can be counted in threes: twelve places, three to the pulse, as in a shuffle or a slow
 * blues. A player counts in fours or in threes for life, from the first record it meets. One that counts
 * in threes has one tick to the pulse, or three once its pulse is even; there is nothing between. Players
 * who count the same way can play together and teach one another; a pupil counts as its teacher does. A
 * recording in threes can be played along with by players who count in threes, on any instrument.
 *
 * Swing. A place can sit as far behind its tick as the triplet, a third of a sixteenth, and a hit that far
 * behind a tick is still heard as that place. So a player that takes on a record's feel takes on its swing,
 * up to the triplet. Nothing here knows the word; swing is only named when the feel is reported.
 *
 * Being asked. The bandleader can ask a player for particular places: "the and of two and the and of four,
 * on your high note." A player plays what it is asked, as far as it can: a place its ticks cannot reach
 * yet, or a note it has not earned yet, has to wait until it can. What it has been asked for it keeps. It
 * does not drop it for not hearing it in a track, and it does not try ideas of its own there. The asking
 * is written into its life, so it is still asked next session.
 *
 * The line. A bass player that has earned both its notes listens, while it plays along with a track, for
 * which note the bottom of the song is on just after each tick it can reach. A song's bass line can be
 * longer than a bar. How long, it finds by ear: each note it hears, it compares with the note it heard at
 * the same place one bar, two bars, four bars and eight bars before, and counts how often they were the
 * same. The phrase is the shortest of those that comes round nearly as well as the best of them. Until it
 * has heard enough to say, a phrase is one bar, which is all a player knows of to begin with. It remembers
 * eight bars of places, counted from the song's own first bar; with a shorter phrase, the places that fall
 * together are heard as one. For each place in the phrase it remembers the note it has heard there, how often it heard that note and how often it
 * listened. It believes a place once it has listened four times and heard the same note at least half of
 * them. When it plays a place it believes, its hand goes to that note; at a place it does not yet believe
 * it holds the last note it does. Its high note is the octave above. It counts how many of its notes were
 * the note the song was on at that moment. The line is written into its life.
 *
 * The ending. After its last bar it lands the one of the next bar and lets that hit ring for one pulse of
 * its own clock, whatever its tempo has become.
 *
 * Its listening, its ticks and its pattern are written into its life and are where the next session starts.
 * It is never told a tempo, and it is never shown the lesson. It only hears it.
 */
public final class Musician implements Player, Hits {
    /** How far the pulse wanders each pulse, as a fraction of itself. */
    static final double WANDER = 0.030;
    /** How far a newborn's hit lands from where it was meant, in seconds (one standard deviation). */
    static final double SLIP = 0.004;
    /** The surest a hand ever gets, and how many hits it takes to get most of the way there. */
    static final double SUREST = 0.0012;
    static final double HITS_TO_LEARN = 1500;
    /** How much a newborn listens, before it has learned anything. */
    static final double NEWBORN_LISTENING = 0.15;
    /** How big a change in listening it tries each time. */
    static final double TRY = 0.15;
    /** A newborn keeps a change in how it listens or leans only if things got this much better (0.05 = 5%). An older player tries smaller changes and asks for a smaller gain. */
    static final double CLEARLY = 0.02;
    /** The most gaps or hits it ever weighs before deciding, and how many hits of experience it takes to double its care. */
    static final int MOST_EVIDENCE = 24;
    static final double HITS_TO_SLOW = 1500;
    /** How many heard gaps a newborn plays before judging whether a change in listening helped. */
    static final int JUDGE_EVERY = Ear.MEMORY;
    /** How strongly it settles back toward the tempo its body likes. */
    static final double HOME = 0.02;

    /** Places in a bar: four pulses, at most four ticks each. */
    public static final int SLOTS = 16;
    public static final int PULSES = 4;
    /** Bars the record plays, and bars in an answer. */
    public static final int TURN = 2;
    /** It earns the next cut when the ticks it has are this even (0.02 = those gaps off by 2%). */
    static final double EARN = 0.012;
    /** A change to the pattern is kept if the answer stayed at least this even. */
    static final double HOLD = 0.025;
    /** It can place a heard hit only if it falls within this part of a tick from one of its ticks... */
    static final double REACH = 0.36;
    /** It sets its clock only by hits this close to a tick, as a share of the gap between ticks. */
    static final double STEADY_REACH = 0.12;
    /** ...but never less than this many seconds, and never more than this many. */
    static final double REACH_AT_LEAST = 0.035, REACH_AT_MOST = 0.060;
    /** A change is also kept if the answer was no more than this much rougher than its playing usually is. */
    static final double ROUGHER = 1.2;
    /** A hit this much heavier than the one it took for the one is the real one. */
    static final double HEAVIER = 1.08;
    /** How far it shifts its tick toward a hit it placed. Tapping along is a reflex, not a skill. */
    static final double TAP = 0.5;
    /** How often a change is its own idea and not something it heard. */
    static final int OWN_IDEA_ONE_IN = 4;
    /** How big a change in leaning on the others it tries each time, the most it ever leans, and how many hits it weighs before judging. */
    static final double LEAN_TRY = 0.05, LEAN_MOST = 0.6;
    static final int LEAN_JUDGE_EVERY = 16;
    /** How far off a newborn bass player's notes land, in hundredths of a semitone; the surest a hand gets; and how many notes that takes. */
    static final double TUNE_SLIP = 30, TUNE_SUREST = 6, NOTES_TO_LEARN = 600;
    /** It earns its next note when its last eight notes were this close to in tune on average. */
    static final double TUNE_EARN = 12;
    /** How much of what it hears of a record's feel it takes on each time, how many hearings make it sure, and how far ahead of a tick it can play. */
    static final double ABSORB = 0.1;
    static final int HEARINGS_TO_TRUST = 20;
    static final double MOST_AHEAD = 0.010;              // behind, it can go as far as the triplet: a third of a sixteenth
    /** While playing together, a hit this heavy next to the heaviest it has heard is the one. */
    static final double NEARLY_THE_HEAVIEST = 0.84;
    /** It can see how much farther away the others are than its own hands, so it knows how much later their sound reaches it. */
    static final double FARTHER = Audio.samples(Session.ACROSS - Session.NEAR);
    public static final String FIRST_PATTERN = "p...p...p...p...";
    /** A bar counted in threes has twelve places, three to the pulse. A player counts in fours or in threes for life. */
    public static final int THREES = 12;
    public static final String FIRST_PATTERN_IN_THREES = "p..p..p..p..";
    private final int per, slots;        // places to the pulse (four, or three), and places in its bar
    /** How many places its bar has: sixteen, or twelve if it counts in threes. */
    public int slots() { return slots; }
    public int per() { return per; }
    /** A newborn bass player: its one note, on the one. */
    public static final String FIRST_BASS_PATTERN = "p...............";
    public static final String FIRST_BASS_PATTERN_IN_THREES = "p...........";
    /** A newborn hat player: one hat, on the one. */
    public static final String FIRST_HATS_PATTERN = "n...............";
    public static final String FIRST_HATS_PATTERN_IN_THREES = "n...........";

    private static final byte REST = Kit.REST, POT = Kit.POT, PAN = Kit.PAN;
    private static final long READY = Math.round(Audio.samples(0.012));
    private static final int WAITING = 0, LISTENING = 1, ANSWERING = 2, TOGETHER = 3;

    private final String name;
    private final Clock clock;
    private final Ear ear = new Ear();        // on everyone else
    private final Ear ownEar = new Ear();     // on itself
    private long ownHitsUsed;
    private final Ear clickEar = new Ear();   // on the click in its ears, if there is one
    private long clicksUsed, lastClick = -1, clicksHeard;
    private boolean ticking;
    private double leaning;               // how far it shifts its tick toward the other players when it has a click; learned
    private double farSum; private int farCount;
    private long lastStrike = -1;
    private double ownDelay = Audio.samples(Session.NEAR);   // learned from its own hits
    private double heaviest;                  // the heaviest hit it has heard from the record
    private long ticksTogether; private long placedTogether = -1; private double placedTogetherAt;
    private final Instrument hands;
    private boolean learnsWhilePlaying;   // it changes its pattern while it plays, from what it hears
    private boolean fullMix;              // what it hears is a real recording: listen with two ears
    private final Ear lowEar = new Ear(), highEar = new Ear();
    private final float[] lowPart = new float[Audio.BLOCK], highPart = new float[Audio.BLOCK];
    private double l1, l2, l3, h1, h2, h3, lowLevel = 1e-3, highLevel = 1e-3;
    private long lowUsed, highUsed;
    private static final double LOW_PASS = 1 - Math.exp(-2 * Math.PI * 150.0 / Audio.RATE), HIGH_PASS = 1 - Math.exp(-2 * Math.PI * 5000.0 / Audio.RATE);
    /** How loud a hit in the bottom, or in the top, of a recording must be for a drummer to take notice (the loudest lately is 0.8). */
    static final double HEAVY_LOW = 0.6, HEAVY_HIGH = 0.7;
    /** How loud a hit in the top of a recording must be for a hat player to take notice: much less, since hats are light. */
    static final double HAT_NOTICE = 0.35;
    private static final double LEVEL_FALLS = Math.exp(-1.0 / Audio.samples(4.0));   // an ear forgets how loud things were over a few seconds
    private final boolean hatsOnly;       // a hat player: one bright sound; listens for the bright hits and the top of a track
    private final boolean oneNote;        // a bass player: listens for where to put its notes, and learns while it plays
    private final Bass bass;              // its instrument, if it is a bass player
    private final PitchEar pitchEar = new PitchEar();
    /** The longest phrase it can keep, in bars, and the lengths it tries. */
    public static final int LONGEST = 8;
    private static final int[] LENGTHS = {1, 2, 4, 8};
    private static final int CELLS = LONGEST * SLOTS;
    /** A length comes round "nearly as well" at this much of the best; it needs this many comparisons to count; and it forgets by halves past this many. */
    private static final double NEARLY = 0.85;
    private static final int PHRASE_EVIDENCE = 24, PHRASE_REMEMBERS = 600;
    private final int[] cameRound = new int[LENGTHS.length], compared = new int[LENGTHS.length];
    private final int[] heardNote = new int[CELLS], heardInBar = new int[CELLS];
    private final int[] tally = new int[LineEar.HIGHEST - LineEar.LOWEST + 2];
    private int pooledNote, pooledSame, pooledHeard;
    private static final int LINE_BELIEVE = 4, LINE_DOUBT = 6, LINE_REMEMBERS = 40;
    private LineEar lineEar;              // a bass player's ear on the notes in the bottom of a track
    private final int[] lineNote = new int[CELLS], lineSame = new int[CELLS], lineHeard = new int[CELLS];
    private int phraseFrom;               // which bar of the song's phrase its first bar is
    private int hearingAt = -1, struckAt = -1, strikeCell, withTheSong, againstTheSong;
    private double struckHz = Bass.LOW;
    private int notes = 1;                // how many notes it has earned: 1 (low) or 2 (low and high)
    private byte struck;                  // the note it last played, so it knows what it meant
    private final double[] outOfTune = new double[Ear.MEMORY];
    private int tuneHead, tuneCount, inTuneBars;
    private double centsSum; private int centsCount;
    private final SplittableRandom chance;
    private final double comfortable;
    private final int bars;               // bars it will play in all its answers
    private final int allBars;            // and counting the bars it plays together with someone
    private final int listenBars;         // bars it ticks along quietly before it comes in
    private final boolean withARecord;    // it is following someone whose heavy hit it takes for the one
    private final int teachBars;          // bars of call and room it gives before playing on, when it is the one teaching

    private double listening;             // how strongly it follows what it hears
    private double judgeSum; private int judgeN;
    private boolean upFirst;
    private Setting howItListens, howItLeans;
    private double ownOffSum; private int ownOffCount;       // and how uneven its own playing has been
    private double clickFarSum; private int clickFarCount;   // how far its pulse has been from the click since it last judged its leaning
    private long judgedAt, gapsJudged;
    private long experience;              // hits played in its whole life
    private long hitsUsed;

    private int ticks;                    // ticks to the pulse it can hold: 1, 2 or 4
    private int evenBars;
    private final byte[] pattern = new byte[SLOTS];
    private static final int MOST_INFLUENCES = 8, MOST_OWN = 2;
    private final byte[][] influence = new byte[MOST_INFLUENCES][SLOTS];   // what it believes each other record plays
    private final String[] influenceName = new String[MOST_INFLUENCES];
    private final byte[] own = new byte[SLOTS];                           // places it holds as its own, and since when
    private final int[] ownSince = new int[SLOTS];
    private int influences, triedFrom = -1, borrowed, borrowedKept, ownClock;
    private String borrowedFrom = "";
    private final byte[] askedFor = new byte[SLOTS];  // what the bandleader asked for at each place, or nothing
    private int askedAndPlaying, askedAndWaiting;
    private final int[] habit = new int[SLOTS];   // how settled each place is, 0 to 9
    private int ideasHeldBack, heardWhilePlaying;
    private final int[] barsHeard = new int[SLOTS], barsHeardIn = new int[SLOTS];
    private int triedSlot = -1;
    private byte triedWas;
    private int changesKept, changesTakenBack, copied, ownIdeas, cutShort;

    private int mode = WAITING;
    private int listenTick;               // which tick of the record's two bars its clock is about to reach
    private int placedTick = -1; private double placedAt;
    private final byte[] hearing = new byte[SLOTS];   // what it is making of the bars being played now
    private final double[] weight = new double[SLOTS];  // and how heavy the hit at each place was
    private final byte[] shifted = new byte[SLOTS];
    private int refoundTheOne, metHalfWay;
    private double usual = -1;            // how uneven its answers usually sound
    private final byte[] lesson = new byte[SLOTS];    // what it made of the last two bars it heard
    private boolean hasLesson;
    private final int[][] votes = new int[3][SLOTS];   // how many times this session it heard a pot, or a pan, at each place
    private int callsHeard;
    private int heardPlaced, heardUnplaced;

    private final double[] gaps = new double[Ear.MEMORY];
    private final int[] gapSlots = new int[Ear.MEMORY];
    private final double[] uneven = new double[Ear.MEMORY];
    private final double[] tickUneven = new double[Ear.MEMORY];
    private int gapHead, gapCount, unevenHead, unevenCount, tickHead, tickCount;
    private double roundUneven; private int roundGaps;
    private long waitingSince = -1;
    private int cameInAlone;
    private long mineBefore = -1;         // when the last of its own hits in this answer came back

    private int bar, tickInBar;
    private long strikeAt = -1;
    private byte strikeSound;
    private int strikeSlot;
    private boolean landing;
    private double lastPulse;
    private long endAt = -1;

    private final long[] hits;
    private final int[] hitSlots;
    private final double[] hitPushes;     // how far off its tick it meant each hit to be
    private final double[] feelPush = new double[SLOTS];    // the feel it has taken on: samples after the tick, for each place
    private final double[] feelWeight = new double[SLOTS];  // and how hard
    private final int[] feelHeard = new int[SLOTS];         // how many times it has heard that place on a record
    private double strikePush, struckPush, struckPushBefore;
    private int hitCount;

    /** A drummer for a session with the record: so many answers, then so many bars together. */
    public Musician(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                   int ticksSoFar, String patternSoFar, int answers, int barsTogether, long seed) {
        this(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, answers, barsTogether, seed, true, false, 0, new Kit());
    }

    /** A drummer for a session with another drummer. The one who starts plays from the top; the other comes in after a bar. */
    /** A drummer playing along with a recording, from its first bar, with a click. */
    public static Musician alongARecording(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                                           int ticksSoFar, String patternSoFar, int bars, double startSeconds, long seed) {
        Musician d = new Musician(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, 0, bars, seed, false, true, 0, new Kit());
        d.alongATrackFrom(startSeconds);
        return d;
    }

    /** It will play along with a track in its headphones, from the top, counted in by the click with everyone else. */
    public void alongATrackFrom(double startSeconds) {
        if (oneNote) lineEar = new LineEar();
        clock.startAt(Audio.samples(startSeconds));
        mode = TOGETHER;
        fullMix = true;
        learnsWhilePlaying = true;
    }

    /** A hat player. Call {@link #alongATrackFrom} to have it play along with a track. */
    public static Musician onHats(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                                  int ticksSoFar, String patternSoFar, int bars, long seed) {
        return new Musician(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, 0, bars, seed, false, false, 0, new Hats());
    }

    /** A bass player joining others who are already playing. It comes in after a bar. */
    public static Musician onBass(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                                  int ticksSoFar, String patternSoFar, int notesSoFar, int bars, long seed) {
        Musician m = new Musician(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, 0, bars, seed, false, false, 0, new Bass());
        m.notes = Math.max(1, Math.min(2, notesSoFar));
        return m;
    }

    public static Musician withAnother(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                                      int ticksSoFar, String patternSoFar, int bars, boolean starts, double startSeconds, long seed) {
        Musician d = new Musician(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, 0, bars, seed, false, starts, 0, new Kit());
        if (starts) d.clock.startAt(Audio.samples(startSeconds));
        return d;
    }

    /** A drummer who gives the lesson: so many calls of two bars with two bars of room after each, then so many bars together. */
    public static Musician teaching(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                                   int ticksSoFar, String patternSoFar, int calls, int barsTogether, double startSeconds, long seed) {
        Musician d = new Musician(name, comfortableBpm, experienceSoFar, listeningSoFar, ticksSoFar, patternSoFar, 0, calls * 2 * TURN + barsTogether, seed, false, true, calls * 2 * TURN, new Kit());
        d.clock.startAt(Audio.samples(startSeconds));
        return d;
    }

    private Musician(String name, double comfortableBpm, long experienceSoFar, double listeningSoFar,
                    int ticksSoFar, String patternSoFar, int answers, int barsTogether, long seed, boolean withARecord, boolean starts, int teachBars, Instrument hands) {
        this.hands = hands;
        this.oneNote = hands instanceof Bass;
        this.hatsOnly = hands instanceof Hats;
        this.learnsWhilePlaying = oneNote || hatsOnly;
        this.bass = oneNote ? (Bass) hands : null;
        this.withARecord = withARecord;
        this.teachBars = teachBars;
        this.listenBars = withARecord ? TURN : 1;
        this.name = name;
        this.listening = listeningSoFar;
        this.howItListens = new Setting(listeningSoFar, 0.02, 0.95, (seed & 1) == 0);
        this.howItLeans = new Setting(0, 0, LEAN_MOST, (seed & 2) == 0);
        this.upFirst = (seed & 2) == 0;
        this.slots = patternSoFar.length() == THREES ? THREES : SLOTS;   // a pattern of twelve places counts in threes
        this.per = slots / PULSES;
        this.ticks = ticksSoFar;
        for (int i = 0; i < SLOTS; i++) pattern[i] = Kit.read(i < patternSoFar.length() ? patternSoFar.charAt(i) : '.');
        if (hatsOnly) { for (int i = 0; i < SLOTS; i++) if (pattern[i] != REST) pattern[i] = PAN; pattern[0] = PAN; }   // every place it hit, it now hits the hat
        this.bars = answers * TURN;
        this.allBars = bars + barsTogether;
        this.comfortable = Audio.periodOf(comfortableBpm);
        this.clock = new Clock(comfortable, starts ? Audio.samples(0.25) : 0);
        if (starts) mode = TOGETHER;
        this.chance = new SplittableRandom(seed);
        this.experience = experienceSoFar;
        this.hits = new long[allBars * SLOTS + 8];
        this.hitSlots = new int[hits.length];
        this.hitPushes = new double[hits.length];
        for (int i = 0; i < SLOTS; i++) feelWeight[i] = Kit.weight(i, per);
    }

    @Override public String name() { return name; }

    /** How many of its recent gaps it trusts: one for a newborn, up to its whole memory with practice. */
    int trust() { return (int) Math.min(Ear.MEMORY, 1 + experience / 12); }

    @Override
    public void hear(float[] others, float[] own, float[] click, float[] track, int n, long blockStart) {
        clickEar.listen(click, n, blockStart);
        if (clickEar.hitsEver() != clicksUsed) {
            clicksUsed = clickEar.hitsEver();
            onClick(Math.round(clickEar.hitTime() - (ownDelay - Audio.samples(Session.NEAR))));   // less the moment its ear takes
        }
        if (fullMix) hearARecording(track, n, blockStart);   // the track in its headphones, with two ears
        ear.listen(others, n, blockStart);                   // and the other players, across the room
        if (ear.hitsEver() != hitsUsed) {
            hitsUsed = ear.hitsEver();
            someoneElse(Math.round(ear.hitTime() - FARTHER - ownDelay), ear.hitBright(), ear.hitLoud(), false);   // when it was played, not when it arrived
        }
        if (oneNote) hearItsOwnPitch(own, n, blockStart);
        ownEar.listen(own, n, blockStart);
        if (ownEar.hitsEver() != ownHitsUsed) {              // only its own hits tell it how it is playing
            ownHitsUsed = ownEar.hitsEver();
            long at = ownEar.hitTime();
            if (oneNote) pitchEar.noteBegan(at);
            if (lastStrike >= 0 && at >= lastStrike && at - lastStrike < Audio.samples(0.05))   // how long after it hits does it hear the hit?
                ownDelay += 0.2 * (at - lastStrike - ownDelay);
            // Its own hit, come back. If it meant this one late and the last one early, that is feel, not unsteadiness.
            if (mineBefore >= 0 && (mode == ANSWERING || mode == TOGETHER)) ownGap(at - mineBefore - (struckPush - struckPushBefore));
            mineBefore = at;
        }
    }

    /** Was the note it just played in tune? It finds out from its own sound. */
    private void hearItsOwnPitch(float[] own, int n, long blockStart) {
        pitchEar.listen(own, n, blockStart);
        if (!pitchEar.ready(blockStart + n)) return;
        double meant = struck == PAN ? 2 * struckHz : struckHz;
        double off = Math.abs(PitchEar.cents(pitchEar.hz(meant), meant));
        outOfTune[tuneHead] = off;
        tuneHead = (tuneHead + 1) % Ear.MEMORY;
        if (tuneCount < Ear.MEMORY) tuneCount++;
        centsSum += off; centsCount++;
    }

    /** How far off its hand lands a note now, in hundredths of a semitone. */
    double tuneSlip() { return TUNE_SUREST + (TUNE_SLIP - TUNE_SUREST) * Math.exp(-experience / NOTES_TO_LEARN); }

    /** Two ears on a full mix: the bottom of the sound to one, the top to the other, each kept at a steady level. */
    private void hearARecording(float[] track, int n, long blockStart) {
        double earTakes = ownDelay - Audio.samples(Session.NEAR);   // headphones are no distance away; only the ear's own moment to allow for
        for (int i = 0; i < n; i++) {
            double x = track[i];
            l1 += LOW_PASS * (x - l1); l2 += LOW_PASS * (l1 - l2); l3 += LOW_PASS * (l2 - l3);
            h1 += HIGH_PASS * (x - h1); double x1 = x - h1;
            h2 += HIGH_PASS * (x1 - h2); double x2 = x1 - h2;
            h3 += HIGH_PASS * (x2 - h3); double top = x2 - h3;
            lowLevel = Math.max(lowLevel * LEVEL_FALLS, Math.abs(l3));
            highLevel = Math.max(highLevel * LEVEL_FALLS, Math.abs(top));
            lowPart[i] = (float) (0.8 * l3 / lowLevel);
            highPart[i] = (float) (0.8 * top / highLevel);
        }
        lowEar.listen(lowPart, n, blockStart);
        // A drummer takes notice only of the heavy hits. In the bottom of a song the kick stands above the bass
        // line; in the top the snare stands above the hats. A bass player wants the whole of the bottom: that
        // is the bass line it has come to learn. A hat player wants the whole of the top and none of the bottom.
        if (lowEar.hitsEver() != lowUsed) { lowUsed = lowEar.hitsEver(); if (!hatsOnly && lowEar.hitLoud() >= (oneNote ? 0 : HEAVY_LOW)) someoneElse(Math.round(lowEar.hitTime() - earTakes), false, lowEar.hitLoud(), true); }
        if (lineEar != null && notes >= 2) hearTheLine(track, n, blockStart);
        highEar.listen(highPart, n, blockStart);
        if (highEar.hitsEver() != highUsed) { highUsed = highEar.hitsEver(); if (!oneNote && highEar.hitLoud() >= (hatsOnly ? HAT_NOTICE : HEAVY_HIGH)) someoneElse(Math.round(highEar.hitTime() - earTakes), true, highEar.hitLoud(), true); }
    }

    /** Which note is the bottom of the track on, at the place that just went by? */
    private void hearTheLine(float[] track, int n, long blockStart) {
        lineEar.listen(track, n, blockStart);
        if (!lineEar.ready(blockStart + n)) return;
        int at = hearingAt;
        hearingAt = -1;
        if (!lineEar.hear() || at < 0) return;
        double heard = lineEar.midi();
        if (at == struckAt) {                                // it played here: was it on the note the song was on?
            double apart = heard - LineEar.midiOf(struckHz);
            apart -= 12 * Math.rint(apart / 12);             // the same note an octave away is the same note
            if (Math.abs(apart) <= 0.5) withTheSong++; else againstTheSong++;
        }
        int cell = (at + phraseFrom * SLOTS) % CELLS, note = (int) Math.round(heard), inBar = at / SLOTS + 1, slot = at % SLOTS;
        for (int k = 0; k < LENGTHS.length; k++) {           // does the line come round after this many bars?
            int then = Math.floorMod(inBar - LENGTHS[k], LONGEST) * SLOTS + slot;
            if (heardInBar[then] != inBar - LENGTHS[k]) continue;
            compared[k]++;
            if (heardNote[then] == note) cameRound[k]++;
        }
        if (compared[LENGTHS.length - 1] > PHRASE_REMEMBERS) for (int k = 0; k < LENGTHS.length; k++) { compared[k] /= 2; cameRound[k] /= 2; }
        int here = (inBar % LONGEST) * SLOTS + slot;
        heardNote[here] = note; heardInBar[here] = inBar;
        if (lineHeard[cell] == 0) { lineNote[cell] = note; lineSame[cell] = 1; lineHeard[cell] = 1; return; }
        lineHeard[cell]++;
        if (note == lineNote[cell]) lineSame[cell]++;
        else if (lineHeard[cell] >= LINE_DOUBT && lineSame[cell] * 2 < lineHeard[cell]) { lineNote[cell] = note; lineSame[cell] = 1; lineHeard[cell] = 1; }
        if (lineHeard[cell] > LINE_REMEMBERS) { lineHeard[cell] /= 2; lineSame[cell] /= 2; }
    }

    /** How many bars it believes the phrase has: the shortest length that comes round nearly as well as the best. */
    public int phraseBars() {
        double best = 0;
        for (int k = 0; k < LENGTHS.length; k++) if (compared[k] >= PHRASE_EVIDENCE) best = Math.max(best, cameRound[k] / (double) compared[k]);
        for (int k = 0; k < LENGTHS.length; k++) if (compared[k] >= PHRASE_EVIDENCE && cameRound[k] >= NEARLY * best * compared[k]) return LENGTHS[k];
        return 1;
    }

    /** Everything it has heard at this place of the phrase, taken together. True if it believes a note there; then pooledNote says which. */
    private boolean believesTheNoteAt(int place, int bars) {
        java.util.Arrays.fill(tally, 0);
        pooledHeard = 0; pooledSame = 0; pooledNote = 0;
        for (int c = place % (bars * SLOTS); c < CELLS; c += bars * SLOTS) {
            if (lineHeard[c] == 0) continue;
            pooledHeard += lineHeard[c];
            int t = Math.max(0, Math.min(tally.length - 1, lineNote[c] - LineEar.LOWEST));
            tally[t] += lineSame[c];
            if (tally[t] > pooledSame) { pooledSame = tally[t]; pooledNote = lineNote[c]; }
        }
        return pooledHeard >= LINE_BELIEVE && pooledSame * 2 >= pooledHeard;
    }

    /** Where its hand goes for this place in the phrase: the note it believes is there, or the last one before it that it believes. */
    private double lineHz(int cell) {
        int bars = phraseBars(), places = bars * SLOTS;
        for (int back = 0; back < places; back++)
            if (believesTheNoteAt(Math.floorMod(cell - back, places), bars)) return LineEar.hzOf(pooledNote);
        return Bass.LOW;                                     // it knows no line yet: its open string
    }

    /** A click in its ears. */
    private void onClick(long at) {
        clicksHeard++;
        if (lastClick >= 0) clock.pullToward(at - lastClick, listening);     // the gap between two clicks is the pulse
        lastClick = at;
        if (mode == WAITING || !ticking || landing || endAt >= 0) return;   // nothing to shift until it is counting
        // Which of its pulses was that click nearest to? Shift that way.
        double tick = clock.period() / ticks;
        int index = mode == LISTENING ? listenTick : tickInBar;
        int past = ((index % ticks) + ticks) % ticks;                        // ticks since its last pulse
        double pulseBefore = clock.nextBeat() - past * tick, pulseAfter = pulseBefore + ticks * tick;
        if (past == 0) pulseBefore -= ticks * tick;
        double off = Math.abs(at - pulseBefore) < Math.abs(at - pulseAfter) ? at - pulseBefore : at - pulseAfter;
        if (past == 0 && Math.abs(at - clock.nextBeat()) < Math.abs(off)) off = at - clock.nextBeat();
        clickFarSum += Math.abs(off); clickFarCount++;
        clock.nudge(TAP * off);
    }

    /** True if it has a click to take its time from. */
    private boolean onAClick() { return clicksHeard > 0; }

    /** A hit it did not make. */
    private void someoneElse(long at, boolean bright, double loud, boolean fromTrack) {
        byte kind = bright ? PAN : POT;
        double tick = clock.period() / ticks;
        if (loud > heaviest) heaviest = loud;
        if (mode == TOGETHER) {
            // With a track on, everyone learns their part from the track and not from the players beside them.
            if (learnsWhilePlaying && (fromTrack || !fullMix)) noteWhatItHears(at, kind, loud, tick, fromTrack);
            if (!fromTrack) alongside(at, loud, tick);       // a track will not lean back, and its hits have a feel of their own
            return;
        }
        if (fromTrack) return;
        if (mode == ANSWERING) {
            boolean firstBar = bar % TURN == 0 && tickInBar > 0;
            if (firstBar) return;                            // too soon to be the record coming back; let it go
            if (bar % TURN != 0) {                           // the record is back before it finished
                cutShort++;
                strikeAt = -1;
                bar = (bar / TURN + 1) * TURN; tickInBar = 0;
            }
            endOfAnswer();
            if (bar >= bars) {                               // that was its last answer: this is the record's one, play on from it
                mode = TOGETHER;
                clock.startAt(at + tick); tickInBar = 1; ticksTogether = 1;
                placedTogether = 0; placedTogetherAt = at;
                return;
            }
            mode = WAITING;
        }
        if (mode == WAITING) {                               // the first hit after the quiet is the one
            mode = LISTENING;
            clock.startAt(at + tick);
            listenTick = 1;
            java.util.Arrays.fill(hearing, REST);
            java.util.Arrays.fill(weight, 0);
            hearing[0] = kind; weight[0] = loud;
            placedTick = 0; placedAt = at; heardPlaced = 1; heardUnplaced = 0;
            return;
        }
        int best = -1; double off = Double.MAX_VALUE;        // which of its ticks was that nearest to?
        for (int back = 0; back <= 2; back++) {
            double d = at - (clock.nextBeat() - back * tick);
            if (Math.abs(d) < Math.abs(off)) { off = d; best = listenTick - back; }
        }
        if (best < 0 || best >= listenBars * PULSES * ticks || Math.abs(off) > Math.min(Math.max(REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST))) {
            heardUnplaced++;
            if (withARecord && loud > HEAVIER * weight[0] && listenTick > PULSES * ticks / 2) {
                // It fits none of its ticks and it is heavier than what it took for the one. So this is the one,
                // of the record's second bar, and its ticks were in the wrong place. Start again from here.
                clock.startAt(at + tick);
                listenTick = PULSES * ticks + 1;
                java.util.Arrays.fill(hearing, REST); java.util.Arrays.fill(weight, 0);
                hearing[0] = kind; weight[0] = loud;
                placedTick = PULSES * ticks; placedAt = at;
                refoundTheOne++;
            }
            return;
        }
        heardPlaced++;
        int place = (best % (PULSES * ticks)) * (per / ticks);
        hearing[place] = kind;
        if (loud > weight[place]) weight[place] = loud;
        if (withARecord && feelHeard[place] < 1_000_000) {    // take on a little of how the record played that place
            feelPush[place] += ABSORB * (off - feelPush[place]);
            // How hard was it hit? A pan is a quieter thing than a pot, so the same loudness means a harder hit.
            double hard = loud / heaviest / (kind == PAN ? Kit.PAN_AGAINST_POT : 1.0);
            if (heaviest > 0) feelWeight[place] += ABSORB * (Math.min(1.0, hard) - feelWeight[place]);
            feelHeard[place]++;
        }
        if (onAClick()) return;                              // it has heard what was played; its time comes from the click
        // A place the record plays late on purpose is not the record running late. It sets its clock by how far
        // the hit is from where it would play that place itself, and not at all by one that is still far off.
        double meant = knownFeelOf(place);
        if (Math.abs(off - meant) > steadyReach(tick)) return;
        clock.nudge(TAP * (off - meant));
        if (best > placedTick) {                             // quiet and only listening, it leans on every hit it can place
            leanToward(at - meant - placedAt, best - placedTick);
            placedTick = best; placedAt = at - meant;
        }
    }

    /** While it plays: which of its places was that hit nearest to? A bass player only notes the dark ones. */
    private void noteWhatItHears(long at, byte kind, double loud, double tick, boolean fromTrack) {
        if (oneNote && kind != POT && notes < 2) return;     // a bass player with one note has no use for the bright hits
        if (hatsOnly && kind != PAN) return;                 // and a hat player none for the dark ones
        int barTicks = PULSES * ticks, back = 0;
        double off = Double.MAX_VALUE;
        for (int b = 0; b <= 2; b++) {
            double d = at - (clock.nextBeat() - b * tick);
            if (Math.abs(d) < Math.abs(off)) { off = d; back = b; }
        }
        if (Math.abs(off) > Math.min(Math.max(REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST))) return;
        int place = (((tickInBar - back) % barTicks + barTicks) % barTicks) * (per / ticks);
        hearing[place] = kind;
        if (barsHeardIn[place] != bar) { barsHeardIn[place] = bar; barsHeard[place]++; }   // in how many of these bars was something there?
        heardWhilePlaying++;
        if (fromTrack) {                                     // a recording has a feel worth taking on; another learner's unsteady hand does not
            feelPush[place] += ABSORB * (off - feelPush[place]);
            feelWeight[place] += ABSORB * (Math.min(1.0, loud / 0.8) - feelWeight[place]);
            if (feelHeard[place] < 1_000_000) feelHeard[place]++;
        }
    }

    /** A hit of the record's while it is playing too. */
    private void alongside(long at, double loud, double tick) {
        if (landing) return;                                 // its last hit is already on its way
        if (bar < teachBars) return;                         // while it is giving the lesson it holds its own time
        if (onAClick()) { leanOnTheOthers(at, tick); return; }   // its pulse comes from the click; only its tick leans on the others
        int barTicks = PULSES * ticks;
        int back = 0; double off = Double.MAX_VALUE;
        for (int b = 0; b <= 2; b++) {
            double d = at - (clock.nextBeat() - b * tick);
            if (Math.abs(d) < Math.abs(off)) { off = d; back = b; }
        }
        boolean heavy = loud > NEARLY_THE_HEAVIEST * heaviest;
        if (Math.abs(off) > Math.min(Math.max(REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST))) {
            if (heavy && !withARecord) {                     // the other drummer's one, where it has no tick: meet it half way
                double thisOne = clock.nextBeat() - tickInBar * tick, nextOne = thisOne + barTicks * tick;
                double away = Math.abs(at - thisOne) < Math.abs(at - nextOne) ? at - thisOne : at - nextOne;
                clock.nudge(0.5 * away);
                metHalfWay++;
            } else if (heavy) {                              // the record's one, where it has no tick: count again from here
                if (tickInBar > barTicks / 2) bar++;
                if (bar >= allBars) {                        // that was the record's last hit: land now, late as it is
                    clock.startAt(at); tickInBar = 0; strikeAt = -1;
                    refoundTheOne++;
                    return;
                }
                clock.startAt(at + tick); tickInBar = 1;
                ticksTogether++; placedTogether = ticksTogether - 1; placedTogetherAt = at;
                strikeAt = -1;
                refoundTheOne++;
            }
            return;
        }
        int inBar = ((tickInBar - back) % barTicks + barTicks) % barTicks;
        if (heavy && inBar != 0) {                           // on one of its ticks, but that tick is the one
            if (inBar > barTicks / 2 && tickInBar - back >= 0) bar++;
            tickInBar = ((tickInBar - inBar) % barTicks + barTicks) % barTicks;
            refoundTheOne++;
        }
        double meant = knownFeelOf((((tickInBar - back) % barTicks + barTicks) % barTicks) * (per / ticks));   // as when listening: late on purpose is not late
        clock.nudge(TAP * (off - meant));
        long which = ticksTogether - back;
        if (placedTogether < 0) { placedTogether = which; placedTogetherAt = at - meant; }
        else if (which - placedTogether >= ticks) {
            leanToward(at - meant - placedTogetherAt, which - placedTogether);
            placedTogether = which; placedTogetherAt = at - meant;
        }
    }

    /**
     * Lean its pulse toward the pulse it hears between two of someone else's hits. The ear is a little
     * quicker on a heavy hit than a light one, so a short gap says little about the pulse. While it is
     * playing too, it only leans on gaps at least a pulse long.
     */
    /** How close to where it would play a place a hit must be for it to set its clock by it. */
    private double steadyReach(double tick) { return Math.min(Math.max(STEADY_REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST)); }

    private void leanToward(double gap, long ticksApart) {
        clock.pullToward(gap / ticksApart * ticks, listening);
    }

    /** With a click: shift its tick a learned part of the way toward a hit of someone else's, and learn from how far off it was. */
    private void leanOnTheOthers(long at, double tick) {
        double off = Double.MAX_VALUE;
        int back = 0, barTicks = PULSES * ticks;
        for (int b = 0; b <= 2; b++) {
            double d = at - (clock.nextBeat() - b * tick);
            if (Math.abs(d) < Math.abs(off)) { off = d; back = b; }
        }
        // Someone who plays a place late on purpose is not behind. Measure them against where it would play that
        // place itself, feel and all; players who grew up on the same records then agree.
        off -= feelOf((((tickInBar - back) % barTicks + barTicks) % barTicks) * (per / ticks));
        if (Math.abs(off) > Math.min(Math.max(REACH * tick, Audio.samples(REACH_AT_LEAST)), Audio.samples(REACH_AT_MOST))) return;
        clock.nudge(leaning * off);
        farSum += Math.abs(off);
        if (++farCount < Math.min(MOST_EVIDENCE, LEAN_JUDGE_EVERY * (1 + experience / HITS_TO_SLOW))) return;
        // How things stand, all in the same measure: how far the others land from its ticks, how far its own
        // pulse has been from the click, and how uneven its own playing has sounded. Leaning harder brings the
        // others closer, but past a point it only makes its own time rougher, and it has to answer for all three.
        double farNow = farSum / farCount + (clickFarCount == 0 ? 0 : clickFarSum / clickFarCount) + (ownOffCount == 0 ? 0 : ownOffSum / ownOffCount);
        farSum = 0; farCount = 0; clickFarSum = 0; clickFarCount = 0; ownOffSum = 0; ownOffCount = 0;
        leaning = howItLeans.heard(farNow, LEAN_TRY * stepNow(), CLEARLY);
    }

    /** A gap between two of its own hits, come back through the room. */
    private void ownGap(double gap) {
        double tick = clock.period() / ticks;
        int span = (int) Math.max(1, Math.round(gap / tick)) * (per / ticks);
        if (gapCount > 0) {
            int before = (gapHead - 1 + Ear.MEMORY) % Ear.MEMORY;
            double expected = gaps[before] * span / gapSlots[before];
            double off = Math.abs(gap - expected) / expected;
            ownOffSum += Math.abs(gap - expected); ownOffCount++;
            uneven[unevenHead] = off;
            unevenHead = (unevenHead + 1) % Ear.MEMORY;
            if (unevenCount < Ear.MEMORY) unevenCount++;
            roundUneven += off; roundGaps++;
            if (span == per / ticks) {
                tickUneven[tickHead] = off;
                tickHead = (tickHead + 1) % Ear.MEMORY;
                if (tickCount < Ear.MEMORY) tickCount++;
            }
        }
        gaps[gapHead] = gap; gapSlots[gapHead] = span;
        gapHead = (gapHead + 1) % Ear.MEMORY;
        if (gapCount < Ear.MEMORY) gapCount++;
        gapsJudged++;
        if (gapCount > 1) { judgeSum += uneven[(unevenHead - 1 + Ear.MEMORY) % Ear.MEMORY]; judgeN++; }

        clock.pullToward(feltPulse(trust()), listening);
    }

    /** The pulse it feels in its last few gaps, however many places each one spanned. */
    private double feltPulse(int k) {
        int use = Math.min(k, gapCount);
        double time = 0; int places = 0;
        for (int j = 1; j <= use; j++) {
            int at = (gapHead - j + Ear.MEMORY) % Ear.MEMORY;
            time += gaps[at]; places += gapSlots[at];
        }
        return time / places * (per);
    }

    double unevenness() {
        if (unevenCount == 0) return 0;
        double sum = 0;
        for (int j = 0; j < unevenCount; j++) sum += uneven[j];
        return sum / unevenCount;
    }

    /** How uneven its finest ticks have sounded lately, or 1 if it has not played enough of them to know. */
    double tickUnevenness() {
        if (tickCount < Ear.MEMORY) return 1;
        double sum = 0;
        for (int j = 0; j < Ear.MEMORY; j++) sum += tickUneven[j];
        return sum / Ear.MEMORY;
    }

    /** How far its hand slips now, in seconds. */
    double slip() { return SUREST + (SLIP - SUREST) * Math.exp(-experience / HITS_TO_LEARN); }

    /** Did the last change in listening make it sound steadier? If not, undo it and try the other way. */
    private void judge() {
        judgedAt = gapsJudged;
        double roughNow = judgeN == 0 ? 0 : judgeSum / judgeN;
        judgeSum = 0; judgeN = 0;
        listening = howItListens.heard(roughNow, TRY * stepNow(), CLEARLY);
    }

    /**
     * One thing a player is learning by trial: how much to listen, or how much to lean. It hears how things
     * are as it is, tries a change for a while, goes back and hears how things are again, and only then
     * decides. A player warms up during a session, so anything tried later would look better than what came
     * before it for that reason alone; comparing the trial with the stretches on both sides of it takes
     * that out. It keeps the change only if the trial was clearly better than those two together.
     */
    private static final class Setting {
        private final double least, most;
        private double settled, tried, way, before = -1, during = -1;
        private boolean trying;

        private static final double PROBE = 0.25;
        private boolean warm;

        /** {@code upFirst}: which way it tries first this session. Left to chance, so no direction is favoured. */
        Setting(double settled, double least, double most, boolean upFirst) { this.settled = settled; this.least = least; this.most = most; this.way = upFirst ? 1 : -1; }

        double settled() { return settled; }

        /** A stretch has ended and this is how it went (smaller is better). What should the setting be for the next stretch? */
        double heard(double cost, double step, double clearly) {
            if (!warm) { warm = true; return settled; }                           // the first stretch of a session is warming up; it proves nothing
            if (trying) { during = cost; trying = false; return settled; }       // back to how it was, to hear that again
            if (during >= 0) {                                                   // it has heard before, during and after
                if (during < (before + cost) / 2 * (1 - clearly)) settled = Math.max(least, Math.min(most, settled + way * step)); else way = -way;
                during = -1;
            }
            before = cost;
            tried = Math.max(least, Math.min(most, settled + way * PROBE));   // try a change big enough to hear; move only a step if it helped
            trying = true;
            return tried;
        }
    }

    /** How many gaps it weighs before it decides anything about itself: more as it gets older. */
    private int evidenceNow() { return (int) Math.min(MOST_EVIDENCE, JUDGE_EVERY * (1 + experience / HITS_TO_SLOW)); }

    /** How big a change it tries, against what a newborn tries: smaller as it gets older. */
    private double stepNow() { return 1.0 / (1 + experience / HITS_TO_SLOW); }

    /** The top of a bar in its answer: where it finds out what it has earned. */
    private void barLine() {
        if (ticks < per && tickUnevenness() < EARN) {
            if (++evenBars >= 2) { ticks = per == 3 ? 3 : ticks * 2; evenBars = 0; tickCount = 0; }
        } else evenBars = 0;
        if (bar % TURN == 0 && judgeN >= 2) judge();         // every two bars it takes stock of how it has sounded
        if (oneNote && notes < 2 && tuneCount == Ear.MEMORY) {          // has it been in tune enough to earn its next note?
            double off = 0;
            for (int j = 0; j < Ear.MEMORY; j++) off += outOfTune[j] / Ear.MEMORY;
            if (off < TUNE_EARN) { if (++inTuneBars >= 2) notes = 2; } else inTuneBars = 0;
        }
        if (mode == TOGETHER && learnsWhilePlaying && bar % TURN == 0 && bar < allBars) {   // it learns while it plays, two bars at a time
            endOfAnswer();
            if (oneNote || hatsOnly) believeByTheBar(); else believe();
            java.util.Arrays.fill(hearing, REST);
            java.util.Arrays.fill(barsHeard, 0);
            if (chance.nextInt(OWN_IDEA_ONE_IN) != 0 && copyOnePlace()) copied++;
            else if (ownIdea()) ownIdeas++;
        }
        if (mode == ANSWERING && bar % TURN == 0) {
            endOfAnswer();
            if (bar < bars) mode = WAITING;                  // quiet now, until the record's one
            else if (allBars > bars) { mode = TOGETHER; ticksTogether = 0; placedTogether = -1; }   // play on, with the record
        }
    }

    /** Could it hold the change it tried? */
    private void endOfAnswer() {
        double rough = roundGaps > 0 ? roundUneven / roundGaps : 0;
        int step = per / ticks;                   // every place it can reach gets more settled, or less
        for (int s = 0; s < slots; s += step) {
            if (s == triedSlot) habit[s] = 0;
            else if (!hasLesson || !wants(s)) habit[s] = Math.min(9, habit[s] + 1);
            else habit[s] = Math.max(0, habit[s] - 2);
        }
        if (triedSlot >= 0) {
            if (roundGaps > 0 && rough > HOLD && usual >= 0 && rough > ROUGHER * usual) { pattern[triedSlot] = triedWas; changesTakenBack++; }
            else { changesKept++; if (triedFrom >= 0) makeItsOwn(triedSlot); }
            triedSlot = -1; triedFrom = -1;
        }
        if (roundGaps > 0) usual = usual < 0 ? rough : usual + 0.3 * (rough - usual);
        roundUneven = 0; roundGaps = 0;
    }

    /** The record has played its two bars. Keep what it made of them, pick one change, and come in. */
    private void startAnswer() {
        int one = 0;                                        // was the heaviest hit where it thought the one was?
        for (int s = 1; s < slots; s++) if (weight[s] > weight[one]) one = s;
        if (weight[one] <= HEAVIER * weight[0]) one = 0;
        if (one != 0) {                                      // no: the heavy hit is the one, and it is already that far into the bar
            for (int s = 0; s < slots; s++) shifted[s] = hearing[(s + one) % slots];
            System.arraycopy(shifted, 0, hearing, 0, slots);
            refoundTheOne++;
        }
        believe();
        mode = ANSWERING;
        tickInBar = ((slots - one) % slots) / (per / ticks);
        mineBefore = -1; gapCount = 0;
        if (hasLesson && chance.nextInt(OWN_IDEA_ONE_IN) != 0 && copyOnePlace()) copied++;
        else if (ownIdea()) ownIdeas++;
    }

    /** One hearing can be wrong. It believes a place only if it has heard it there in at least half its hearings so far. */
    private void believe() {
        callsHeard++;
        for (int s = 0; s < slots; s++) {
            if (hearing[s] != REST && !(oneNote && notes < 2 && hearing[s] == PAN) && !(hatsOnly && hearing[s] == POT)) votes[hearing[s]][s]++;   // a bass player with one note has no use for the bright hits
            byte likelier = votes[POT][s] >= votes[PAN][s] ? POT : PAN;
            lesson[s] = 2 * votes[likelier][s] >= callsHeard ? likelier : REST;
        }
        hasLesson = true;
    }

    /**
     * A hat player or a bass player takes in everything in its part of the sound, and a real song is full of
     * things there that are not hats or bass: the edge of a voice, a stray note. What belongs to the part
     * comes round every bar; the rest comes and goes. So these players count bars, not hearings: they
     * believe a place only if something was there in at least half of all the bars they have listened to.
     */
    private void believeByTheBar() {
        callsHeard += TURN;
        for (int s = 0; s < slots; s++) {
            if (hearing[s] != REST && !(oneNote && notes < 2 && hearing[s] == PAN) && !(hatsOnly && hearing[s] == POT)) votes[hearing[s]][s] += Math.min(TURN, barsHeard[s]);
            byte likelier = votes[POT][s] >= votes[PAN][s] ? POT : PAN;
            lesson[s] = 2 * votes[likelier][s] >= callsHeard ? likelier : REST;
        }
        hasLesson = true;
    }

    /** Move one place it can reach toward what it heard. False if it already plays everything it heard. */
    private boolean copyOnePlace() {
        int step = per / ticks, differ = 0;
        for (int s = step; s < slots; s += step) if (wants(s)) differ++;
        if (differ == 0) return false;
        int pick = chance.nextInt(differ);
        for (int s = step; s < slots; s += step) {
            if (!wants(s) || pick-- != 0) continue;
            triedSlot = s; triedWas = pattern[s]; triedFrom = -1;
            pattern[s] = lesson[s];
        }
        return true;
    }

    /** A place where it heard something other than what it plays. A pulse is never left empty. */
    private boolean wants(int s) {
        if (askedFor[s] != REST && pattern[s] == askedFor[s]) return false;   // it was asked for this; it stays
        if (own[s] != REST && pattern[s] == own[s]) return false;             // this is its own; it stays
        if (lesson[s] == pattern[s]) return false;
        if (!oneNote && !hatsOnly) return !(lesson[s] == REST && s % (per) == 0);
        // A bass player or a hat player adds a note where it believes there is one to follow, and takes one away
        // where it has listened at least four times and heard something there less than one time in four.
        return lesson[s] != REST || (callsHeard >= 4 && 4 * (votes[POT][s] + votes[PAN][s]) < callsHeard);
    }

    /** Change one place its ticks can reach, for no reason but to try it. The one stays on the pot. */
    private boolean ownIdea() {
        int step = per / ticks;
        int found = 0, at = -1, from = -1;                   // first, something to borrow from another record it has lived with
        for (int k = 0; k < influences; k++)
            for (int s = step; s < slots; s += step) {
                byte want = influence[k][s];
                if (want == REST || want == pattern[s] || (hasLesson && want == lesson[s]) || askedFor[s] != REST || own[s] != REST) continue;
                if ((hatsOnly && want != PAN) || (oneNote && want == PAN && notes < 2)) continue;
                if (chance.nextInt(++found) == 0) { at = s; from = k; }
            }
        if (at >= 0) {
            if (chance.nextInt(10) < habit[at]) { ideasHeldBack++; return false; }
            triedSlot = at; triedWas = pattern[at]; triedFrom = from;
            pattern[at] = influence[from][at];
            borrowed++;
            return true;
        }
        int slot = step * chance.nextInt(slots / step);
        if (slot == 0 || own[slot] != REST) return false;
        triedFrom = -1;
        if (askedFor[slot] != REST && pattern[slot] == askedFor[slot]) return false;   // not where it has been asked for something
        if (chance.nextInt(10) < habit[slot]) { ideasHeldBack++; return false; }   // too settled there to bother
        byte was = pattern[slot];
        if (hatsOnly) pattern[slot] = was != REST ? REST : PAN;
        else if (oneNote) pattern[slot] = was != REST ? REST : notes >= 2 && chance.nextBoolean() ? PAN : POT;
        else pattern[slot] = was == REST ? (chance.nextBoolean() ? POT : PAN)
                      : was == POT ? PAN
                      : slot % (per) == 0 ? POT : REST;
        triedSlot = slot; triedWas = was;
        return true;
    }

    @Override
    public void play(float[] out, int n, long blockStart) {
        long blockEnd = blockStart + n;
        if (mode == WAITING) {
            if (waitingSince < 0) waitingSince = blockStart;
            else if (blockStart - waitingSince > TURN * PULSES * clock.period()) {   // nobody came: come in on its own
                clock.startAt(blockStart);
                System.arraycopy(lesson, 0, hearing, 0, slots);
                java.util.Arrays.fill(weight, 0);
                cameInAlone++;
                mode = LISTENING; listenTick = listenBars * PULSES * ticks;
            }
        } else waitingSince = -1;
        // It gets ready a moment before each tick, so a hand that slips early can land early, not only late.
        while (mode != WAITING && !landing && endAt < 0 && strikeAt < 0 && clock.dueBefore(blockEnd + READY)) {
            if (mode == LISTENING) {
                if (listenTick < listenBars * PULSES * ticks) {   // ticking along, quietly
                    listenTick++;
                    step();
                    continue;
                }
                if (withARecord) startAnswer();
                else {                                       // a bar has gone by: come in with the other drummer
                    mode = TOGETHER;
                    bar = listenBars; tickInBar = 0;
                    ticksTogether = 0; placedTogether = -1;
                    mineBefore = -1; gapCount = 0;
                }
            } else if (tickInBar == 0) {
                barLine();
                if (mode == WAITING) break;
            }
            int slot = tickInBar * (per / ticks);
            boolean last = bar >= allBars;                   // the one after its last bar: where it lands
            boolean leavingRoom = bar < teachBars && bar % (2 * TURN) >= TURN;   // the two bars it leaves for the answer
            byte sound = last ? POT : leavingRoom ? REST : pattern[slot];
            if (lineEar != null && notes >= 2 && fullMix && mode == TOGETHER && !last && !lineEar.busy()) { lineEar.noteAt(Math.round(clock.nextBeat())); hearingAt = bar * SLOTS + slot; }
            if (sound != REST) {
                strikePush = last ? 0 : feelOf(slot);
                long meant = Math.round(clock.nextBeat() + strikePush + chance.nextGaussian() * Audio.samples(slip()));
                strikeAt = Math.max(meant, blockStart);
                strikeSound = sound;
                strikeSlot = last ? allBars * slots : bar * slots + slot;
                landing = last;
            }
            if (last) break;
            step();
            if (mode == TOGETHER) ticksTogether++;
            if (++tickInBar >= PULSES * ticks) { tickInBar = 0; bar++; }
        }
        for (int i = 0; i < n; i++) {
            if (strikeAt >= 0 && blockStart + i >= strikeAt) {
                if (oneNote) {
                    struckHz = fullMix && notes >= 2 ? lineHz((strikeSlot / slots * SLOTS + strikeSlot % slots + phraseFrom * SLOTS) % CELLS) : Bass.LOW;
                    bass.tune(struckHz);
                    bass.detune(chance.nextGaussian() * tuneSlip());
                    struck = strikeSound; struckAt = strikeSlot / slots * SLOTS + strikeSlot % slots;   // the line is kept sixteen places to the bar, used or not
                }
                hands.strike(strikeSound, (float) (weightOf(strikeSlot % slots) * (0.88 + 0.12 * chance.nextDouble())));
                if (hitCount < hits.length) { hits[hitCount] = blockStart + i; hitPushes[hitCount] = strikePush; hitSlots[hitCount++] = strikeSlot; }
                struckPushBefore = struckPush; struckPush = strikePush;
                experience++;
                lastStrike = blockStart + i;
                strikeAt = -1;
                if (landing) { lastPulse = clock.period(); endAt = blockStart + i + Math.round(lastPulse); }
            }
            out[i] += hands.next();
        }
    }

    /** How sure it is of the feel of a place: nothing until it has heard it, everything after twenty hearings. */
    /**
     * How far after its tick it knew a place to sit when this session began. What it is taking in right now
     * is no use for telling a feel from an error: if its own tempo is off, every later place in the bar looks
     * late, and it would take its own mistake for the record's feel and stop correcting it. What it brought
     * with it from other days is the record's.
     */
    private double knownFeelOf(int place) {
        return Math.max(-Audio.samples(MOST_AHEAD), Math.min(clock.period() / 12, (feelCameWith[place] - feelCameWith[0]) * trustIn(place)));
    }
    private final double[] feelCameWith = new double[SLOTS];

    private double trustIn(int place) { return Math.min(1.0, feelHeard[place] / (double) HEARINGS_TO_TRUST); }

    /** How far after its tick it means to play a place, in samples. It cannot get more than a moment ahead. */
    private double feelOf(int place) {
        // Feel is where a place sits against the one. If everything it heard was early or late by the same amount,
        // that was the click and the record being a little apart, not a feel.
        return Math.max(-Audio.samples(MOST_AHEAD), Math.min(clock.period() / 12, (feelPush[place] - feelPush[0]) * trustIn(place)));   // as far behind as the triplet
    }

    /** How hard it means to hit a place: the usual accent, bent toward what it has heard. The one stays the heaviest. */
    private double weightOf(int place) {
        if (place == 0) return 1.0;
        double t = trustIn(place);
        return Math.min(0.9, Kit.weight(place, per) * (1 - t) + feelWeight[place] * t);
    }

    /** On to its next tick, with the wander of its hand and the pull of the tempo its body likes. */
    private void step() {
        ticking = true;
        clock.advance(1.0 / ticks);
        clock.drift(chance.nextGaussian() * WANDER / Math.sqrt(ticks));
        clock.pullToward(comfortable, HOME / ticks);
    }

    /** True once its last hit has landed and rung for one pulse. */
    public boolean done(long now) { return endAt >= 0 && now >= endAt; }

    public String pattern() { return write(pattern); }
    /** What it believes the lesson is, from every call it has heard this session. */
    public String lessonAsHeard() { return write(lesson); }
    private String write(byte[] b) {
        char[] c = new char[slots];
        for (int i = 0; i < slots; i++) c[i] = Kit.write(b[i]);
        return new String(c);
    }

    @Override public int hitCount() { return hitCount; }
    @Override public long hitAt(int i) { return hits[i]; }
    public int hitSlot(int i) { return hitSlots[i]; }
    /** How far off its tick it meant that hit to be, in samples. */
    public double hitPush(int i) { return hitPushes[i]; }
    /** The first place, counted from the start, that belongs to the bars played together with the record. */
    public int togetherFrom() { return Math.max(bars, teachBars) * slots; }
    public double tempoNow() { return Audio.bpmOf(clock.period()); }
    public double lastPulse() { return lastPulse; }
    public long experience() { return experience; }
    /** How strongly it follows what it hears. A change it is only trying out does not count yet. */
    public double listening() { return howItListens.settled(); }
    /** How far it shifts its tick toward the other players when it has a click, from 0 to 0.6. */
    public double leaning() { return howItLeans.settled(); }
    /** Where its life says its leaning had got to. */
    public void leaning(double soFar) { leaning = Math.max(0, Math.min(LEAN_MOST, soFar)); howItLeans = new Setting(leaning, 0, LEAN_MOST, upFirst); }
    public int ticks() { return ticks; }
    public int changesKept() { return changesKept; }
    public int changesTakenBack() { return changesTakenBack; }
    public int copied() { return copied; }
    public int ownIdeas() { return ownIdeas; }
    public int cutShort() { return cutShort; }
    public int refoundTheOne() { return refoundTheOne; }
    public int cameInAlone() { return cameInAlone; }
    public String plays() { return hands.describe(); }

    /** Which bar of the song its first bar is, counted from the song's own first bar. */
    public void phraseFrom(int barOfTheSong) { phraseFrom = Math.floorMod(barOfTheSong, LONGEST); }

    /** What it has heard about how long a phrase is, for its life: for one, two, four and eight bars, how often the line came round of how often it compared. */
    public String phrase() {
        StringBuilder b = new StringBuilder();
        for (int k = 0; k < LENGTHS.length; k++) b.append(k == 0 ? "" : " ").append(cameRound[k]).append('/').append(compared[k]);
        return b.toString();
    }

    /** Where its life says that had got to. */
    public void phrase(String soFar) {
        String[] parts = soFar.trim().split("\\s+");
        for (int k = 0; k < LENGTHS.length && k < parts.length; k++) {
            String[] f = parts[k].split("/");
            if (f.length != 2) continue;
            cameRound[k] = Integer.parseInt(f[0]); compared[k] = Integer.parseInt(f[1]);
        }
    }

    private static final String[] NOTE_NAMES = {"C", "C#", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B"};
    static String nameOf(int midi) { return NOTE_NAMES[Math.floorMod(midi, 12)] + (midi / 12 - 1); }

    /** The line it has learned, for its life: for each place in eight bars, the note, how often it heard that note, how often it listened. */
    public String line() {
        StringBuilder b = new StringBuilder();
        for (int c = 0; c < CELLS; c++) b.append(c == 0 ? "" : " ").append(lineHeard[c] == 0 ? "-" : lineNote[c] + "/" + lineSame[c] + "/" + lineHeard[c]);
        return b.toString();
    }

    /** Where its life says its line had got to. */
    public void line(String soFar) {
        String[] parts = soFar.trim().split("\\s+");
        for (int c = 0; c < CELLS && c < parts.length; c++) {
            String[] f = parts[c].split("/");
            if (f.length != 3) continue;
            lineNote[c] = Integer.parseInt(f[0]); lineSame[c] = Integer.parseInt(f[1]); lineHeard[c] = Integer.parseInt(f[2]);
        }
    }

    /** The line as it believes it, bar by bar for as many bars as it believes the phrase has, at the places its ticks reach: a note name, or a dot where it is not sure. */
    public String lineInWords() {
        StringBuilder b = new StringBuilder();
        int bars = phraseBars();
        for (int bar = 0; bar < bars; bar++)
            for (int slot = 0; slot < slots; slot += per / ticks) {
                if (bar + slot > 0) b.append(slot == 0 ? " | " : " ");
                b.append(believesTheNoteAt(bar * SLOTS + slot, bars) ? nameOf(pooledNote) : ".");
            }
        return b.toString();
    }
    /** How many places in the phrase it believes it knows the note for, of how many its ticks reach. */
    public int linePlaces() {
        int k = 0, bars = phraseBars();
        for (int bar = 0; bar < bars; bar++) for (int slot = 0; slot < slots; slot += per / ticks) if (believesTheNoteAt(bar * SLOTS + slot, bars)) k++;
        return k;
    }
    public int linePlacesInReach() { return phraseBars() * PULSES * ticks; }
    /** Of the notes it played while the song's note was clear: how many were that note, and how many were not. */
    public int withTheSong() { return withTheSong; }
    public int againstTheSong() { return againstTheSong; }

    /** Its feel, for its life: for each place, milliseconds after the tick, how hard, and how often heard. */
    public String feel() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < slots; i++)
            b.append(i == 0 ? "" : " ").append(String.format(java.util.Locale.ROOT, "%.1f/%.2f/%d", feelPush[i] * 1000.0 / Audio.RATE, feelWeight[i], feelHeard[i]));
        return b.toString();
    }

    /** Where its life says its feel had got to. */
    public void feel(String soFar) {
        String[] parts = soFar.trim().split("\\s+");
        for (int i = 0; i < slots && i < parts.length; i++) {
            String[] f = parts[i].split("/");
            if (f.length != 3) continue;
            feelPush[i] = Audio.samples(Double.parseDouble(f[0]) / 1000.0);
            feelWeight[i] = Double.parseDouble(f[1]);
            feelHeard[i] = Integer.parseInt(f[2]);
            feelCameWith[i] = feelPush[i];
        }
    }

    /** How far after the tick, in milliseconds, it means to play a place now. */
    /** How much its sixteenths swing as it plays them now: 50 is straight, 66.7 is on the triplet, -1 if it plays nothing off the eighths. */
    public double swing() {
        if (per != 4) return -1;                             // swing is a matter of sixteenths
        for (int s = 0; s < slots; s++) feelNow[s] = feelMs(s);
        return Hearing.swing(pattern(), feelNow, 60.0 * Audio.RATE / clock.period());
    }
    private final double[] feelNow = new double[SLOTS];

    public double feelMs(int place) { return feelOf(place) * 1000.0 / Audio.RATE; }
    /** How hard it means to hit a place now, from 0 to 1. */
    public double feelWeight(int place) { return weightOf(place); }
    /**
     * What the bandleader has asked it for: sixteen places, p or n where something is asked and a dot where
     * nothing is. It takes up each one it can play now and leaves the rest waiting. Call after its ticks and
     * notes are known.
     */
    public void asked(String places) {
        int step = per / ticks;
        askedAndPlaying = 0; askedAndWaiting = 0;
        for (int i = 0; i < slots && i < places.length(); i++) {
            byte want = Kit.read(places.charAt(i));
            if (want == REST) continue;
            if (hatsOnly) want = PAN;                                    // a hat player has one sound to give
            boolean canReach = i % step == 0, hasTheNote = !(oneNote && want == PAN && notes < 2);
            if (!canReach || !hasTheNote || i == 0) { askedAndWaiting++; continue; }   // the one is not up for asking
            askedFor[i] = want;
            pattern[i] = want;
            habit[i] = 9;
            askedAndPlaying++;
        }
    }
    /** How many of the places it was asked for it is playing, and how many are waiting until it can. */
    public int askedAndPlaying() { return askedAndPlaying; }
    public int askedAndWaiting() { return askedAndWaiting; }

    /** It held a borrowed place: now it is its own. It can hold only so many; the oldest gives way. */
    private void makeItsOwn(int s) {
        own[s] = pattern[s]; ownSince[s] = ++ownClock;
        borrowedKept++;
        borrowedFrom = influenceName[triedFrom];
        int held = 0, oldest = -1;
        for (int i = 0; i < slots; i++) if (own[i] != REST) { held++; if (oldest < 0 || ownSince[i] < ownSince[oldest]) oldest = i; }
        if (held > MOST_OWN) own[oldest] = REST;
    }

    /** What it believes another record plays, by that record's name. Not the record it is with now. */
    public void influence(String name, String bar) {
        if (influences == MOST_INFLUENCES) return;
        influenceName[influences] = name;
        for (int i = 0; i < slots && i < bar.length(); i++) influence[influences][i] = Kit.read(bar.charAt(i));
        influences++;
    }

    /** The places it holds as its own, for its life: p or n at each, a dot elsewhere. */
    public String own() { return write(own); }
    /** Where its life says they were. */
    public void own(String places) {
        for (int i = 0; i < slots && i < places.length(); i++) {
            byte was = Kit.read(places.charAt(i));
            if (was == REST || i % (per / ticks) != 0) continue;
            pattern[i] = was; own[i] = was; ownSince[i] = ++ownClock;   // what is its own goes with it to every song
        }
    }
    /** How many ideas it borrowed this session, how many of them it held, and which record the last one it held came from. */
    public int borrowed() { return borrowed; }
    public int borrowedKept() { return borrowedKept; }
    public String borrowedFrom() { return borrowedFrom; }

    /** How settled each of its sixteen places is, a digit from 0 to 9 for each. */
    public String habits() {
        char[] c = new char[slots];
        for (int i = 0; i < slots; i++) c[i] = (char) ('0' + habit[i]);
        return new String(c);
    }
    /** Where its life says its habits had got to. */
    public void habits(String soFar) {
        for (int i = 0; i < slots && i < soFar.length(); i++) habit[i] = Math.max(0, Math.min(9, soFar.charAt(i) - '0'));
    }
    public int ideasHeldBack() { return ideasHeldBack; }
    /** How many hits of what it was playing along with it managed to put at one of its places. */
    public int heardWhilePlaying() { return heardWhilePlaying; }
    /** What it believes it has been hearing, from every two bars so far this session. */
    public String believes() { return write(lesson); }
    /** How many notes a bass player has earned. */
    public int notes() { return notes; }
    /** How far out of tune its notes were this session on average, by its own ear, in hundredths of a semitone. */
    public double outOfTuneCents() { return centsCount == 0 ? 0 : centsSum / centsCount; }
}
