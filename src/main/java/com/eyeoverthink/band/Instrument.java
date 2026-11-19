package com.eyeoverthink.band;

/** What a player's hands are on. Claimed once; nothing is made while playing. */
public interface Instrument {
    /** Start a sound: for a kit {@link Kit#POT} or {@link Kit#PAN}; a one-note instrument plays its note for either. */
    void strike(byte sound, float loudness);

    /** The next sample of whatever is ringing. */
    float next();

    /** What its life file says it plays. */
    String describe();
}
