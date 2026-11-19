# For any agent working in this repository

Read `HANDOFF.md` before doing anything. It says what this project is, how Vaughn wants the work done,
what exists, what went wrong before, and what comes next. Keep it up to date: at the end of every step,
update sections 3, 6, 7 and 8 and commit it with the step.

The short version:

- A band of AI players in plain Java 21. Every player performs live, owns one instrument, has its own
  clock, and has a life it lived. No Python. Nothing allocated in the audio loop.
- One step at a time. Each step works, passes `mvn package`, and is pushed before the next begins.
- Never edit, delete or regenerate the files in `lives/`. They are the players.
- You cannot hear audio. Send Vaughn the WAV and say so. Report what was measured, what is still wrong,
  and what was given to the players versus what they learned.
- He reads on a phone. Short replies, result first.
