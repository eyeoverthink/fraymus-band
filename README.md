# Fraymus Band

A band of players built on the computer. Each one owns an instrument, keeps its own clock, plays live
into a shared room, and has to grow up before it can play well. Java, no interpreted code, every buffer
claimed before the first sample.

## Step one (this is all that exists so far)

One infant drummer. One sound, a hand on a pot. Its own pulse, which nobody sets for it.

- Its hand is unsteady: the pulse wanders and each hit lands a little off.
- The room carries its hits back to it 30 ms later, and it hears them.
- It can pull its pulse toward what it heard. How much it listens is not given to it. A newborn barely
  listens. Every few hits it tries listening a little more or a little less and keeps whichever sounded
  steadier to its own ear.
- That habit is written into `lives/drummer.life`, which is read before every session and added to
  after it. The next session starts where the last one left off.

## Run it

Needs Java 21 and Maven.

    mvn package
    java -jar target/fraymus-band-0.1.0.jar

It plays 64 pulses, writes `out/session.wav`, and adds a line to `lives/drummer.life`.
Options: `--pulses N --seed N --out FILE --life FILE`.

## What was measured

Sixty drummers, each living eight sessions of 64 pulses. Unsteadiness is the typical jump from one gap
between hits to the next, in milliseconds.

| Session | At the start | By the end | How much it listens |
|--------:|-------------:|-----------:|--------------------:|
| 1 | 17.6 | 17.5 | 0.21 |
| 2 | 17.1 | 16.4 | 0.28 |
| 3 | 16.0 | 15.8 | 0.35 |
| 4 | 16.6 | 14.7 | 0.41 |
| 5 | 15.8 | 14.2 | 0.47 |
| 6 | 14.6 | 12.8 | 0.52 |
| 7 | 13.4 | 12.0 | 0.58 |
| 8 | 13.2 | 12.0 | 0.62 |

`mvn test` reproduces this table and fails if practice stops making the drummer steadier.

## Known limits

- It has no sense of where its tempo should sit, so a take can speed up or slow down a lot
  (the first recorded session went from 92 to 109 bpm).
- One player, one sound, no bar, no other players to listen to. Those are the next steps.

## Layout

- `Clock` each player's own pulse
- `Ear` hears hits in the room and remembers the gaps between them
- `Room` the shared air, a fixed delay line
- `Thump` the one sound
- `Drummer` the infant
- `Life` the lineage file
- `Session` one sitting, written to a WAV
