package com.eyeoverthink.band;

/**
 * The air the players share. Each player stands somewhere in it. A player's own sound reaches it almost
 * at once; everyone else's arrives a moment later, from across the room. So a player can tell its own
 * hits from the others', the way you can feel your own hands. That is given to it, not learned.
 * The buffers are rings claimed once: samples move through them and nothing is created while the music runs.
 */
public final class Room {
    private final float[][] air;          // what each player has put into the room
    private final int mask;
    private final int near, across;

    public Room(int players, double nearSeconds, double acrossSeconds) {
        this.near = (int) Math.round(Audio.samples(nearSeconds));
        this.across = (int) Math.round(Audio.samples(acrossSeconds));
        if (near < Audio.BLOCK || across < Audio.BLOCK) throw new IllegalArgumentException("Sound cannot arrive sooner than one block");
        int size = Integer.highestOneBit(Math.max(near, across) + 2 * Audio.BLOCK) << 1;
        this.air = new float[players][size];
        this.mask = size - 1;
    }

    /** What reaches one player during the block that starts at {@code blockStart}: the others, and itself. */
    public void carry(int listener, float[] others, float[] own, int n, long blockStart) {
        for (int i = 0; i < n; i++) {
            long mine = blockStart + i - near, theirs = blockStart + i - across;
            own[i] = mine < 0 ? 0f : air[listener][(int) (mine & mask)];
            float sum = 0f;
            if (theirs >= 0) for (int p = 0; p < air.length; p++) if (p != listener) sum += air[p][(int) (theirs & mask)];
            others[i] = sum;
        }
    }

    /** Put the block one player just made into the air. */
    public void fill(int player, float[] played, int n, long blockStart) {
        for (int i = 0; i < n; i++) air[player][(int) ((blockStart + i) & mask)] = played[i];
    }
}
