# Fraymus Band

**New here, or picking this up again? Read [HANDOFF.md](HANDOFF.md) first.** It has the whole picture:
what this is, how the work is done, what has been tried, and what comes next.

A band of players built on the computer. Each one owns an instrument, keeps its own clock, plays live
into a shared room, and has to grow up before it can play well. Java, no interpreted code, every buffer
claimed before the first sample.

So far there are three players: two drummers with a pot and a pan, and a bass player with two notes. There
is a record for them to learn from and a click for their ears.

## What the drummer does

**The pulse.** Nobody sets its tempo. Its hand is unsteady: the pulse wanders and each hit lands a little
off. The room carries its hits back 30 ms later. It pulls its pulse toward what it heard, and it learns
how much to listen by trying a little more or a little less and keeping whichever sounded steadier.

**The tick.** A bar is four pulses. A newborn feels only the pulse: one tick to the pulse. To earn the
next cut (two ticks, then four) it has to be playing the ticks it already has, and those gaps have to
sound even enough to its own ear for two bars running. Four ticks on four pulses is sixteen places in
the bar.

**The lesson.** A record plays two bars and leaves two bars of room, on its own clock. It does not
listen and it does not wait. While it plays, the drummer is quiet: it takes the first hit as the one, ticks
along, and leans its clock toward the record's. Every hit it hears it puts at the nearest tick it has, and
notes whether it was bright (pan) or dark (pot). A hit that falls between its ticks it cannot place. So a
drummer with one tick to the pulse hears only the pulses of the lesson, and one with four hears all of it.
It is never shown the lesson. It only hears it.

**The answer.** Then it plays two bars of its own. It starts life with the pot on every pulse. Before each
answer it changes one place its ticks can reach: most times one place toward what it just heard, one time
in four an idea of its own. If the answer sounded clearly more uneven than its playing usually does, the
change was more than its hand could hold and it takes it back. The one of the bar stays on the pot, and a
pulse is never left empty. If the record comes back before it has finished, it stops and listens.

**Together.** After the last answer the record plays straight through and the drummer keeps playing. It
still puts every hit of the record's at the nearest tick it has, shifts its own tick part of the way
toward it, and leans its pulse toward the record's. Nothing else holds the two clocks together. If a
heavy hit lands where it has no tick, it takes that as the one and counts again from there.

**With another drummer.** There is no record. One starts; the other takes the first hit it hears as the
one, ticks along for a bar, and comes in. From then on each does to the other what it did to the record:
shifts its tick part of the way toward the other's hits and leans its pulse toward the other's. If the
other's one lands where it has no tick, it moves half way to it, and the other is doing the same. Neither
is in charge. Each keeps its own pattern and its own life file.

**Teaching.** A drummer can be the one the lesson comes from. It plays its own pattern for two bars and
leaves two bars of room, as the record did, but on its own unsteady clock. The one learning treats it
exactly as it treated the record and hears only what its ticks can reach. Because one hearing of an
unsteady teacher can be wrong, a learner believes a place only if it has heard it there in at least half
the calls so far. After the last answer the two play on together.

**The click.** A click track at a steady tempo, counting one bar in. It is not in the room: every player
hears it in its own ears, the way a band wears a click, and it is not on the recording. A drummer with a
click takes its time from it. It leans its pulse toward the gaps between clicks, shifts its nearest pulse
part of the way toward each click, and stops taking its time from the other players, though it still
listens to them for what they play. How strongly it leans is its own listening, so a newborn follows a
click badly and a practised drummer well. The click says nothing about where the one is.

**The click and the others.** With a click, a drummer's pulse comes from the click, but where exactly it
puts its tick can also lean toward the other players, so their hits land together. How much it leans on
them is not given to it. It starts at nothing. Every sixteen hits it tries leaning a little more or a
little less and keeps whichever made the others' hits land closer to its own ticks. That is written into
its life too.

**The bass player.** The same kind of player at a different instrument: a fretless bass. It is born with
one note, a low E.
Everything about time is the same: its own clock, its listening, its ticks, the click. A newborn bass
player has its one note on the one and nothing else. It listens for the dark hits, the pots, and ignores
the bright ones. While it plays, every two bars, it changes one place its ticks can reach. Most times it
puts a note where it has been hearing a pot, or takes one away where it has listened four times and never
heard one. One time in four it tries an idea of its own. It keeps a change unless those two bars sounded
clearly rougher than its playing usually does. Nobody tells it to follow the drummers' pots; that is
simply what it listens for.

**Earning a note.** A bass has no frets, so a note can land sharp or flat. A newborn's hand is far off (30
hundredths of a semitone); it gets surer with every note by a fixed rule, as its timing hand does. It
hears the pitch of every note it plays, from its own sound. When its last eight notes were within 12
hundredths of a semitone on average, two bars running, it has earned its second note: the E an octave
up. From then on it stops ignoring the bright hits. It puts the low note where it hears a pot and the
high note where it hears a pan. In its pattern `p` is the low note and `n` the high one.

**Settling.** Each of the sixteen places in a player's bar has a habit, from 0 to 9. Every round, a place
where what it plays agrees with what it hears gets one more settled; a place where it does not gets two
less; a place it has just changed starts again from nothing. An idea of its own at a place only goes
ahead if chance beats that place's habit: always at 0, one time in ten at 9. Copying what it hears is
never held back. So a young player is restless everywhere and an old one mostly leaves alone what has
worked for a long time. Habits are written into its life.

**Feel.** Where a hit falls against the tick, a little ahead or a little behind, and how hard it is hit,
is the feel of a record. A lesson can say how it is played (`push` and `weight`, below). When a player
listens to a record it notices both for every hit it can place, and each place in its bar slowly takes on
what it has heard there, a tenth of the way each time. When it plays, it plays each place that far off
its tick and that hard, as far as it has heard that place often enough to trust it. Its feel goes with
it wherever it plays and is written into its life. A player raised on a different record plays the same
places differently.

**Playing along with a recording.** The track is in the players' headphones with a click at the song's
tempo, lined up with the song's one, the way a band practises to a record. They still hear each other
across the room. Everyone starts together after the count-in and plays their own pattern from the first
bar. A song is a full mix and to one ear nearly all of it sounds bright, so to the track each player
listens with two: one on the bottom of the sound (kick and bass), one on the top (hats, the crack of a
snare). A drummer takes notice only of the heavy hits in each: the kick stands above the bass line and
the snare above the hats. A bass player takes the whole of the bottom: that is the bass line. Every two
bars each moves one place toward what it has been hearing, and takes on the feel of it. A drummer with
a track on learns its part from the track, not from the bass player beside it. Nobody is told what is
in the song. The tool that sets the click finds the tempo and the one beforehand, and a person should
check the one by ear (`--hear` writes a clip for that).

Players judge each other's timing against where they themselves would play a place, feel included. Someone
who plays a place late on purpose is not behind.

**How it learns its settings.** Listening and leaning are found by trial, on honest evidence. Every two
bars a player takes stock of how it has sounded. It hears how things are as it is, tries a change big
enough to hear, then goes back and hears how things are again. It keeps a small step of the change only
if the trial was clearly better than the stretches on both sides of it. Comparing with both sides matters:
a player warms up during a session, so anything tried later would otherwise look better for that reason
alone. The first two bars of a session count for nothing, and which direction it tries first is left to
chance. A young player takes bigger steps than an old one.

**The room.** Each player stands somewhere in it. A player's own sound reaches it in 3 ms and everyone
else's in 9 ms, so it can tell its own hits from the others'. It learns how long after it hits it hears
itself, and it can see how much farther away the others are, so it places their hits when they were
played and not when they arrived.

**The ending.** After its last bar it lands the one of the next bar and lets that hit ring for one pulse
of its own clock, whatever its tempo has become.

**Its life.** `lives/drummer.life` is read before every session and added to after it. Its listening, its
ticks and its pattern are where the next session starts. That file in this repository is the real
drummer's life so far.

## Run it

Needs Java 21 and Maven.

    mvn package
    java -jar target/fraymus-band-0.17.0.jar

The record plays `lessons/first.lesson`, the drummer answers four times, then they play eight bars
together. The take goes to `out/session.wav` and a line is added to `lives/drummer.life`.
Options: `--lesson FILE --answers N --together BARS --seed N --out FILE --life FILE`.

To sit two drummers down together with no record:

    java -jar target/fraymus-band-0.17.0.jar --with lives/second.life

The drummer in `--life` starts and the one in `--with` comes in after a bar. They play 16 bars
(`--together BARS` changes that). A life file that does not exist yet is a newborn.

To have one drummer teach another its pattern:

    java -jar target/fraymus-band-0.17.0.jar --teach lives/second.life

The drummer in `--life` gives the lesson and the one in `--teach` learns: four calls and answers, then
eight bars together.

To add the bass player to one or two drummers:

    java -jar target/fraymus-band-0.17.0.jar --with lives/second.life --bass lives/bass.life --click on

Add `--click on` to any of these to put a click in the players' ears. With the record it runs at the
lesson's tempo. With drummers only it runs at 92 bpm, or at `--tempo BPM`.

## Play along with a song

    java -jar target/fraymus-band-0.17.0.jar --along songs/some-song.wav

The drummer in `--life` plays along for 24 bars (`--together BARS`), starting from the first one at or
after 46 seconds in (`--from SECONDS`). Add `--with LIFE` for a second drummer and `--bass LIFE` for the
bass player. The take is the song and the players together. Check the one
first with `--hear`, below.

## What does the ear make of a recording?

    java -jar target/fraymus-band-0.17.0.jar --hear songs/some-song.wav

This plays a WAV (mono, 16-bit, 44,100 Hz; convert with `ffmpeg -i song.mp3 -ac 1 -ar 44100 -sample_fmt s16
song.wav`) past the same ear the players use. It teaches nobody anything. It reports what the ear noticed,
the steady pulse the hits line up on, and the whole recording folded onto one bar, which it writes out as
a lesson with its feel. It also writes thirty seconds of the recording with a click where it thinks the
pulse is and a higher click on the one, because it can be wrong about the one and somebody has to check
by ear.

One ear is not enough for a full mix: almost everything in a song sounds bright to an ear built for a pot
and a pan. So it listens with two, one on the bottom of the sound (below about 150 Hz: kick, bass) and
one on the top (above about 5,000 Hz: hats, the crack of a snare). What lies between, voices and keys,
goes to neither. With a loud voice-like sound laid over the record, one ear makes the bar out as
`p.p.....p..pn...`; two ears get `p..pn...p.p.n..n`, which is what the record plays.

Recordings are kept in `songs/`, which is not committed.

## Write your own lesson

A lesson is a text file: one bar of sixteen places, a tempo, and optionally how it is played. `p` is the
pot, `n` is the pan, a dot is nothing.

    name: laid back
    tempo: 96
    bar:    p..pn...p.p.n..n
    push:   0 0 0 -8 22 0 0 0 0 0 0 0 22 0 0 12
    weight: 1.0 0 0 0.45 0.85 0 0 0 0.7 0 0.5 0 0.85 0 0 0.4

`push` is how many milliseconds after its tick each place is played; a minus sign means ahead of it. It
cannot be more than 30 either way. `weight` is how hard each place is hit, from 0 to 1. Keep the one the
heaviest hit of the bar; that is how a listener finds it. Three lessons come with the project:
`first` (on the tick), `laid-back` (pans behind) and `on-top` (pans ahead). They use the same places.

## What was measured

**Read this first.** Until step 13 the way players learned their settings had a bias in it: a change was
compared only with what came before it, and since a player warms up during a session, almost any change
looked like an improvement and was kept. Players seemed to learn faster than they really could. The
tables below marked *honest* were measured after that was fixed. The others are from before it and
overstate how fast players grow; the comparisons inside them still hold.

**With the record.** Forty drummers, each living twenty-four sessions with the first lesson: four answers,
then eight bars together. "Apart" is how far each of the drummer's hits landed from the record's nearest.

| Session | Apart, playing together (ms) | Last hits within 40 ms | Like the lesson (of 16) | Have two ticks | Have four ticks | Unsteadiness (ms) | Listening |
|--------:|-----------------------------:|-----------------------:|------------------------:|---------------:|----------------:|------------------:|----------:|
| 1  | 31.2 | 15 of 40 | 12.2 | 2  | 0  | 18.9 | 0.24 |
| 6  | 31.1 | 19 | 13.0 | 17 | 0  | 16.6 | 0.27 |
| 12 | 26.0 | 25 | 13.4 | 31 | 12 | 12.9 | 0.40 |
| 24 | 20.7 | 23 | 14.3 | 37 | 27 | 9.9  | 0.46 |

*Honest.* This is without a click. Before the fix the same study reported 13.3 ms apart and listening of
0.61 at session 24.

A drummer playing only the pot on every pulse already matches 11 of 16 places, because most places in
this lesson are empty.

**Two drummers, no record.** Twenty takes of 16 bars for each pairing, with lives written by hand so the
pairings could be compared.

| Pairing | Apart (ms) | Hits with nothing of the other's near | Tempo, first to last bars | Last hits within 40 ms |
|---------|-----------:|--------------------------------------:|---------------------------|-----------------------:|
| Two practised | 6.5 | 0 of 75 | 92.1 to 88.9 bpm | 20 of 20 |
| Practised and newborn | 19.5 | 6 of 60 | 92.4 to 90.3 bpm | 14 of 20 |
| Two newborns | 24.5 | 15 of 60 | 92.7 to 93.9 bpm | 14 of 20 |

**One drummer teaching another.** Sixteen newborns, each taught for sixteen sessions by a practised
drummer playing the first lesson's pattern.

| Session | Like the teacher (of 16) | Believes the lesson right (of 16) | Have four ticks |
|--------:|-------------------------:|----------------------------------:|----------------:|
| 1  | 11.6 | 12.0 | 0 of 16 |
| 4  | 12.4 | 12.7 | 0 |
| 7  | 12.3 | 12.9 | 0 |
| 10 | 12.7 | 12.5 | 1 |
| 13 | 13.2 | 13.2 | 2 |
| 16 | 13.4 | 13.8 | 4 |

That is slower than learning from the record, which never wavers. A teacher whose own time moves is
harder to hear.

**With and without a click** (from before step 13). The same measurements, with a click in every player's ears.

| | Without a click | With a click |
|---|---:|---:|
| Two practised drummers, tempo over 16 bars | 92.1 to 88.9 bpm | 92.0 to 92.0 bpm |
| Two practised drummers, apart | 6.4 ms | 10.8 ms |
| A practised drummer's unsteadiness beside a newborn | 25.4 ms | 10.6 ms |
| Two newborns' unsteadiness | 37.6 ms | 14.4 ms |
| Taught by a drummer for 16 sessions: like the teacher (of 16) | 13.4 | 15.2 |
| Taught by a drummer for 16 sessions: have four ticks | 4 of 16 | 15 of 16 |
| With the record, session 24: apart | 19.8 ms | 8.3 ms |
| With the record, session 24: have four ticks | 13 of 20 | 19 of 20 |

The click fixes the slowing and stops a newborn dragging a practised drummer. At first it costs
something: two practised drummers land a little farther apart, because each follows the click and not
the other. Leaning on each other can win some of that back, but old hands take it up very slowly, because the gain
is too small to hear clearly. *Honest:* eight pairs of practised drummers over eight sessions went from
11.3 ms apart to 9.3, with leaning still at 0.01. Set by hand, leaning of 0.4 brings a practised trio from
7.3 ms apart to 5.2 and costs almost nothing in steadiness; above 0.6 it only makes their time rougher.

The tempo stayed within 2 bpm of the click in every one of those takes.

**Playing along.** *Honest.* A made-up song whose drum part is known (`p...n...p.p.n...`), with a bass
line under it, hats over it and a voice in the middle. Six practised drummers who start with the pot on
every pulse play along three times each. In at least five of the six lives the pattern moves toward the
song's drum part and most hits land with a hit in the song; the first one ends on `p...n...p.ppn...`, the
drum part plus one bass note it took for a kick, with 120 of 131 hits landing with the song, 6 ms apart.

The whole band on the same made-up song: two drummers starting with the pot on every pulse and a bass
player starting with one note on the one, three takes. The drummers end on `p...n...p.ppn...` and
`p...n...p.ppnp..` (the song's drums are `p...n...p.p.n...`), the bass player on `p...n.p.p.p.n.p.`, and
everyone keeps their time in at least three bands of four.

**Feel.** *Honest.* Six practised drummers raised for six sessions on each of two records that use the
same places and opposite feels, with a click.

| Raised on | Its pans | The record's pans | Its pickup | The record's pickup | How hard it hits its pans | The record | Unsteadiness |
|---|---:|---:|---:|---:|---:|---:|---:|
| laid back | +21.6 ms | +22 | -8.0 ms | -8 | 0.82 | 0.85 | 9.6 ms |
| on top    | -6.9 ms  | -9  | 0.0 ms  | 0  | 0.70 | 0.70 | 9.8 ms |

**The bass player.** *Honest.* Twelve newborn bass players, each living twenty-four sessions of 16 bars
with two practised drummers and a click. The drummers' pots fall on four places of the bar and their
pans on three.

| Session | Out of tune (hundredths of a semitone) | Have two notes | Low notes on a pot | High notes | High notes on a pan | From the drummer (ms) |
|--------:|---------------------------------------:|---------------:|-------------------:|-----------:|--------------------:|----------------------:|
| 1  | 25 | 0 of 12 | 1.9 | 0.0 | 0.0 | 24.5 |
| 13 | 10 | 12 | 2.9 | 2.0 | 1.9 | 10.9 |
| 22 | 6  | 12 | 2.9 | 2.0 | 2.0 | 10.3 |

Most of them settle on `p...n...p.p.n...`: the low note with the pots, the high note with the pans.
Nobody told them to; it is what they listen for.

**Settling** (from before step 13). The same lives measured before and after players had habits.

| | Before | With habits |
|---|---:|---:|
| Bass player, sessions 13 to 24: ends on the pattern most of them find | 60 of 144 takes | 105 of 144 |
| Bass player, sessions 13 to 24: ends a session on the pattern it began with | 31 of 144 | 101 of 144 |
| Bass player: ideas of its own per session | 3.3 | 1.0 |
| Drummer with the record, sessions 21 to 30: plays the lesson exactly | 48 of 120 | 96 of 120 |
| Taught by a drummer for 16 sessions: like the teacher (of 16) | 13.4 | 14.6 |

**Wandering and honest learning.** Eight bands of two practised drummers and a newborn bass player, 48
sessions each, under the three ways players have learned their settings.

| After session 16 | First rule (step 1) | Careful but biased (step 12) | Honest (step 13) |
|---|---:|---:|---:|
| Takes with someone unsteadier than 14 ms | 25 of 256 | 9 of 256 | 2 of 256 |
| Takes with the bass more than 12 ms from the drummer | 21 of 256 | 5 of 256 | 18 of 256 |
| Bass from the drummer at session 24 | 7.2 ms | 7.7 ms | 8.5 ms |
| Bass player's listening at session 24 | 0.69 | 0.63 | 0.74 |
| Drummers' leaning at session 48 | 0.44 and 0.48 | 0.27 and 0.37 | 0.00 and 0.05 |

Set by hand, more listening is simply better, steeply at first: a trio all listening at 0.05 has the bass
31.6 ms from the drummer; at 0.4, 10.9 ms; at 0.8, 5.5 ms; at 0.95, 5.2 ms.

Without a click a newborn bass player beside two drummers loses its place: in twelve takes it ended
together with them in none.

`mvn test` reproduces the first and third tables and the bass table, checks the first row of the second, and checks that
the click holds the tempo, protects a practised drummer from a newborn, and speeds up learning. It fails if practice stops
bringing a drummer closer to the record, if two practised drummers cannot hold time together, or if a
drummer plays or hears a place its ticks cannot reach.

## What is given and what is learned

- Learned by the drummer: how much to listen, how much to lean on the other players, which ticks it can
  hold, its pattern, and the lesson, by ear.
- Given to it: its hand gets surer with every hit by a fixed rule. A heavier hit on the one and lighter
  hits on the small ticks. The one stays on the pot. Tapping along to what it hears is a reflex. The form
  (two bars of record, two bars of answer, then so many bars together) is told to it. It can tell its
  own hits from everyone else's, and it can see how far away the others are.
- Not there yet: taste of its own. It copies the one record it hears. Its own ideas are picked by chance.

## Known limits

- Every session starts again at 92 bpm, the tempo its body likes.
- It finds the one by weight, so a lesson with nothing on the one will confuse it.
- A drummer that has drifted far from the record can lose its place for a whole call and hear almost
  nothing of the lesson that round.
- Without a click, two practised drummers who follow each other slow down, about 3 bpm over 16 bars.
  Half of that was a bias in how they leaned on short gaps, now fixed. The rest is not explained yet.
- Learning from a drummer is slow and sometimes wrong: its time moves, so the learner misplaces hits.
- The teacher does not listen to the answers or change what it plays to help.
- Without a click, a newborn pulls a practised drummer's time around. Nobody yet decides whom to follow.
- The bass has two notes, a low E and the E above. Two notes an octave apart is not yet a bass line.
- A bass player rarely earns four ticks to the pulse, so it seldom reaches the drummers' sixteenth notes.
- Settling is not taste. A player settles on agreeing with what it hears. An idea of its own that nobody
  else plays never lasts, so nothing original survives.
- Playing along has only been tried on one real song. How heavy a hit must be to count (0.6 in the bottom,
  0.5 in the top) was chosen on that song and may not suit another.
- In the bottom of a song a drummer cannot tell a loud bass note from a kick.
- Playing along, everyone ends up on much the same line. The drummers hear the same heavy hits, and the
  bass player follows the bottom of the track at the eighth notes it can reach.
- A player's feel can pick up the lateness of a slow bass note as if it were a late kick.
- Feel is one number for timing and one for weight at each place. It cannot hold swing: a swung note is
  about 100 ms off the straight tick, which is a different place, and the players have no triplet ticks.
- A player has one feel, an average of everything it has heard. It cannot yet keep two records apart.
- A player cannot play more than 10 ms ahead of its tick.
- Feel has only been measured with a click.
- Honest learning is slower. Without a click, drummers reach four ticks to the pulse and tight time later
  than the earlier tables claimed.
- Players do not learn to lean on each other, though leaning of about 0.4 would help them. The gain is
  real but too small for a player to hear clearly in two bars.
- The real band's leaning (0.60 each) was learned under the biased rule and is higher than is good for them.
- Without a click, three players do not hold together.
- The drummers do not listen to the bass for anything but time.
- Mono.

## Layout

- `Clock` each player's own pulse
- `Ear` hears hits in the room
- `Room` the shared air: each player's sound, and how long it takes to reach the others
- `PitchEar` how a player hears whether its own note was in tune
- `Instrument`, `Kit`, `Bass` what a player's hands are on; `Thump`, `Pan` the kit's two sounds
- `Lesson`, `Teacher` the bar to learn and the record that plays it
- `Hearing` what the players' ear makes of a recording; `Recording` a song playing in the room
- `Metronome` the click in the players' ears
- `Musician` the player, at a kit or at the bass
- `Life` the lineage file
- `Session` one sitting, written to a WAV
