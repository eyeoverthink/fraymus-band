package com.eyeoverthink.band;

/**
 * A player's own clock. There is no master clock in the room: each player keeps its own pulse
 * and moves it by listening. Times are in samples, kept as doubles so a pulse can fall between samples.
 */
public final class Clock {
    private double period;
    private double nextBeat;

    public Clock(double periodSamples, double firstBeat) {
        this.period = periodSamples;
        this.nextBeat = firstBeat;
    }

    public double period() { return period; }
    public double nextBeat() { return nextBeat; }

    /** True when this clock's next pulse falls before the end of the block. */
    public boolean dueBefore(long blockEnd) { return nextBeat < blockEnd; }

    /** Move on to the following pulse. */
    public void advance() { nextBeat += period; }

    /** Move on by part of a pulse: one tick, when the player is dividing its pulse into ticks. */
    public void advance(double partOfPulse) { nextBeat += period * partOfPulse; }

    /** Let the pulse wander by a fraction of itself, the way an unpractised hand does. */
    public void drift(double fraction) { period *= 1.0 + fraction; }

    /** Pull the pulse part of the way toward a length the player heard or prefers. */
    public void pullToward(double targetPeriod, double strength) { period += strength * (targetPeriod - period); }
}
