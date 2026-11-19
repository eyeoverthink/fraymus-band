package com.eyeoverthink.band;

/** When a player's hits landed, so two players can be compared afterwards. */
public interface Hits {
    int hitCount();
    long hitAt(int i);
}
