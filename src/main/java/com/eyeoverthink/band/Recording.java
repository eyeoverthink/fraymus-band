package com.eyeoverthink.band;

/**
 * A recording: a real song, from a file. The players hear it in their headphones, as a band does when it
 * practises to a record, and it goes onto the take. It does not listen and it does not wait.
 * The sound was read into memory before the session; while playing it only copies samples.
 */
public final class Recording implements Player {
    private final float[] sound;
    private final long startsAt;          // which sample of the recording is playing at the first moment of the session
    private final float level;

    /** {@code fromSeconds} may be negative: then the room is quiet until the recording begins. */
    public Recording(float[] sound, double fromSeconds, float level) {
        this.sound = sound;
        this.startsAt = Math.round(Audio.samples(fromSeconds));
        this.level = level;
    }

    @Override public String name() { return "the recording"; }
    @Override public void hear(float[] others, float[] own, float[] click, float[] track, int n, long blockStart) { }

    @Override
    public void play(float[] out, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            long at = startsAt + blockStart + i;
            out[i] = at >= 0 && at < sound.length ? sound[(int) at] * level : 0f;
        }
    }
}
