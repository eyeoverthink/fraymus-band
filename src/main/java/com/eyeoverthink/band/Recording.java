package com.eyeoverthink.band;

/**
 * A recording playing in the room: a real song, from a file. It does not listen and it does not wait.
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
    @Override public void hear(float[] others, float[] own, float[] click, int n, long blockStart) { }

    @Override
    public void play(float[] out, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            long at = startsAt + blockStart + i;
            if (at >= 0 && at < sound.length) out[i] += sound[(int) at] * level;
        }
    }
}
