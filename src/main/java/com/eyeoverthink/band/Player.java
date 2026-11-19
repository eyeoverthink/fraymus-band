package com.eyeoverthink.band;

/**
 * Anyone in the room. A player hears what the room carried to it, then plays into the room.
 * Both calls happen once per block and must not create objects: all memory is claimed before the count-in.
 */
public interface Player {
    String name();

    /** What reached this player's ears during this block. {@code blockStart} is the sample time of {@code heard[0]}. */
    void hear(float[] heard, int n, long blockStart);

    /** Add this player's sound for this block to {@code out}. */
    void play(float[] out, int n, long blockStart);
}
