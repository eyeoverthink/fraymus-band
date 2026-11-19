# Fraymus Band: handoff

Read this first. It is written so that a person or an agent who has never seen this project can pick it up
without Vaughn having to explain it again. `README.md` describes what the code does; this file says what
we are making, why, how we work, what has been tried, and what comes next.

Owner: Vaughn Scott (GitHub `eyeoverthink`). Repository: `eyeoverthink/fraymus-band`, branch `main`.

---

## 1. What we are making

A band of AI players, built entirely on the computer.

In Vaughn's words and by his corrections, the things that are not negotiable:

1. **Every player performs live.** There is no "writing" stage separate from "performing". Nobody composes
   a part and then renders it. Each player plays into the room in real time and the take is whatever
   happened.
2. **Every player owns one instrument** and is a soloist in its own right. The picture is a band of
   world-class players trading bars, not a sequencer with tracks.
3. **Every player has its own clock.** There is no master clock. Players hold time together by listening
   and pulling toward each other (entrainment). A click track is allowed, as a real band uses one, but it
   is something players hear and choose to follow, not something that drives them.
4. **Lineage must be lived.** A player has a history, and the history is not a backstory written as text.
   It has to grow up: crawl, walk, run. A newborn can do almost nothing. Whatever it can do later, it got
   by playing. Its life is a file that is read before every session and added to after it.
5. **Java, not Python.** Vaughn: "We both know Python is cheating, and I am not for interpreted language."
   Plain Java 21 and Maven. No audio libraries.
6. **Preallocate everything.** All buffers are claimed before the first note. Nothing is allocated inside
   the audio loop. "Data never sits still."
7. **Simulated devices first.** Everything runs on the computer and writes a WAV. Real hardware comes
   later (see section 9).
8. **The bar is the buffer; genre lives in the ticks.** Rhythm, feel and style come from where hits fall
   among the ticks of the bar. "Tick and timing" is the foundation; master it before anything else.
9. **Where an ending lands matters.** A take does not just stop at bar N. Ending on the one of the next
   bar, on the four, on the "and", are different musical choices and belong to the player.

The eventual lineup he described: drums, three backup singers, electric and acoustic guitar, keys, sax,
muted trumpet, playing jazz, hip hop and salsa. Each one an agent with its own lineage.

---

## 2. How Vaughn wants the work done

- **One step at a time, in the order he gives.** Each step must actually work, with tests passing and the
  change pushed, before the next step starts. Do not pull in everything at once.
- **Act; do not ask multiple-choice questions he has already answered.** When he says "proceed" or
  "resume", take the next step. If he has not named it, pick the most important open limit, say in one
  line which one you picked and why, and do it.
- **Do not go off on tangents.**
- **He reads on a phone.** Keep replies short, lead with the result, use a short list when it helps.
- **He is the ear.** The agent cannot listen to audio. Every step must end with a WAV sent to him and a
  plain statement that the agent has not heard it. His judgement of the sound outranks the numbers.
- **Report honestly.** For every step say what was measured, what is still wrong, and what was *given* to
  the players as a rule versus what they *learned*. He cares a great deal about that distinction. Never
  claim a behaviour the measurements do not show. If a number got worse, say so.
- **Correct yourself when he corrects you.** Examples from this project: the agent once said a take
  "ended on the right measure because it stops at 64 pulses"; he pointed out that there are many places to
  end and that this is a musical matter. The agent once assumed one master clock; wrong. The agent once
  offered Python; wrong.
- **Commit after each working step and push to `main`.** The life files are committed with the code.

---

## 3. Where things stand

Version 0.11.0. 30 tests pass (`mvn test`, about four minutes). Two drummers and a bass player exist, each
with a real life file. There is a record that plays lessons and a click track.

### The steps so far

| Step | What it added |
|-----:|---------------|
| 1 | One infant drummer, one sound (a hand on a pot), its own unsteady clock. It hears itself through the room and learns by trial how much to listen. Writes a WAV and a life file. |
| 2 | The tick. A bar is four pulses; a newborn has one tick to the pulse and must *earn* two, then four (sixteen places in the bar) by playing the ticks it has evenly. A second sound (the pan). A pattern built one change per round and kept only if it could play it evenly. An ending it owns: lands the one after its last bar and rings for one pulse of its own clock. |
| 3 | A record plays a two-bar lesson and leaves two bars; the drummer listens, then answers, and moves its pattern one place at a time toward what it heard. It tells pot from pan by ear and hears only the places its ticks can reach. Lessons are small text files. |
| 4 | The record and the drummer play at the same time. Each player has its own place in the room and can tell its own sound from the others'. |
| 5 | Two drummers, no record, each listening to the other. A second drummer was born. Found and fixed: hits could only land late, never early; a bias in leaning on short gaps that made pairs slow down. |
| 6 | One drummer teaches another its pattern, call and answer, then together. A learner believes a place only if it heard it in at least half the calls. |
| 7 | A click track in the players' ears (Vaughn's suggestion). Not in the room, not on the recording. Fixed the slowdown, stopped a newborn dragging a practised drummer, sped up learning. |
| 8 | With a click, each drummer also learns how much to lean its tick toward the others, so they land together while the click holds the tempo. |
| 9 | The first non-drummer: a bass player with one note. Same player class, different instrument. It listens for the pots and moves its notes onto them while it plays. The class `Drummer` was renamed `Musician`. This handoff was written. |
| 10 | The bass earns a second note (the octave) by playing in tune, judged by its own ear on its own sound. With two notes it puts the low one on the pots and the high one on the pans. |
| 11 | Settling. Every place in a player's bar has a habit (0 to 9) that grows while what it plays agrees with what it hears. Its own ideas are held back at settled places. Players now stay on a pattern they have found. |

### The players that exist (do not reset these)

- `lives/drummer.life`: "the drummer". Born in step 1. Four ticks to the pulse. Learned the first lesson
  from the record, then drifted a little with its own ideas.
- `lives/second.life`: "the second drummer". Born in step 5. Taught by the first.
- `lives/bass.life`: "the bass player". Born in step 9. Earned its second note in step 10. Two ticks to the
  pulse. Low notes on the drummers' pots, high notes on their pans, plus ideas of its own.

**These files are the players' lived history. Never hand-edit them, never delete them, never regenerate
them.** Tests use throwaway lives in temporary folders. If a format changes, make the reader accept the
old lines (there is a test for that: `livesFromEarlierStepsStillRead`).

---

## 4. How to build, run and test

Needs Java 21 and Maven 3.9.

    mvn package                                   # builds and runs every test (about four minutes)
    java -jar target/fraymus-band-0.11.0.jar       # the drummer with the record: 4 answers, then 8 bars together
    java -jar target/fraymus-band-0.11.0.jar --click on
    java -jar target/fraymus-band-0.11.0.jar --with lives/second.life --click on     # two drummers, 16 bars
    java -jar target/fraymus-band-0.11.0.jar --teach lives/second.life --click on    # the first teaches the second
    java -jar target/fraymus-band-0.11.0.jar --with lives/second.life --bass lives/bass.life --click on   # all three

Options: `--lesson FILE --answers N --together BARS --with LIFE --teach LIFE --bass LIFE --click on --tempo BPM
--seed N --out FILE --life FILE`. Every run writes `out/session.wav` (not committed) and adds one line to
each life file involved (committed).

Running a session with the real life files *is* the players living. Do it deliberately, a few sessions
per step, and commit the lives with the step.

---

## 5. How the code is laid out

All in `src/main/java/com/eyeoverthink/band/`.

- `Audio`: sample rate 44,100 and block size 64 samples. Everything is sized from these.
- `Player`: the interface. Once per block a player `hear`s (others, itself, the click) and then `play`s.
- `Clock`: one player's pulse. `period`, `nextBeat`, `advance`, `drift`, `pullToward`, `nudge`.
- `Ear`: finds hits in a stream of sound: when, how loud, bright or dark. One ear per stream.
- `Room`: one ring buffer per player. A player's own sound reaches it in 3 ms, others' in 9 ms.
- `Instrument`: what a player's hands are on. `Kit` (pot and pan) and `Bass` (fretless, low E and high E).
- `PitchEar`: measures the pitch of a player's own note from its own sound, by how far the bottom tone
  turns between two short listens. Accurate to about one hundredth of a semitone.
- `Sound`, `Thump` (pot), `Pan`: sounds are worked out once into tables and only read while playing.
- `Lesson`: a bar of sixteen places (`p` pot, `n` pan, `.` nothing) and a tempo. Files in `lessons/`.
- `Teacher`: the record. Plays the lesson for two bars, leaves two, repeats; then plays straight through.
- `Metronome`: the click. One bar of count-in, then every pulse.
- `Musician`: the player (called `Drummer` until step 9). This is where nearly all the behaviour is. Read
  its class comment first. The same class plays the kit or the bass; `oneNote` marks a bass player.
- `Life`: the lineage file. Header lines say where the player is now; `session N:` lines are what it lived.
- `Session`: one sitting. `run` (with the record), `together` (two drummers), `teach` (one teaches one),
  `band` (one or two drummers and the bass player).
  Also measures the take afterwards from when hits actually landed.
- `WavWriter`: mono 16-bit PCM.
- `Main`: command line.

Tests are in one class, `StepElevenTest` (renamed each step). The big "study" tests run dozens of drummers
through whole lives and print tables; `README.md` quotes those tables.

### The player's states

`WAITING` (quiet until it hears someone), `LISTENING` (ticking along quietly, placing what it hears),
`ANSWERING` (playing its two bars after a call), `TOGETHER` (playing while others play).

### What a drummer learns, and what it is given

Learned, by trial, and saved in its life:

- **listening** (0 to 0.95): how strongly it pulls its pulse toward what it hears. A newborn has 0.15.
- **ticks to the pulse** (1, 2, 4): earned when gaps exactly one tick long have sounded even enough for
  two bars running.
- **pattern**: sixteen places. One change per round; kept unless that round sounded clearly rougher than
  its playing usually does.
- **the lesson**: only what it heard, at the places its ticks reach.
- **habits**: sixteen digits, how settled each place of its bar is.
- **notes** (bass: 1 or 2): the second is earned when its last eight notes were within 12 hundredths of a
  semitone on average, two bars running.
- **leaning on the others** (0 to 0.6): with a click, how far it shifts its tick toward other players.
- **its own delay**: how long after it hits it hears itself.

Given, as fixed rules (be honest about these):

- Its hand gets surer with every hit by a fixed formula (slip 4 ms falling toward 1.2 ms over ~1500 hits).
  The bass player's pitch hand likewise (30 hundredths of a semitone falling toward 6 over ~600 notes).
- Which note goes with which sound: low with the pot, high with the pan. And that the second note is the octave.
- Accents: the one is hit hardest, pulses next, small ticks softest. The one stays on the pot. A pulse is
  never left empty.
- Tapping along (shifting half way toward a heard hit) is a reflex.
- The form of a session (two bars of call, two of answer, so many bars together) is told to it.
- It can tell its own sound from everyone else's, and it can see how far away the others are.
- It finds the one by weight: the heaviest hit is the one.
- It gets ready 12 ms before each tick, so a slip can land early as well as late.

---

## 6. Things that went wrong, so nobody repeats them

- **Practice did not carry over between sessions** (step 1) until what it learned was something stored in
  the life file, not just experience points.
- **Judging by gaps that included other players' hits** made tempo run away to 190 bpm. A player must judge
  its own playing only from its own hits. This is why each player has a separate ear on itself.
- **Hits could only be late.** A strike was scheduled when its tick fell inside the current block, so an
  early slip was clipped. Players now get ready 12 ms ahead.
- **Two players following each other slowed down.** Half was a real bias: the ear is slightly quicker on
  heavy hits than light ones, so short gaps between a light and a heavy hit mis-state the pulse. While
  playing, a player now leans its pulse only on gaps at least a pulse long. The other half (about 3 bpm
  over 16 bars without a click) is still unexplained. With a click it does not happen.
- **Anchoring on the wrong hit.** A listener takes the first hit after quiet as the one. If that hit was
  masked or it came in late, everything it heard was shifted. Fixes: the heaviest hit is the one; an
  unplaceable heavy hit restarts the count.
- **A bass player that dropped notes it should have kept.** Not hearing a pot once was treated as proof
  there was none. It now removes a note only after listening four times and never hearing one there.
- **A pan in the bass part.** What it heard while waiting to come in still counted the bright hits. A bass
  player now ignores them when it decides what it believes.
- **Two pots landing almost together sounded bright**, so the bass put high notes on the pots. The ear
  judged brightness 5 to 20 ms after a hit, where a second player's click could fall. It now judges 25 to
  45 ms after.
- **Measuring pitch from the whole shape of the note was off by up to 13 hundredths of a semitone**, because
  the upper tones die away at different speeds. `PitchEar` follows only the bottom tone.
- **Tried and taken back out (step 11):** making a player weigh its own steadiness when judging how hard to
  lean on the others. It sounded right and measured no better (36 rough takes in 256 against 25), so it
  was removed. Do not keep a change because it is plausible; measure it.
- **A take that never ended**, twice: a waiting player with nobody left to wait for (it now comes in alone
  after two bars), and a final hit cancelled by a re-anchor. `Session` now throws if a take runs absurdly
  long, so a hang shows up as a failed test.
- **Clipping** when two players overlap. The kit's output level is 0.5 so two full hits cannot exceed 1.
- **Loudness that made the one ambiguous.** Accents were 1.0 / 0.85; with a human-like random loudness on
  top, a pulse could be as loud as a one. Now 1.0 / 0.75 / 0.62 / 0.52.
- **Tests that counted lines in a life file** broke whenever a header line was added. Find lines by content.
- **Maven offline (`-o`) does not work in the agent's sandbox.** Run it online.
- **The full test run takes about four minutes.** Give commands a long timeout.

---

## 7. Known limits right now

- Without a click: two practised drummers slow by about 3 bpm over 16 bars, and a newborn drags a
  practised drummer's time around.
- The click gives the pulse only, not the one.
- Learning from another drummer is slower and less accurate than from the record.
- The teacher does not listen to the answers.
- A drummer's own ideas are picked by chance. There is no taste yet beyond copying what it hears.
- Every session starts at 92 bpm, the tempo the players' bodies like.
- The bass has two notes an octave apart. That is not yet a bass line.
- A bass player rarely earns four ticks to the pulse.
- Settling is not taste. Players settle on agreeing with what they hear; an idea nobody else plays never
  lasts. Nothing original survives yet.
- **Learned settings wander.** Listening and leaning are found by hill-climbing on noisy evidence. In the
  real band, over sessions 25 to 42, the bass player's listening fell from 0.81 to 0.29 and its unsteadiness
  rose from 6 to about 18 ms. Averaged over many simulated bands there is no decline, but any one player can
  have a bad stretch: about one take in ten late in life has someone unsteadier than 14 ms. This needs a
  steadier way of learning (longer evidence before each change, or smaller steps as a player ages).
- Without a click, three players do not hold together.
- The drummers take nothing from the bass but time.
- Mono.
- The repository is public and has no license file. Vaughn has been told.

---

## 8. What comes next

The bass player exists (step 9). Roughly in this order unless Vaughn says otherwise:

0. Probably first: stop learned settings wandering (see section 7).
1. More notes for the bass (a fifth, then a scale), each earned by playing the ones it has in tune. A third
   note needs a third symbol in the pattern and a reason to choose it; pots and pans only give two.
2. Lessons in other feels (a clave, a hip hop bar, a swing bar). "Genre lives in the ticks."
3. Taste: a reason to prefer one pattern over another that is not just copying.
4. Trading bars between players as soloists.
5. The other instruments in his lineup, one at a time, each born new.
6. Stereo, so players can be heard apart.
7. Real devices (section 9).

---

## 9. His studio, for later

Tascam Model 12, Akai MPC Key 61, Boss RC-505mkII loop station, Zoom H6, Tascam Portacapture X8,
M-Audio M-Track Duo. His original idea was one agent per input or output of these devices, layering live
on the Model 12 and the RC-505, with feedback and delay as part of the instrument. The simulation comes
first; the same players should later play through real inputs and outputs.

---

## 10. Other projects of his (not part of this repository)

So a new agent is not confused by references: the infinite-particle physics simulations
(`eyeoverthink/infinite-particle-sim`), the Shaped Player web video player (`eyeoverthink/Shaped-player`)
and the Shaped Studio desktop app (`eyeoverthink/shaped-player-desktop-`, private until he says to
publish). None of them share code with this one.
