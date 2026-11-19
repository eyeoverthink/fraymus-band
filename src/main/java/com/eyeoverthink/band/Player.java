package com.eyeoverthink.band;

/**
 * Anyone in the room. A player hears what the room carried to it, then plays into the room.
 * Both calls happen once per block and must not create objects: all memory is claimed before the count-in.
 */
public interface Player {
    String name();

    /** What reached this player during this block: everyone else, its own sound, and the click in its ears (silence if there is none). */
    void hear(float[] others, float[] own, float[] click, int n, long blockStart);

    /** Put this player's sound for this block into {@code out}. */
    void play(float[] out, int n, long blockStart);
}
