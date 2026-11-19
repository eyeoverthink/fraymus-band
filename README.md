# Fraymus Band

A band of players built on the computer. Each one owns an instrument, keeps its own clock, plays live
into a shared room, and has to grow up before it can play well. Java, no interpreted code, every buffer
claimed before the first sample.

So far there are two players, both drummers with a pot and a pan, and a record for them to learn from.

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
    java -jar target/fraymus-band-0.8.0.jar

The record plays `lessons/first.lesson`, the drummer answers four times, then they play eight bars
together. The take goes to `out/session.wav` and a line is added to `lives/drummer.life`.
Options: `--lesson FILE --answers N --together BARS --seed N --out FILE --life FILE`.

To sit two drummers down together with no record:

    java -jar target/fraymus-band-0.8.0.jar --with lives/second.life

The drummer in `--life` starts and the one in `--with` comes in after a bar. They play 16 bars
(`--together BARS` changes that). A life file that does not exist yet is a newborn.

To have one drummer teach another its pattern:

    java -jar target/fraymus-band-0.8.0.jar --teach lives/second.life

The drummer in `--life` gives the lesson and the one in `--teach` learns: four calls and answers, then
eight bars together.

Add `--click on` to any of these to put a click in the players' ears. With the record it runs at the
lesson's tempo. With drummers only it runs at 92 bpm, or at `--tempo BPM`.

## Write your own lesson

A lesson is a text file: one bar of sixteen places and a tempo. `p` is the pot, `n` is the pan, a dot is
nothing. Put the heaviest hit on the one; that is how the drummer finds it.

    name: first lesson
    tempo: 96
    bar: p..pn...p.p.n..n

## What was measured

**With the record.** Forty drummers, each living twenty-four sessions with the first lesson: four answers,
then eight bars together. "Apart" is how far each of the drummer's hits landed from the record's nearest.

| Session | Apart, playing together (ms) | Last hits within 40 ms | Like the lesson (of 16) | Have two ticks | Have four ticks | Unsteadiness (ms) |
|--------:|-----------------------------:|-----------------------:|------------------------:|---------------:|----------------:|------------------:|
| 1  | 27.6 | 31 of 40 | 11.9 | 6  | 0  | 18.9 |
| 8  | 24.5 | 30 | 12.5 | 32 | 3  | 13.2 |
| 16 | 16.8 | 36 | 13.5 | 40 | 20 | 10.4 |
| 24 | 13.3 | 36 | 14.5 | 40 | 34 | 9.0  |

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

**With and without a click.** The same measurements, with a click in every player's ears.

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
the other. Then they learn to lean on each other as well. Eight pairs of practised drummers, eight
sessions of 16 bars with a click:

| Session | Apart (ms) | Leaning on each other (0 to 0.6) |
|--------:|-----------:|---------------------------------:|
| 1 | 10.1 | 0.11 |
| 2 | 8.7  | 0.18 |
| 4 | 7.5  | 0.25 |
| 6 | 6.5  | 0.29 |
| 8 | 6.3  | 0.28 |

The tempo stayed within 2 bpm of the click in every one of those takes.

`mvn test` reproduces the first and third tables, checks the first row of the second, and checks that
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
- Mono, one drummer.

## Layout

- `Clock` each player's own pulse
- `Ear` hears hits in the room
- `Room` the shared air: each player's sound, and how long it takes to reach the others
- `Thump`, `Pan`, `Kit` the two sounds and the hands that ring them
- `Lesson`, `Teacher` the bar to learn and the record that plays it
- `Metronome` the click in the players' ears
- `Drummer` the player
- `Life` the lineage file
- `Session` one sitting, written to a WAV
