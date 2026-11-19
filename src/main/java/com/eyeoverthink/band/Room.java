package com.eyeoverthink.band;

/**
 * The air the players share. Sound put into the room reaches ears a moment later. That delay is the
 * only way a player ever hears anything, including itself. The buffer is a ring claimed once:
 * samples move through it and nothing is created while the music runs.
 */
public final class Room {
    private final float[] air;
    private final int mask;
    private final int delay;

    public Room(double delaySeconds) {
        this.delay = (int) Math.round(Audio.samples(delaySeconds));
        int size = Integer.highestOneBit(delay + 2 * Audio.BLOCK) << 1;
        this.air = new float[size];
        this.mask = size - 1;
    }

    public int delaySamples() { return delay; }

    /** What reaches the ears during the block that starts at {@code blockStart}. */
    public void carry(float[] heard, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            long from = blockStart + i - delay;
            heard[i] = from < 0 ? 0f : air[(int) (from & mask)];
        }
    }

    /** Put the block the players just made into the air. */
    public void fill(float[] played, int n, long blockStart) {
        for (int i = 0; i < n; i++) air[(int) ((blockStart + i) & mask)] = played[i];
    }
}
