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

Version 0.18.0. 39 tests pass (`mvn test`, about four minutes). Two drummers and a bass player exist, each
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
| 12 | Learned settings no longer wander. A player hears how it sounds, tries one change, keeps it only if clearly better, and grows more careful with age (more evidence, smaller steps). Rough takes late in life fell from 25 in 256 to 9. |
| 13 | Honest learning. Found that most of the players' apparent learning of their settings came from a bias (warming up during a session made any change look good). Rebuilt it: every two bars a player takes stock; a trial is compared with the stretches before and after it; the first two bars of a session count for nothing; first direction left to chance; probe big, move small. Growth is real now and slower. |
| 14 | Feel. A lesson can say how far ahead or behind the tick, and how hard, each place is played. A listener takes on a tenth of what it hears each time and plays with it. Vaughn's idea: players need artists to study, as he had. He sent his own song as the first real influence (see section 8). |
| 15 | Two ears for a full recording: one on the bottom of the sound, one on the top (`Hearing.listenLowAndHigh`). Recovers the record's bar exactly from under a loud voice-like sound. `Main --hear` also writes a clip with the pulse and the one marked by clicks, for Vaughn to check by ear. |
| 16 | Playing along with a real recording (`Main --along`, `Session.playAlong`, `Recording`). The song plays in the room, a click at its tempo is lined up with its one, and the drummer plays along with two ears, noticing only heavy hits, learning two bars at a time. Vaughn confirmed by ear that the clicks were on the beat and the one was right. The real first drummer has played along with his song five times. |
| 17 | The whole band plays along. The track moved from the room into the players' headphones (a fourth thing a `Player` hears). The bass player takes the whole bottom of the track; drummers only the heavy hits. Players judge each other's timing against where they would play a place themselves, feel included. All three real players have played along with his song. |
| 18 | Roles. A hi-hat (`Hats`) and a hat player: takes the top of a track, plays only `n`. Bass takes only the bottom. Kit drummer takes only heavy hits, and the snare threshold went up to 0.7. With a track on, everyone learns from the track alone. The second drummer moved to the hats. |

### The players that exist (do not reset these)

- `lives/drummer.life`: "the drummer". Born in step 1. Four ticks to the pulse. Learned the first lesson
  from the record. In step 16 it played along with Vaughn's song "Gleam of the Prize" five times and its
  pattern went from `pn.pn...p.p.n..n` to `p.n.n.n.n.n.n.np` (it believes the song is `p.n.n.n.ppn.n.np`:
  kick on one and three, something bright on every off eighth). This is the first real influence on a
  real player. If Vaughn dislikes what it became, the earlier life is in git history (commit `c8a07a0`).
- `lives/second.life`: "the second drummer". Born in step 5. Taught by the first. **Moved to the hi-hat in
  step 18** (Vaughn agreed to roles: one drummer on kick and snare, the other on the hats). Its life says
  `sounds: a hi-hat (n)`. Run it with `--hats lives/second.life`, not `--with`. Its name is unchanged.
- `lives/bass.life`: "the bass player". Born in step 9. Earned its second note in step 10. Two ticks to the
  pulse. Low notes on the drummers' pots, high notes on their pans, plus ideas of its own.

**These files are the players' lived history. Never hand-edit them, never delete them, never regenerate
them.** Tests use throwaway lives in temporary folders. If a format changes, make the reader accept the
old lines (there is a test for that: `livesFromEarlierStepsStillRead`).

---

## 4. How to build, run and test

Needs Java 21 and Maven 3.9.

    mvn package                                   # builds and runs every test (about four minutes)
    java -jar target/fraymus-band-0.18.0.jar       # the drummer with the record: 4 answers, then 8 bars together
    java -jar target/fraymus-band-0.18.0.jar --click on
    java -jar target/fraymus-band-0.18.0.jar --with lives/second.life --click on     # two drummers, 16 bars
    java -jar target/fraymus-band-0.18.0.jar --teach lives/second.life --click on    # the first teaches the second
    java -jar target/fraymus-band-0.18.0.jar --with lives/second.life --bass lives/bass.life --click on   # all three

Options: `--lesson FILE --answers N --together BARS --with LIFE --teach LIFE --bass LIFE --click on --tempo BPM
--seed N --out FILE --life FILE`. Every run writes `out/session.wav` (not committed) and adds one line to
each life file involved (committed).

Running a session with the real life files *is* the players living. Do it deliberately, a few sessions
per step, and commit the lives with the step.

---

## 5. How the code is laid out

All in `src/main/java/com/eyeoverthink/band/`.

- `Audio`: sample rate 44,100 and block size 64 samples. Everything is sized from these.
- `Player`: the interface. Once per block a player `hear`s (others in the room, itself, and in its headphones
  the click and a track) and then `play`s.
- `Clock`: one player's pulse. `period`, `nextBeat`, `advance`, `drift`, `pullToward`, `nudge`.
- `Ear`: finds hits in a stream of sound: when, how loud, bright or dark. One ear per stream.
- `Room`: one ring buffer per player. A player's own sound reaches it in 3 ms, others' in 9 ms.
- `Instrument`: what a player's hands are on. `Kit` (pot and pan), `Hats` (one closed hi-hat) and `Bass`
  (fretless, low E and high E). In `Musician`, `hatsOnly` and `oneNote` mark a hat player and a bass player.
- `PitchEar`: measures the pitch of a player's own note from its own sound, by how far the bottom tone
  turns between two short listens. Accurate to about one hundredth of a semitone.
- `Sound`, `Thump` (pot), `Pan`: sounds are worked out once into tables and only read while playing.
- `Hearing`: plays a WAV past the players' ear and reports what it noticed; folds a recording onto one bar
  and can write that as a lesson. A tool for us, not something the players do yet.
- `Lesson`: a bar of sixteen places (`p` pot, `n` pan, `.` nothing), a tempo, and optionally its feel
  (`push` in ms and `weight` per place). Files in `lessons/`: `first`, `laid-back`, `on-top`.
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

Tests are in one class, `StepEighteenTest` (renamed each step). The big "study" tests run dozens of drummers
through whole lives and print tables; `README.md` quotes those tables.

### The player's states

`WAITING` (quiet until it hears someone), `LISTENING` (ticking along quietly, placing what it hears),
`ANSWERING` (playing its two bars after a call), `TOGETHER` (playing while others play).

### What a drummer learns, and what it is given

Learned, by trial, and saved in its life:

- **listening** (0 to 0.95): how strongly it pulls its pulse toward what it hears and toward the click. A
  newborn has 0.15. More is simply better (measured by setting it by hand), steeply at the low end.
  Learned by the inner class `Musician.Setting`: every two bars it takes stock of how uneven its own gaps
  were. Rest stretch, trial stretch (setting moved by 0.25), rest stretch; if the trial beat the average of
  the two rests by 2%, the setting moves one step (0.15 for a newborn, shrinking as `1/(1+hits/1500)`).
  The first stretch of a session is discarded. Leaning is learned the same way, on a cost that adds how
  far the others land from its ticks, how far it is from the click, and how uneven it has sounded.
- **ticks to the pulse** (1, 2, 4): earned when gaps exactly one tick long have sounded even enough for
  two bars running.
- **pattern**: sixteen places. One change per round; kept unless that round sounded clearly rougher than
  its playing usually does.
- **the lesson**: only what it heard, at the places its ticks reach.
- **feel**: for each place, milliseconds after the tick, how hard, and how many times heard. Taken on from
  records (and from a teaching drummer), a tenth each hearing; trusted fully after twenty hearings.
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
- **Learning that was not learning (found in step 13).** For twelve steps a setting was judged against the
  stretch before it. Players warm up during a session, so later always looked better, and the first
  direction tried each session was always up. Listening climbed, which happened to be good for it, and so
  did leaning, past where it helps. When the bias was removed, players at first learned nothing at all,
  which is how it was found. Rules that came out of it: compare a trial with both sides of it; discard the
  warm-up; randomise the first direction; and before trusting a learned value, set it by hand across its
  range and measure what each value really does (`Sweep` style). The README marks which tables are honest.
- **Settings that wandered (fixed in step 12).** The first learning rule moved a setting after every look,
  whether or not it helped, so on noisy evidence it random-walked across its whole range. In the real band
  the bass player's listening fell from 0.81 to 0.29 over 18 sessions. Also learned the hard way: a decision
  that needs more evidence than a session contains never gets made, because the trial is forgotten when
  the session ends. Keep a decision's evidence well inside one session.
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
- Feel cannot hold swing (no triplet ticks), a player has only one feel (it cannot keep two records
  apart), and it cannot play more than 10 ms ahead of its tick.
- Every session starts at 92 bpm, the tempo the players' bodies like.
- The bass has two notes an octave apart. That is not yet a bass line.
- A bass player rarely earns four ticks to the pulse.
- Settling is not taste. Players settle on agreeing with what they hear; an idea nobody else plays never
  lasts. Nothing original survives yet.
- **Honest learning is slower.** Without a click, 27 of 40 drummers have four ticks after 24 sessions (it was
  34 under the biased rule) and they land 20.7 ms from the record (it was 13.3).
- **Players do not learn to lean on each other.** Set by hand, leaning of 0.4 brings a practised trio from 7.3
  to 5.2 ms apart at almost no cost; above 0.6 it only roughens their time. But the gain is too small to
  hear in two bars, so honest trial leaves leaning near zero. It needs a better signal than trial and error.
- **The real band's leaning is 0.60 each**, learned under the biased rule. That is higher than is good for
  them and honest learning is not bringing it down. Do not edit the life files to fix it; fix the learning.
- Every session starts at 92 bpm, the tempo the players' bodies like.
- The bass has two notes an octave apart. That is not yet a bass line.
- A bass player rarely earns four ticks to the pulse.
- Settling is not taste. Players settle on agreeing with what they hear; an idea nobody else plays never
  lasts. Nothing original survives yet.
- **Old players learn slowly now**, so they recover slowly. After the fix the real bass player's listening
  climbed from 0.29 to only 0.39 in 16 sessions (it had been 0.81 before it wandered off). It is improving,
  not restored.
- **Leaning only goes up.** In the real band all three players have reached the most allowed (0.60). The
  test a player uses (do the others land closer to my ticks?) partly rewards leaning by construction. It
  needs a fairer test or a cost.
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

**Vaughn's direction, 2026-11-20: the players should study artists' style, as he did. "I grew up with people
who inspired me and they need inspiration also."** He is to be their first influence. He sent his own song,
"Gleam of the Prize" (on Spotify and Apple Music under Vaughn Scott), as an MP3. The agent cannot fetch
audio from streaming services; he has to send files. **His audio must not be committed to this public
repository.** Keep it in `songs/`, which is in `.gitignore`.

**What was found when his song was played past the players' ear (2026-11-20, `Main --hear`, class `Hearing`):**

- The file is `GleamOfThePreeSize_Cover_3.mp3`, 3:04, tagged title "GleamOfThePreeSize (Cover)", artist
  "hypnotizingmelody0426". Converted copy: `songs/gleam-of-the-prize.wav` (in the agent's workspace only;
  if the workspace is lost, ask him to send it again).
- **Its tempo is 92.10 to the minute, steady in every 20-second section.** The players' own natural tempo
  is 92, chosen arbitrarily in step 1. That is luck, and it means they can play along with it as they are.
- The ear noticed 805 hits, 679 of them bright. In a full mix nearly everything is bright to an ear built
  for a pot and a pan: voices, keys, hats, snare.
- Folded onto one bar it gave `p....n..n...nn..`, with a dark hit on the "one" in only 3 bars of 10. That
  is not a believable drum part, and the one is probably not in the right place. **Do not teach the real
  players from it.**
- The same folding on a clean recording of the record alone recovers tempo, bar and feel exactly (there is
  a test). The method is sound; the full mix is the problem.

**With two ears (step 15), same song:** 642 hits in the bottom of the sound and 704 in the top. Real
structure shows: the top has a steady hit on every eighth note (hats) through 46 to 92 s and 115 to 138 s;
the first 23 s are sparse. The bottom is a busy bass line, not a sparse kick, and folds to
`ppp.n.npppppn.pp`, with the low hits on the odd places about 26 ms late, cause unknown (slow attack of
the bass, or notes that really sit between ticks). **Where the one is has not been confirmed.** Vaughn has
no drum stem. He was sent `gleam-of-the-prize-pulse-marked.wav` (30 s from 0:46 with clicks on the pulse
and a higher click on the supposed one) and asked whether the high click is on the one. **He answered: the clicks are on the beat and it is on
the one** ("That song was a challenge. It's on the 1.").

**Roles (step 18), what was learned:**
- On his song, with each ear keeping its loudest-lately at 0.8: top hits of at least 0.7 fall on places 3, 5
  and 13 (the snare and something on the "and" of one); at least 0.35 gives steady eighths (the hats); at
  0.2 nearly every place is filled. So `HEAVY_HIGH` = 0.7 for the kit and `HAT_NOTICE` = 0.35 for the hats.
  These were chosen on one song.
- The bass player was copying the hat player's hats as high notes, because it was still learning from the
  players beside it. With a track on, everyone now learns from the track alone.
- The hat player kept every place it had ever heard once. Hat and bass players now drop a place heard less
  than one time in four.
- In the real band after twelve takes with roles: kit `p.n.n...ppp.n..p`, hats `n.nnn.nnnnnnnnn.` (busy),
  bass `p.p.p.p.p.p...p.`. Three different parts.

**Playing along with the whole band (step 17), what was learned:**
- With feel, leaning on another player's hits went wrong: each dragged the others toward notes that were late
  on purpose (unsteadiness 18 to 25 ms). Fixed by measuring another player against where it would play that
  place itself, feel included. Back to 6 to 9 ms.
- The first try put the track in the room. Then a drummer heard the bass player through the same two ears
  as the song. Headphones are both cleaner and what a real band does.
- A feel cannot be more than 30 ms behind or 10 ms ahead. The bottom of his song put some places 35 ms late.

**Playing along (step 16), what was learned:**
- Copying every hit the two ears notice makes a drummer mirror the whole mix (15 of 16 places filled). The
  drum part only appears when it notices the heavy hits alone: at least 0.6 in the bottom and 0.5 in the top,
  where each ear keeps the loudest-lately at 0.8. Offline, heavy low hits in his song fall on places 1, 9 and
  11 (kick on one, three and the "and" of three) and heavy high hits on 3, 5 and 13.
- Leaning on the recording made the drummer's time rough (20 to 29 ms). A recording does not lean back and
  its hits carry their own feel. With a click it now stays on the click; unsteadiness is back to 8 to 10 ms.
- Everything it heard in the song was about 9 ms early against the click, the same at every place. That is
  the click sitting slightly late on the song (the tool lines it up on all hits, and the bass is late), not
  a feel. Feel is now measured against the one, so a constant offset is not carried into other sessions.

In order:

0. Done for the whole band in steps 16 to 18, with roles. Still to do under this heading: line the click up on the heavy
   hits only; a second song, to see whether the thresholds hold; and the players all ending up on much the
   same line, which is the next thing to fix (item 2).
1. **Let a player listen to a real recording.** A player that plays a WAV into the room (convert MP3 with
   ffmpeg outside the Java code; the players are still plain Java). The hard parts: the players' ear was
   built for a pot and a pan, not a full mix; a song has its own tempo and the players only know 92 to 96;
   and a listener needs to find the one. Start by reporting honestly what the ear can and cannot hear in
   his song. A drum stem from him would make this far easier; he has been asked. Without one, the
   ear needs to separate low from high before it looks for hits (two ears: one on the bottom of the
   sound, one on the top), and a way to find the one that does not depend on a kick landing there.
2. **More than one influence, kept apart.** A player should hold what it took from each record separately
   and have its own mix, so an idea of its own is something borrowed from one influence and tried in
   another. This is what should finally let something original survive.
3. A way for players to learn leaning that works (section 7). Untried idea: if another player's hits keep
   landing on the same side of its ticks, lean more; if they flip from side to side, lean less.
4. Triplet ticks, so swing can be heard and played. "Genre lives in the ticks."
5. More notes for the bass (a fifth, then a scale), each earned by playing the ones it has in tune.
6. Trading bars between players as soloists.
7. The other instruments in his lineup, one at a time, each born new.
8. Stereo, so players can be heard apart.
9. Real devices (section 9).

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
