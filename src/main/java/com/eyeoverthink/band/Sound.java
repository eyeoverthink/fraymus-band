package com.eyeoverthink.band;

/** One sound a player can make, worked out once before the session and then only read. */
public interface Sound {
    int length();
    float at(int i);
}
