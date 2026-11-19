# Fraymus Band

A band of players built on the computer. Each one owns an instrument, keeps its own clock, plays live
into a shared room, and has to grow up before it can play well. Java, no interpreted code, every buffer
claimed before the first sample.

So far there is one player, a young drummer with a pot and a pan, and a record for it to learn from.

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

**The room.** Each player stands somewhere in it. A player's own sound reaches it in 3 ms and everyone
else's in 9 ms, so it can tell its own hits from the others'.

**The ending.** After its last bar it lands the one of the next bar and lets that hit ring for one pulse
of its own clock, whatever its tempo has become.

**Its life.** `lives/drummer.life` is read before every session and added to after it. Its listening, its
ticks and its pattern are where the next session starts. That file in this repository is the real
drummer's life so far.

## Run it

Needs Java 21 and Maven.

    mvn package
    java -jar target/fraymus-band-0.4.0.jar

The record plays `lessons/first.lesson`, the drummer answers four times, then they play eight bars
together. The take goes to `out/session.wav` and a line is added to `lives/drummer.life`.
Options: `--lesson FILE --answers N --together BARS --seed N --out FILE --life FILE`.

## Write your own lesson

A lesson is a text file: one bar of sixteen places and a tempo. `p` is the pot, `n` is the pan, a dot is
nothing. Put the heaviest hit on the one; that is how the drummer finds it.

    name: first lesson
    tempo: 96
    bar: p..pn...p.p.n..n

## What was measured

Forty drummers, each living twenty-four sessions with the first lesson: four answers, then eight bars
together. "Apart" is how far each of the drummer's hits landed from the record's nearest hit.

| Session | Apart, playing together (ms) | Last hits within 40 ms | Like the lesson (of 16) | Have two ticks | Have four ticks | Unsteadiness (ms) |
|--------:|-----------------------------:|-----------------------:|------------------------:|---------------:|----------------:|------------------:|
| 1  | 26.7 | 20 of 40 | 11.9 | 9  | 0  | 17.7 |
| 4  | 27.6 | 21 | 12.1 | 29 | 3  | 13.4 |
| 8  | 20.1 | 35 | 12.9 | 38 | 16 | 9.9  |
| 12 | 18.0 | 33 | 14.0 | 40 | 27 | 9.2  |
| 16 | 17.1 | 36 | 14.4 | 40 | 34 | 7.9  |
| 20 | 15.4 | 35 | 14.2 | 40 | 39 | 7.9  |
| 24 | 16.4 | 35 | 14.4 | 40 | 40 | 7.2  |

About 9 to 10 ms of "apart" is the room: the drummer hears the record 9 ms late and plays to what it
hears, so it sits that far behind. A drummer playing only the pot on every pulse already matches 11 of
16 places, because most places in this lesson are empty.

`mvn test` reproduces the table and fails if practice stops bringing the drummer closer to the record,
or if a drummer plays or hears a place its ticks cannot reach.

## What is given and what is learned

- Learned by the drummer: how much to listen, which ticks it can hold, its pattern, and the lesson, by ear.
- Given to it: its hand gets surer with every hit by a fixed rule. A heavier hit on the one and lighter
  hits on the small ticks. The one stays on the pot. Tapping along to what it hears is a reflex. The form
  (two bars of record, two bars of answer, then so many bars together) is told to it. It can tell its
  own hits from everyone else's.
- Not there yet: taste of its own. It copies the one record it hears. Its own ideas are picked by chance.

## Known limits

- Every session starts again at 92 bpm, the tempo its body likes.
- It finds the one by weight, so a lesson with nothing on the one will confuse it.
- A drummer that has drifted far from the record can lose its place for a whole call and hear almost
  nothing of the lesson that round.
- It plays behind the record by the time sound takes to cross the room. It has not learned to lean ahead.
- The record does not listen. Only the drummer adjusts.
- Mono, one drummer.

## Layout

- `Clock` each player's own pulse
- `Ear` hears hits in the room
- `Room` the shared air, a fixed delay line
- `Thump`, `Pan`, `Kit` the two sounds and the hands that ring them
- `Lesson`, `Teacher` the bar to learn and the record that plays it
- `Drummer` the player
- `Life` the lineage file
- `Session` one sitting, written to a WAV
