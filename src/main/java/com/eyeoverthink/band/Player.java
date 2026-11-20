package com.eyeoverthink.band;

/**
 * Anyone in the room. A player hears what the room carried to it, then plays into the room.
 * Both calls happen once per block and must not create objects: all memory is claimed before the count-in.
 */
public interface Player {
    String name();

    /**
     * What reached this player during this block: everyone else in the room, its own sound, and what is in its
     * headphones: the click, and a track it is playing along with (silence where there is none).
     */
    void hear(float[] others, float[] own, float[] click, float[] track, int n, long blockStart);

    /** Put this player's sound for this block into {@code out}. */
    void play(float[] out, int n, long blockStart);
}
