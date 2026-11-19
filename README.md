# Fraymus Band

A band of players built on the computer. Each one owns an instrument, keeps its own clock, plays live
into a shared room, and has to grow up before it can play well. Java, no interpreted code, every buffer
claimed before the first sample.

So far there is one player: a young drummer with a pot and a pan.

## What the drummer does

**The pulse.** Nobody sets its tempo. Its hand is unsteady: the pulse wanders and each hit lands a little
off. The room carries its hits back 30 ms later. It pulls its pulse toward what it heard, and it learns
how much to listen by trying a little more or a little less and keeping whichever sounded steadier.

**The tick.** A bar is four pulses. A newborn feels only the pulse: one tick to the pulse. To earn the
next cut (two ticks, then four) it has to be playing the ticks it already has, and those gaps have to
sound even enough to its own ear for two bars running. Four ticks on four pulses is sixteen places in
the bar.

**The pattern.** It starts with the one thing it knows: the pot on every pulse. Every round (two bars) it
changes one place its ticks can reach: a new hit, a pot swapped for the pan, a hit taken away. If the
round sounded too uneven, the change was more than its hand could hold and it takes it back. Otherwise
it keeps it. The one of the bar stays on the pot, and a pulse is never left empty.

**The ending.** After its last bar it lands the one of the next bar and lets that hit ring for one pulse
of its own clock, whatever its tempo has become.

**Its life.** `lives/drummer.life` is read before every session and added to after it. Its listening, its
ticks and its pattern are where the next session starts. That file in this repository is the real
drummer's life so far.

## Run it

Needs Java 21 and Maven.

    mvn package
    java -jar target/fraymus-band-0.2.0.jar

It plays 16 bars, writes `out/session.wav`, and adds a line to `lives/drummer.life`.
Options: `--bars N --seed N --out FILE --life FILE`.

## What was measured

Sixty drummers, each living twenty sessions of 16 bars.

| Session | Unsteadiness (ms) | Listening | Have two ticks | Have four ticks | Hits in 16 bars |
|--------:|------------------:|----------:|---------------:|----------------:|----------------:|
| 1  | 17.2 | 0.24 | 8 of 60  | 0 of 60  | 66  |
| 3  | 14.5 | 0.38 | 35 | 3  | 79  |
| 5  | 12.3 | 0.45 | 48 | 13 | 96  |
| 8  | 9.7  | 0.54 | 58 | 37 | 124 |
| 10 | 8.4  | 0.56 | 60 | 42 | 138 |
| 15 | 6.3  | 0.58 | 60 | 59 | 162 |
| 20 | 5.4  | 0.56 | 60 | 60 | 186 |

`mvn test` reproduces this and fails if nobody has to grow into their ticks, if practice stops helping,
or if a drummer plays a place its ticks cannot reach.

## What is given and what is learned

- Learned by the drummer: how much to listen, which ticks it can hold, its pattern.
- Given to it: its hand gets surer with every hit by a fixed rule. A louder hit on the one and softer
  hits on the small ticks. The one stays on the pot.
- Not there yet: taste. Changes to the pattern are picked by chance and kept only if it could play them
  evenly. Nothing tells it whether a pattern is good.

## Known limits

- Every session starts again at 92 bpm, the tempo its body likes.
- One player, mono, nobody else to listen to.

## Layout

- `Clock` each player's own pulse
- `Ear` hears hits in the room
- `Room` the shared air, a fixed delay line
- `Thump`, `Pan` the two sounds
- `Drummer` the player
- `Life` the lineage file
- `Session` one sitting, written to a WAV
